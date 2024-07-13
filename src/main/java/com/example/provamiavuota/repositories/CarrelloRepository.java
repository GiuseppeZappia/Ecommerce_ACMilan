package com.example.provamiavuota.repositories;

import com.example.provamiavuota.entities.Carrello;
import com.example.provamiavuota.entities.Ordine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarrelloRepository extends JpaRepository<Carrello,Integer> {
}
