package com.rashid.ai_helpdesk.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rashid.ai_helpdesk.payload.request.LoginRequest;
import com.rashid.ai_helpdesk.payload.request.SignUpRequest;
import com.rashid.ai_helpdesk.payload.response.JwtResponse;
import com.rashid.ai_helpdesk.payload.response.MessageResponse;
import com.rashid.ai_helpdesk.security.jwt.JwtUtils;
import com.rashid.ai_helpdesk.security.jwt.UserDetailsImpl;
import com.rashid.ai_helpdesk.service.UserDetailsServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            UserDetailsServiceImpl userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/signup")
    @Operation(summary = "Registriert einen neuen User", description = "Erstellt einen neuen Account mit Email, Username und Passwort")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userDetailsService.assureEmailExists(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email is already in use."));
        }
        
        userDetailsService.createUser(
                signUpRequest.getEmail(),
                signUpRequest.getUsername(),
                signUpRequest.getPassword());

Authentication authentication = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(
        signUpRequest.getEmail(),
        signUpRequest.getPassword()
    )
);

String jwt = jwtUtils.generateJwtToken(authentication);

UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

return ResponseEntity.status(HttpStatus.CREATED)
    .body(new JwtResponse(
        jwt,
        userDetails.getId(),
        userDetails.getUsername(),
        userDetails.getEmail(),
        List.of("ROLE_USER")
    ));
    }

    
    @PostMapping("/login")
    @Operation(summary = "User anmelden", description = "Ermöglicht einem User, sich mit Email und Passwort einzuloggen")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUserEmail(),
                            loginRequest.getPassword()));

            String jwt = jwtUtils.generateJwtToken(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            return ResponseEntity.ok(
                    new JwtResponse(
                            jwt,
                            userDetails.getId(),
                            userDetails.getUsername(),
                            userDetails.getEmail(),
                            List.of("ROLE_USER")));
                            
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid username/email or password."));
        }
    }


    @DeleteMapping("/deletemyAccount")
    @Operation(
    summary = "Eigenen Account löschen",
    description = "Ermöglicht es dem authentifizierten Benutzer, seinen eigenen Account dauerhaft zu löschen."
)
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal UserDetailsImpl user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Unauthorized."));
        }

        boolean deleted = userDetailsService.deleteById(user.getId());

        if (deleted) {
            return ResponseEntity.ok(new MessageResponse("Your account was deleted successfully."));


        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse("Account could not be deleted."));
    }


@GetMapping("/logged-in-username")
public ResponseEntity<String> getUsername(
        @AuthenticationPrincipal UserDetailsImpl user) {

    return ResponseEntity.ok(user.getUsername());
}
}
