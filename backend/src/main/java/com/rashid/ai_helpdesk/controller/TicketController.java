package com.rashid.ai_helpdesk.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rashid.ai_helpdesk.entity.Status;
import com.rashid.ai_helpdesk.entity.TicketEntity;
import com.rashid.ai_helpdesk.payload.request.TicketRequest;
import com.rashid.ai_helpdesk.payload.response.MessageResponse;
import com.rashid.ai_helpdesk.security.jwt.JwtUtils;
import com.rashid.ai_helpdesk.security.jwt.UserDetailsImpl;
import com.rashid.ai_helpdesk.service.TicketServiceImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ticket")

public class TicketController {

    private Long logedInUserId;

    private final TicketServiceImpl ticketServiceImpl;
    private final JwtUtils jwtUtils;

    List<Status> ticketStatus = new ArrayList<>();

    public TicketController(TicketServiceImpl ticketServiceImpl,
            JwtUtils jwtUtils) {

        this.jwtUtils = jwtUtils;
        this.ticketServiceImpl = ticketServiceImpl;
    }

    @GetMapping("/getTicket")

    public TicketEntity getTicket() {
        return ticketServiceImpl.findTicketById(this.logedInUserId);

    }

    @PostMapping("/createTicket")
    public ResponseEntity<?> createTicket(
            @AuthenticationPrincipal UserDetailsImpl loggedInUser,
            @Valid @RequestBody TicketRequest request) {
        try {
            if (loggedInUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("You are not logged in"));
            }

            Long userId = loggedInUser.getId();

            ticketServiceImpl.createTicket(
                    userId,
                    request.getTitle(),
                    request.getDescription());

            return ResponseEntity.ok(
                    new MessageResponse("Successfully created your ticket"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Could not create the ticket"));
        }
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<List<TicketEntity>> getMyTickets(
            @AuthenticationPrincipal UserDetailsImpl user) {

        return ResponseEntity.ok(
                ticketServiceImpl.findTicketsByUserId(user.getId()));
    }



}


/*
Entity/List
→ JSON
*/
