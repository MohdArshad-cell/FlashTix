package com.flashtix.backend.controller;

import com.flashtix.backend.entity.Ticket;
import com.flashtix.backend.entity.TicketStatus;
import com.flashtix.backend.repository.TicketRepository;
import com.flashtix.backend.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@Validated
public class BookingController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @PostMapping("/seed")
    public ResponseEntity<String> seedTickets() {
        if (ticketRepository.count() > 0) {
            return ResponseEntity.ok("Database already has tickets.");
        }
        for (int i = 1; i <= 100; i++) {
            Ticket t = new Ticket();
            t.setSeatNumber("Seat-" + i);
            t.setStatus(TicketStatus.AVAILABLE);
            ticketRepository.save(t);
        }
        return ResponseEntity.ok("Created 100 Seats!");
    }

    @Operation(summary = "Book a ticket", 
               description = "Handles high concurrency using Redis Distributed Locks + Optimistic Locking with Auto-Retry")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking Successful"),
        @ApiResponse(responseCode = "400", description = "Invalid Input"),
        @ApiResponse(responseCode = "409", description = "Conflict: Ticket sold or locked")
    })
    @PostMapping("/book")
    public ResponseEntity<Ticket> bookTicket(
            @RequestParam @Positive(message = "Ticket ID must be positive") Long ticketId,
            @RequestParam @Positive(message = "User ID must be positive") Long userId) {
        
        Ticket bookedTicket = ticketService.bookTicket(ticketId, userId);
        return ResponseEntity.ok(bookedTicket);
    }
}
