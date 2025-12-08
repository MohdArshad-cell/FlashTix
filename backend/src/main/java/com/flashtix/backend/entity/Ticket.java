package com.flashtix.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seatNumber;
    private String status; // "AVAILABLE" or "SOLD"
    private Long userId;

    @Version
    private Long version; 

    // --- CONSTRUCTORS ---
    public Ticket() {}

    // --- GETTERS AND SETTERS (Manual) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}