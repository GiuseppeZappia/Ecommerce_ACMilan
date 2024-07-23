package com.example.provamiavuota.repositories;

import com.example.provamiavuota.entities.Carrello;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface CarrelloRepository extends JpaRepository<Carrello,Integer> {

    boolean existsByIdAndAndAttivo(int id,int attivo);

    @Query( "SELECT c "+
            "FROM Carrello c "+
            "WHERE c.utente.id = ?1 " +
            "AND c.attivo = ?2 ")
    Carrello findActiveCarrelloByUtenteId( int utenteId,int attivo);
}
