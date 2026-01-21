package com.flashtix.backend.service;

import com.flashtix.backend.entity.Ticket;
import com.flashtix.backend.entity.TicketStatus;
import com.flashtix.backend.exception.TicketBookingException;
import com.flashtix.backend.repository.TicketRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
public class TicketService {

    private static final String UNLOCK_LUA_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    private static final long LOCK_TTL_SECONDS = 5;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final Counter soldOutCounter;
    private final Counter lockContentionCounter;
    private final Timer bookingTimer;

    public TicketService(MeterRegistry registry) {
        this.soldOutCounter = Counter.builder("flashtix.sold.out")
                .description("Count of booking attempts rejected because ticket was already sold")
                .register(registry);
        
        this.lockContentionCounter = Counter.builder("flashtix.lock.contention")
                .description("Count of booking attempts that failed to acquire Redis lock")
                .register(registry);
        
        this.bookingTimer = Timer.builder("flashtix.booking.duration")
                .description("Time taken to complete booking operation")
                .register(registry);
    }

    public Ticket bookTicket(Long ticketId, Long userId) {
        return bookingTimer.record(() -> {
            String lockKey = "ticket_lock:" + ticketId;
            String userIdStr = String.valueOf(userId);

            Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, userIdStr, LOCK_TTL_SECONDS, TimeUnit.SECONDS);

            if (Boolean.FALSE.equals(acquired)) {
                lockContentionCounter.increment();
                throw new TicketBookingException("Too many requests! Please try again.");
            }

            try {
                return processBookingInDatabase(ticketId, userId);
            } finally {
                RedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_LUA_SCRIPT, Long.class);
                redisTemplate.execute(script, Collections.singletonList(lockKey), userIdStr);
            }
        });
    }

    // ✅ KEEP ONLY THIS ONE METHOD - Delete any duplicate below this
    @Retryable(
        value = {JpaOptimisticLockingFailureException.class, OptimisticLockException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public Ticket processBookingInDatabase(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketBookingException("Invalid Ticket ID"));

        if (TicketStatus.SOLD.equals(ticket.getStatus())) {
            soldOutCounter.increment();
            throw new TicketBookingException("Sold Out!");
        }

        ticket.setStatus(TicketStatus.SOLD);
        ticket.setUserId(userId);
        return ticketRepository.save(ticket);
    }
    
    // ❌ DELETE any second processBookingInDatabase method if it exists below
}
