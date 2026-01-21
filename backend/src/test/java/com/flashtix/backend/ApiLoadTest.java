package com.flashtix.backend;

import com.flashtix.backend.entity.Ticket;
import com.flashtix.backend.entity.TicketStatus;
import com.flashtix.backend.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiLoadTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    public void testHighConcurrencyAttack() throws InterruptedException {
        // Setup: Create one ticket
        ticketRepository.deleteAll();
        Ticket ticket = new Ticket();
        ticket.setSeatNumber("Seat-Attack");
        ticket.setStatus(TicketStatus.AVAILABLE);
        ticket = ticketRepository.save(ticket);
        Long ticketId = ticket.getId();
        
        String url = "http://localhost:" + port + "/api/tickets/book?ticketId=" + ticketId + "&userId=";
        
        // ✅ Increased to 5000 threads
        int numberOfThreads = 5000;
        // ✅ Use virtual threads for better performance (Java 21+) or larger thread pool
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(numberOfThreads, 200));
        
        // ✅ Configure HttpClient with connection pool and timeouts
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger otherErrors = new AtomicInteger(0);

        System.out.println("🚀 STARTING MASSIVE ATTACK with " + numberOfThreads + " users on localhost:" + port + "...");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfThreads; i++) {
            int userId = 1000 + i;
            executor.submit(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url + userId))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .timeout(Duration.ofSeconds(10))
                            .build();

                    HttpResponse<String> response = client.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                    
                    if (response.statusCode() == 200) {
                        successCount.incrementAndGet();
                    } else if (response.statusCode() == 409) {
                        conflictCount.incrementAndGet();
                    } else {
                        otherErrors.incrementAndGet();
                    }
                } catch (Exception e) {
                    otherErrors.incrementAndGet();
                    // Don't print stack trace for each error to avoid console flood
                }
            });
        }

        executor.shutdown();
        // ✅ Increased timeout to 60 seconds for 5000 requests
        boolean completed = executor.awaitTermination(60, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(completed, "Load test threads did not complete within timeout");

        System.out.println("✅ ATTACK FINISHED in " + duration + "ms");
        System.out.println("Success (200): " + successCount.get());
        System.out.println("Conflicts (409): " + conflictCount.get());
        System.out.println("Other Errors: " + otherErrors.get());
        System.out.println("Throughput: " + (numberOfThreads * 1000.0 / duration) + " requests/sec");

        // CRITICAL ASSERTIONS
        assertEquals(1, successCount.get(), 
            "Exactly one request should succeed with 200 OK");
        assertEquals(4999, conflictCount.get(), 
            "Remaining 4999 should get 409 Conflict");
        
        // ✅ Allow some errors due to timeouts under extreme load
        assertTrue(otherErrors.get() < 100, 
            "Should have minimal unexpected errors (got " + otherErrors.get() + ")");
        
        // Verify database integrity
        Ticket finalTicket = ticketRepository.findById(ticketId).orElseThrow();
        assertEquals(TicketStatus.SOLD, finalTicket.getStatus());
        assertNotNull(finalTicket.getUserId(), "Winner userId should be assigned");
        
        System.out.println("🎯 Winner: User " + finalTicket.getUserId());
    }
}
