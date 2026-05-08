package com.rashid.ai_helpdesk.service;

import java.util.List;

import com.rashid.ai_helpdesk.entity.Status;
import com.rashid.ai_helpdesk.entity.TicketEntity;

public interface TicketService {

    TicketEntity createTicket(
        Long creatBy,
        String title,
        String description
    );


    
    TicketEntity findTicketById(Long ticketId);

    List<TicketEntity> findAllTickets();

    void updateTicketStatus(Long ticketId, Status status);

    void deleteTicket(Long ticketId);
    

}
