package com.example.provamiavuota.repositories;

import com.example.provamiavuota.entities.Promozione;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromozioneRepository extends JpaRepository<Promozione, Integer> {

    Page<Promozione> findByAttiva(int attiva, Pageable pageable);

    boolean existsByNomeAndAttiva(String nome,int attiva);
}
