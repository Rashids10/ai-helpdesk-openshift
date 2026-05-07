package com.rashid.ai_helpdesk.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class TicketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status")
    private Status ticket_status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "updated_at")
    private LocalDateTime updetAt;


    public Long getId() {
        return id;
    }


    public Long createdBy() {
        return createdBy;
    }


    public String getTitle() {
        return title;
    }


    public String getDescription() {
        return description;
    }


    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public void setTicket_status(Status ticket_status) {
        this.ticket_status = ticket_status;
    }


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public void setUpdetAt(LocalDateTime updetAt) {
        this.updetAt = updetAt;
    }


    public Status getTicket_status() {
        return ticket_status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getUpdetAt() {
        return updetAt;
    }

}
