package com.example.provamiavuota.repositories;

import com.example.provamiavuota.entities.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtenteRepository extends JpaRepository<Utente, Integer> {
    boolean existsByNomeIgnoreCaseAndCognomeIgnoreCaseAndEmailIgnoreCaseAndUsernameIgnoreCaseAndPasswordAndPuntifedelta(String nome,String cognome,String email,String username,String pass,int punti);



}
