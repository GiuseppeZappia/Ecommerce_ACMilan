package com.example.provamiavuota.repositories;

import com.example.provamiavuota.entities.DettaglioOrdine;
import com.example.provamiavuota.entities.Ordine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DettaglioOrdineRepository extends JpaRepository<DettaglioOrdine,Integer> {
    Page<DettaglioOrdine> findByOrdine(Ordine idOrdine, Pageable pageable);
}
