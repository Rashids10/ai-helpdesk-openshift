package com.rashid.ai_helpdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rashid.ai_helpdesk.entity.TicketEntity;

public interface TicketRepo extends JpaRepository<TicketEntity, Long> {

    

}
