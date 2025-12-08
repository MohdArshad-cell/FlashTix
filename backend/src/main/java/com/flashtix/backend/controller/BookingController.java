package com.flashtix.backend.controller;

import com.flashtix.backend.entity.Ticket;
import com.flashtix.backend.repository.TicketRepository;
import com.flashtix.backend.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class BookingController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    // 1. Create Dummy Tickets (Run this once to setup DB)
    @PostMapping("/seed")
    public String seedTickets() {
        for (int i = 1; i <= 100; i++) {
            Ticket t = new Ticket();
            t.setSeatNumber("Seat-" + i);
            t.setStatus("AVAILABLE");
            ticketRepository.save(t);
        }
        return "Created 100 Seats!";
    }

    // 2. The High-Concurrency Booking Endpoint
    @PostMapping("/book")
    public String bookTicket(@RequestParam Long ticketId, @RequestParam Long userId) {
        return ticketService.bookTicket(ticketId, userId);
    }
}