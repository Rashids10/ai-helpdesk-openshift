package com.rashid.ai_helpdesk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rashid.ai_helpdesk.entity.TicketEntity;


public interface TicketRepo extends JpaRepository<TicketEntity, Long> {

    List<TicketEntity> findByCreatedBy(Long userId);

}
