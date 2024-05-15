package com.example.provamiavuota.repositories;

import com.example.provamiavuota.entities.Prodotto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ProdottoRepository extends JpaRepository<Prodotto,Integer> {

    Prodotto findByNomeIgnoreCaseAndNascosto(String nome,int nascosto);
    List<Prodotto> findByNomeContainingIgnoreCase(String nome);
    Page<Prodotto> findByCategoriaContainingIgnoreCaseAndNascosto(String categoria, Pageable paging,int nascosto);
    List<Prodotto> findByPrezzo(Double prezzo);
    List<Prodotto> findAllByOrderByPrezzoAsc();
    List<Prodotto> findAllByOrderByPrezzoDesc();
    List<Prodotto> findByPrezzoGreaterThan(Double prezzo);
    List<Prodotto> findByPrezzoLessThan(Double prezzo);
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

    int countByCategoriaIgnoreCase(String categoria);
    int countByPrezzo(Double prezzo);
    int countByCategoriaIgnoreCaseAndPrezzo(String categoria,Double prezzo);
    Page<Prodotto> findAllByNascosto(Pageable paging ,int nascosto);
    int countByNascosto(int nascosto);

    Page<Prodotto> findAllByPreferito(Pageable paging, int i);
}
