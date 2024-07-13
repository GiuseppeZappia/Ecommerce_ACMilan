package com.example.provamiavuota.services;

import com.example.provamiavuota.entities.Carrello;
import com.example.provamiavuota.entities.Ordine;
import com.example.provamiavuota.repositories.CarrelloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

@Service
public class CarrelloService {
    @Autowired
    private CarrelloRepository carrelloRepository;


    @Transactional(readOnly = true)
    public List<Carrello> visualizza(int numPagina, int dimPagina, String ordinamento) {
        Sort.Direction tipoOrdinamento = Sort.Direction.DESC;
        Pageable paging = PageRequest.of(numPagina, dimPagina, Sort.by(tipoOrdinamento, ordinamento));
        Page<Carrello> risultatiPagine = carrelloRepository.findAll(paging);
        if (risultatiPagine.hasContent()) {
            return risultatiPagine.getContent();
        } else {
            return new LinkedList<>();
        }
    }

    




}
