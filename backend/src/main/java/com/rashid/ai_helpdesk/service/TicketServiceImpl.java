package com.rashid.ai_helpdesk.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rashid.ai_helpdesk.entity.Status;
import com.rashid.ai_helpdesk.entity.TicketEntity;
import com.rashid.ai_helpdesk.repository.TicketRepo;

import jakarta.transaction.Transactional;


@Service
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
        ticket.setTicket_status(Status.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdetAt(LocalDateTime.now());

        return ticketRepo.save(ticket);
    }


    @Override
    public TicketEntity findTicketById(Long ticketId) {

        return ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }

    @Override

    public List<TicketEntity> findAllTickets() {
        return ticketRepo.findAll();
    }

    @Override
    @Transactional

    public void updateTicketStatus(Long ticketId, Status status) {
        TicketEntity ticket = findTicketById(ticketId);
        ticket.setTicket_status(status);
        ticket.setUpdetAt(LocalDateTime.now());
        ticketRepo.save(ticket);

    }

    @Override
    @Transactional
    public void deleteTicket(Long ticketId) {
        ticketRepo.deleteById(ticketId);
    }


    public Status getTicketStatus(){
        TicketEntity ticketStatus= new TicketEntity();

        return ticketStatus.getTicket_status();

    }

    public Long getTicketId(){


        TicketEntity ticket = new TicketEntity();

       if(ticket.getId()!= null){

        return ticket.getId();
       }


       return null;
    }




    public List<TicketEntity> findTicketsByUserId(Long userId){
     return  ticketRepo.findByCreatedBy(userId);

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
