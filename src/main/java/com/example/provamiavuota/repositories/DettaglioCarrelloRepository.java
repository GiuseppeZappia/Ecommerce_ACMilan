package com.example.provamiavuota.repositories;

import com.example.provamiavuota.entities.DettaglioCarrello;
import com.example.provamiavuota.entities.Ordine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DettaglioCarrelloRepository extends JpaRepository<DettaglioCarrello,Integer> {
    Page<DettaglioCarrello> findByCarrello_Id(int idCarrello, Pageable pageable);

    boolean existsByCarrello_IdAndProdotto_Id(int idCarrello, int prodottoId);
    DettaglioCarrello findByCarrello_IdAndProdotto_Id(int idCarrello, int prodottoId);

}
