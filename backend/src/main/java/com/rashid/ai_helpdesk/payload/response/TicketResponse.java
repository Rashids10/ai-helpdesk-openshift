package com.rashid.ai_helpdesk.payload.response;

import com.rashid.ai_helpdesk.entity.Status;

// Welche Daten schickt das Backend zurück an den Client?“

public class TicketResponse {

    private Long ticket;
    private String title;
    private String description;

    private Status ticketStatus;

    public TicketResponse(String description, Long ticket, Status ticketStatus, String title) {
        this.description = description;
        this.ticket = ticket;
        this.ticketStatus = ticketStatus;
        this.title = title;
    }

    public Long getTicket() {
        return ticket;
    }

    public void setTicket(Long ticket) {
        this.ticket = ticket;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(Status ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

}
