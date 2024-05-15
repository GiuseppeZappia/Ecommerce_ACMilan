package com.example.provamiavuota.repositories;

import com.example.provamiavuota.entities.Ordine;
import com.example.provamiavuota.entities.Utente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

public interface OrdineRepository extends JpaRepository<Ordine,Integer> {

    @Query( "SELECT o "+
            "FROM Ordine o "+
            "WHERE o.data  > ?2 AND " +
            " o.data < ?3 AND o.utente = ?1 ")
    Page<Ordine> ricercaOrdiniInPeriodo(Utente u, Date dataInizio, Date dataFine, Pageable paging);

    Page<Ordine> findByUtente(Utente utente, Pageable paging);
}
