package com.example.provamiavuota.repositories;

import com.example.provamiavuota.entities.Prodotto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProdottoRepository extends JpaRepository<Prodotto,Integer> {

    Prodotto findByNomeIgnoreCaseAndNascosto(String nome,int nascosto);
    Page<Prodotto> findByCategoriaContainingIgnoreCaseAndNascosto(String categoria, Pageable paging,int nascosto);

    @Query( "SELECT p "+
            "FROM Prodotto p "+
            "WHERE (p.prezzo >= ?1 OR ?1 IS NULL) AND " +
            " (p.prezzo <=?2 OR ?2 IS NULL) AND  p.nascosto = 0")
    Page<Prodotto> ricercaIntervalloPrezzo(Double sogliaMin,Double sogliaMax,Pageable paging);//non ho usato query native sql, ma JPQL perche se un giorno cambiassi db almeno questa porzione di codice continuerebbe a funzionare

    @Query( "SELECT p "+
            "FROM Prodotto p "+
            "WHERE (p.prezzo >= ?1 OR ?1 IS NULL) AND " +
            "      (p.prezzo <=?2 OR ?2 IS NULL) AND " +
            "      (p.nome LIKE ?3 OR ?3 IS NULL) AND " +
            "      (p.categoria LIKE ?4 OR ?4 IS NULL) AND " +
            "      (p.quantita >= ?5 OR ?5 IS NULL) AND p.nascosto = 0 "
    )
    Page<Prodotto> ricercaAvanzata(Double sogliaMin,Double sogliaMax,String nome,String categoria,int quantita,Pageable paging);
    boolean existsByNomeIgnoreCaseAndNascosto(String nome,int nascosto);

    @Query( "SELECT p "+
            "FROM Prodotto p "+
            "WHERE (p.prezzo >= ?1 OR ?1 IS NULL) AND " +
            "      (p.prezzo <=?2 OR ?2 IS NULL) AND " +
            "      (p.nome LIKE ?3 OR ?3 IS NULL) AND " +
            "      (p.categoria LIKE ?4 OR ?4 IS NULL) AND " +
            "      (p.quantita >= ?5 OR ?5 IS NULL) AND p.nascosto = 0 "
    )
    Page<Prodotto> findAllByNascosto(Pageable paging ,int nascosto);
    Page<Prodotto> findByQuantitaGreaterThanAndNascosto(Pageable paging,int quantita, int nascosto);

}
