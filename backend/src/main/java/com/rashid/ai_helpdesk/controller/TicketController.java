package com.rashid.ai_helpdesk.controller;

import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rashid.ai_helpdesk.payload.request.TicketRequest;
import com.rashid.ai_helpdesk.security.jwt.JwtUtils;
import com.rashid.ai_helpdesk.service.TicketServiceImpl;
import com.rashid.ai_helpdesk.security.jwt.UserDetailsImpl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

import com.rashid.ai_helpdesk.entity.TicketEntity;
import com.rashid.ai_helpdesk.repository.TicketRepo;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ticket")

public class TicketController {

    private  Long logedInUserId;

    private final TicketServiceImpl ticketServiceImpl;
    private final UserDetailsServiceImpl userDetailsService;

    private final JwtUtils jwtUtils;

    public TicketController(TicketServiceImpl ticketServiceImpl,
            JwtUtils jwtUtils,
            UserDetailsServiceImpl user) {
        this.ticketServiceImpl = ticketServiceImpl;
        this.jwtUtils = jwtUtils;
    }




      @GetMapping("/getTicket")

      public TicketEntity getTicket(){
   return  ticketServiceImpl.findTicketById(this.logedInUserId);

      }
    




@PostMapping
public ResponseEntity<?> createTicket(
        @AuthenticationPrincipal UserDetailsImpl loggedInUser,
        @Valid @RequestBody TicketRequest request) {

    if (loggedInUser == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("You are not logged in");
    }

    Long userId = loggedInUser.getId();

    ticketServiceImpl.createTicket(

            userId,
            request.getTitle(),
            request.getDescription()
    );

    return ResponseEntity.status(HttpStatus.CREATED)
            .body("Ticket created successfully");
}


          }  

