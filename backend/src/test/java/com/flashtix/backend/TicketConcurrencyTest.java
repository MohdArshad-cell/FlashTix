package com.flashtix.backend;

import com.flashtix.backend.entity.Ticket;
import com.flashtix.backend.entity.TicketStatus;
import com.flashtix.backend.repository.TicketRepository;
import com.flashtix.backend.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TicketConcurrencyTest {

    private static final Logger logger = LoggerFactory.getLogger(TicketConcurrencyTest.class);

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    public void setup() {
        ticketRepository.deleteAll();
    }

    @Test
public void testConcurrency() throws InterruptedException {
    // 1. Setup
    Ticket t = new Ticket();
    t.setSeatNumber("VIP-TEST-1");
    t.setStatus(TicketStatus.AVAILABLE);
    ticketRepository.save(t);
    Long ticketId = t.getId();

    // 2. The Load: 5000 threads
    int numberOfThreads = 5000;
    // ✅ Limit thread pool size to prevent resource exhaustion
    ExecutorService executor = Executors.newFixedThreadPool(200);
    CountDownLatch latch = new CountDownLatch(1);
    
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);

    logger.info("🚀 Launching {} concurrent booking attempts", numberOfThreads);

    for (int i = 0; i < numberOfThreads; i++) {
        long userId = 1000 + i;
        executor.submit(() -> {
            try {
                latch.await(); // Wait for start signal
                ticketService.bookTicket(ticketId, userId);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            }
        });
    }

    // 3. Start!
    latch.countDown();
    
    executor.shutdown();
    // ✅ Increased timeout to 60 seconds
    boolean completed = executor.awaitTermination(60, TimeUnit.SECONDS);
    assertTrue(completed, "Threads did not complete within timeout");

    // 4. Verify
    logger.info("Total Requests: {}", numberOfThreads);
    logger.info("Success: {}", successCount.get());
    logger.info("Failed: {}", failCount.get());

    assertEquals(1, successCount.get(), "Exactly one ticket should be sold!");
    assertEquals(numberOfThreads - 1, failCount.get(), "Fail count mismatch");
    
    // Enhanced verification
    Ticket updatedTicket = ticketRepository.findById(ticketId).orElseThrow();
    assertEquals(TicketStatus.SOLD, updatedTicket.getStatus());
    assertNotNull(updatedTicket.getUserId(), "Winner userId should be assigned");
    assertTrue(updatedTicket.getUserId() >= 1000 && updatedTicket.getUserId() < 6000,
        "UserId should be from our test range");
    
    assertNotNull(updatedTicket.getVersion(), "Version should be set");
    assertTrue(updatedTicket.getVersion() > 0, "Version should be incremented");
    
    logger.info("✅ Test passed: Winner UserId = {}, Version = {}", 
        updatedTicket.getUserId(), updatedTicket.getVersion());
}

}
