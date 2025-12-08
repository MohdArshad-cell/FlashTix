package com.flashtix.backend.service;

import com.flashtix.backend.entity.Ticket;
import com.flashtix.backend.repository.TicketRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // --- STEP 1: FAST CHECK (CACHE) ---
    public String bookTicket(Long ticketId, Long userId) {
        String cacheKey = "ticket_lock:" + ticketId;

        // 1. Redis Atomic Check: Kya ticket pehle se locked hai?
        // setIfAbsent = SETNX (Set if Not Exists). Returns true agar lock mil gaya.
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(cacheKey, String.valueOf(userId));

        if (Boolean.FALSE.equals(acquired)) {
            return "Too Slow! Someone else is booking this seat.";
        }

        try {
            // Agar Redis lock mil gaya, toh Database transaction shuru karo
            return processBookingInDatabase(ticketId, userId);
        } finally {
            // Transaction khatam hone ke baad Redis lock hata do taaki koi aur try kar sake
            // (Agar fail hua ho toh)
            // Note: Production mein hum isse expire time ke saath set karte hain.
            redisTemplate.delete(cacheKey);
        }
    }

    // --- STEP 2: RELIABLE CHECK (DATABASE LOCK) ---
    @Transactional
    public String processBookingInDatabase(Long ticketId, Long userId) {
        try {
            Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);

            if (ticketOpt.isEmpty()) {
                return "Invalid Ticket ID";
            }

            Ticket ticket = ticketOpt.get();

            if ("SOLD".equals(ticket.getStatus())) {
                return "Sold Out!";
            }

            // --- CRITICAL SECTION ---
            // Yahan Optimistic Locking magic hota hai.
            // Hum Java object ko update kar rahe hain.
            // Jab transaction commit hoga, Hibernate check karega:
            // "Kya DB mein version abhi bhi same hai?"
            ticket.setStatus("SOLD");
            ticket.setUserId(userId);

            // Save call karega version check + increment
            ticketRepository.save(ticket); 

            return "Success! Ticket Booked for User " + userId;

        } catch (OptimisticLockException e) {
            // Agar do log same time pe yahan tak pahunch gaye,
            // toh ek ka version match nahi karega aur yeh exception aayega.
            return "Race Condition Detected! Booking Failed. Try Again.";
        }
    }
}