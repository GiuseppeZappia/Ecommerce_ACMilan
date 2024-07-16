package com.example.provamiavuota.configurations;

import com.example.provamiavuota.authentication.JwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationConverter jwtAuthConverter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.requestMatchers(
                antMatcher("/prodotti/elencoDisponibili/**"),
                antMatcher("/prodotti/percategoria/**"),
                antMatcher("/prodotti/perFasciaPrezzo/**"),
                antMatcher("/prodotti/perNome/{nomeProdotto}/**"),
                antMatcher("/prodotti/ricercaAvanzata/**"),
                antMatcher("/promozioni/elenco"),
                antMatcher("/utenti/**")
        ).permitAll().anyRequest().authenticated());
        //QUINDI HO SOPRA QUEGLI ENDPOINT ACCESSIBILI ANCHE SENZA NESSUNA AUTENTICAZIONE
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));
        http.sessionManagement(sess -> sess.sessionCreationPolicy(STATELESS));
        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedOrigin("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("OPTIONS");
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        source.registerCorsConfiguration("/**", configuration);
        return new CorsFilter(source);
    }


}
