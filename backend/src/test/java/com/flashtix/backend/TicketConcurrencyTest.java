package com.flashtix.backend;

import com.flashtix.backend.entity.Ticket;
import com.flashtix.backend.repository.TicketRepository;
import com.flashtix.backend.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class TicketConcurrencyTest {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    public void testConcurrency() throws InterruptedException {
        // 1. Setup: Create a single ticket
        Ticket t = new Ticket();
        t.setSeatNumber("VIP-1");
        t.setStatus("AVAILABLE");
        ticketRepository.save(t);
        Long ticketId = t.getId();

        // 2. The Attack: Simulate 100 users trying to buy it at once
        int numberOfThreads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            long userId = 1000 + i;
            executor.submit(() -> {
                String result = ticketService.bookTicket(ticketId, userId);
                if (result.contains("Success")) {
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
            });
        }

        // 3. Wait for all attacks to finish
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // 4. The Verdict (The Proof)
        System.out.println("Successful Bookings: " + successCount.get());
        System.out.println("Failed Bookings: " + failCount.get());

        // ONLY ONE should succeed. 99 MUST fail.
        assertEquals(1, successCount.get()); 
        assertEquals(99, failCount.get());
    }
}