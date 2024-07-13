package com.example.provamiavuota.repositories;

import com.example.provamiavuota.entities.ProdottiPromo;
import com.example.provamiavuota.entities.Prodotto;
import com.example.provamiavuota.entities.Promozione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProdottiPromoRepository extends JpaRepository<ProdottiPromo,Integer> {

    @Query( "SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END "+
            "FROM ProdottiPromo p "+
            "WHERE p.prodotto = ?1 AND p.promozione.attiva = 1 ")
    boolean existsByProdottoAndAttiva(Prodotto p) ;//verifico se la promo è attiva, l'admin stabilisce quando una promo finisce mettendo attiva a false?

    @Query( "SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END "+
            "FROM ProdottiPromo p "+
            "WHERE p.prodotto = ?1 AND p.promozione.id = ?2 AND p.promozione.attiva = 1 ")
    boolean existsByProdottoAndPromo(Prodotto p,int idPromo) ;//verifico se la promo è attiva, l'admin stabilisce quando una promo finisce mettendo attiva a false?


    ProdottiPromo findByProdotto(Prodotto p);

    @Query( "SELECT p "+
            "FROM ProdottiPromo p "+
            "WHERE p.prodotto = ?1 " +
            "ORDER BY p.sconto DESC ")//in questo modo se dovessero esserci più promozioni prendo la più conveniente per l'utente
    ProdottiPromo findPromozioneWithMaxScontoByProdotto(Prodotto p);
}
