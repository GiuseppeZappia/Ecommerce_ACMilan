package com.example.provamiavuota.authentication;


import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class Utils {

    public static Integer getIdUtente() {
        JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (authenticationToken == null) {
            // Nessuna autenticazione presente
            return null;
        }
        Jwt jwt = (Jwt) authenticationToken.getCredentials();
        Long idUtenteLong = (Long) jwt.getClaims().get("idUtente");
        Integer idUtente = idUtenteLong.intValue();
        return idUtente;
    }


}
