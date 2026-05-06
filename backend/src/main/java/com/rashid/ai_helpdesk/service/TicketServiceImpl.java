package com.rashid.ai_helpdesk.service;

import java.util.List;

import com.rashid.ai_helpdesk.entity.Status;
import com.rashid.ai_helpdesk.entity.TicketEntity;
import com.rashid.ai_helpdesk.repository.TicketRepo;

import jakarta.transaction.Transactional;

public class TicketServiceImpl implements TicketService {

    private final TicketRepo ticketRepo;

    public TicketServiceImpl(TicketRepo ticketRepo) {
        this.ticketRepo = ticketRepo;
    }

    @Override
    @Transactional
    public TicketEntity createTicket(
            Long createdBy,
            String title,
            String description)

    {

        TicketEntity ticket = new TicketEntity();

        ticket.setCreatedBy(createdBy);

        ticket.setTitle(title);

        ticket.setDescription(description);

        return ticket; // nochmal checken hier
    }

    @Override

    public TicketEntity findTicketById(Long ticketId) {

        return ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }

    @Override

    public List<TicketEntity> findAllTickets() {
        return null;
    }

    @Override
    @Transactional

    public void updateTicketStatus(Long ticketId, Status status) {
        //

    }

    @Override
    @Transactional
    public void deleteTicket(Long ticketId) {

    }

    public void getTicketStatus(){
        //
    }




    
    public Ticket findAnswer(Long ticketId) {
        /*
         * hier wird RAG AUF gerufen und die Antwort zurückgegeben und wenn nciht dann
         * wird ein ticket erstellt
         * r
         * }
         */
        return null;
    }





}
