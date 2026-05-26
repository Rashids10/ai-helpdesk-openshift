package com.rashid.ai_helpdesk.security.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rashid.ai_helpdesk.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*

Request →
JWT im Header →
AuthTokenFilter →
Token valid? →
User laden →
Authentication setzen →
Controller Zugriff erlauben


alles in allem TÜr steher für die API


*/
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthTokenFilter.class);
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

/**
 * Diese Methode bestimmt, ob der JWT Filter
 * für einen Request übersprungen werden soll.
 *
 * return true  -> Filter NICHT ausführen
 * return false -> Filter normal ausführen
 *
 * Dadurch können bestimmte öffentliche Endpoints
 * ohne JWT Token erreichbar bleiben.
 */
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {

    // Holt den aktuellen Request-Pfad
    // Beispiel: /rag/ask
    String path = request.getServletPath();

    // Öffentliche Endpoints überspringen
    return path.startsWith("/api/auth/")
            || path.equals("/rag/ask")
            || path.startsWith("/v3/api-docs/")
            || path.startsWith("/swagger-ui/")
            || path.equals("/swagger-ui.html")
            || path.equals("/error");
}



}
