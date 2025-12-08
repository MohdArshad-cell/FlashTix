package com.flashtix.backend.repository;

import com.flashtix.backend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // No code needed here! 
    // Spring Boot automatically gives you methods to talk to the DB.
}