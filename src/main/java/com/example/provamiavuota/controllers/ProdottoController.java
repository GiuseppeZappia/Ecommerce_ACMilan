package com.example.provamiavuota.controllers;

import com.example.provamiavuota.entities.Prodotto;
import com.example.provamiavuota.services.ProdottoService;
import com.example.provamiavuota.supports.ResponseMessage;
import com.example.provamiavuota.supports.exceptions.*;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/prodotti")
public class ProdottoController {
    @Autowired
    private ProdottoService prodottoService;

    @GetMapping("/elencoDisponibili")
    public ResponseEntity getAll(@RequestParam(value = "numPagina", defaultValue = "0") int numPagina,
                                 @RequestParam(value = "dimPagina", defaultValue = "20") int dimPagina,
                                 @RequestParam(value = "ordinamento", defaultValue = "prezzo") String ordinamento) {
        try {
            LinkedList<String> ordinamentoValido = new LinkedList<>();
            ordinamentoValido.addAll(Arrays.asList("nome", "categoria", "descrizione", "prezzo", "quantita"));
            if (numPagina < 0 || dimPagina <= 0 || !ordinamentoValido.contains(ordinamento)) {
                System.out.println("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI");
                return new ResponseEntity<>(new ResponseMessage("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI"), HttpStatus.BAD_REQUEST);
            }
            List<Prodotto> listaProdotti = prodottoService.elencoProdotti(numPagina, dimPagina, ordinamento);
            if (listaProdotti.isEmpty()) {
                System.out.println("NESSUN RISULTATO O NUMERO DI PAGINA NON VALIDO");
                return new ResponseEntity<>(new ResponseMessage("NESSUN RISULTATO O NUMERO DI PAGINA NON VALIDO"), HttpStatus.OK);
            }
            return new ResponseEntity<>(listaProdotti, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("ERRORE NELLA RICERCA");
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELLA RICERCA"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/percategoria")
    public ResponseEntity getProdottiByCategoria(@RequestParam(required = false) String categoria,
                                                 @RequestParam(value = "numPagina", defaultValue = "0") int numPagina,
                                                 @RequestParam(value = "dimPagina", defaultValue = "20") int dimPagina,
                                                 @RequestParam(value = "ordinamento", defaultValue = "prezzo") String ordinamento) {

        LinkedList<String> ordinamentoValido = new LinkedList<>();
        ordinamentoValido.addAll(Arrays.asList("nome", "categoria", "descrizione", "prezzo", "quantita"));
        if (numPagina < 0 || dimPagina <= 0 || !ordinamentoValido.contains(ordinamento)) {
            System.out.println("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI");
            return new ResponseEntity<>(new ResponseMessage("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI"), HttpStatus.BAD_REQUEST);
        }
        List<Prodotto> listaProdotti = prodottoService.elencoProdottiPerCategoria(categoria, numPagina, dimPagina, ordinamento);
        if (listaProdotti.isEmpty()) {
            System.out.println("NESSUN RISULTATO O NUMERO DI PAGINA NON VALIDO");
            return new ResponseEntity<>(new ResponseMessage("NESSUN RISULTATO O NUMERO DI PAGINA NON VALIDO"), HttpStatus.OK);
        }
        return new ResponseEntity<>(listaProdotti, HttpStatus.OK);
    }

    @GetMapping("/perFasciaPrezzo")
    public ResponseEntity getProdottiByFasciaPrezzo(@RequestParam(required = false, defaultValue = "0.1") double minPrezzo,
                                                    @RequestParam(required = false, defaultValue = "" + Double.MAX_VALUE) double maxPrezzo,
                                                    @RequestParam(value = "numPagina", defaultValue = "0") int numPagina,
                                                    @RequestParam(value = "dimPagina", defaultValue = "20") int dimPagina) {

        //CHIAMO METODO RICERCA AVANZATA CHE FARA' TUTTI I CONTROLLI
        return this.ricercaAvanzata(minPrezzo, maxPrezzo, null, null, 0, numPagina, dimPagina, "prezzo");
    }


    @GetMapping("/perNome/{nomeProdotto}")
    public ResponseEntity getProdottoByNomeProdotto(@PathVariable String nomeProdotto) {
        try {
            Prodotto prodotto = prodottoService.trovaProdottoByNome(nomeProdotto);
            if (prodotto == null) {
                System.out.println("NESSUN PRODOTTO CON QUESTO NOME");
                return new ResponseEntity<>(new ResponseMessage("NESSUN PRODOTTO CON QUESTO NOME"), HttpStatus.OK);
            }
            return new ResponseEntity<>(prodotto, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("ERRORE NELLA RICERCA");
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELLA RICERCA"), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping
    public ResponseEntity salvaProdotto(@RequestBody @Valid Prodotto prodotto) {
        try {
            prodottoService.salvaProdotto(prodotto);
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO SALVATO CON SUCCESSO"), HttpStatus.OK);
        } catch (ProdottoGiaEsistenteException e) {
            System.out.println("PRODOTTO GIA' ESISTENTE");
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO GIA' ESISTENTE"), HttpStatus.BAD_REQUEST);
        } catch (ProdottoNonValidoException e) {
            System.out.println("PRODOTTO NON VALIDO");
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO NON VALIDO "), HttpStatus.BAD_REQUEST);
        } catch (FasciaDiPrezzoNonValidaException e) {
            System.out.println("FASCIA DI PREZZO NON VALIDA");
            return new ResponseEntity<>(new ResponseMessage("FASCIA DI PREZZO NON VALIDA"), HttpStatus.BAD_REQUEST);
        } catch (CategoriaNonValidaException e) {
            System.out.println("CATEGORIA NON VALIDA");
            return new ResponseEntity<>(new ResponseMessage("CATEGORIA NON VALIDA"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.out.println("ERRORE NELL'AGGIUNTA");
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELL'AGGIUNTA'"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/ricercaAvanzata")
    public ResponseEntity ricercaAvanzata(@RequestParam(required = false, defaultValue = "0.1") double prezzoMin,
                                          @RequestParam(required = false, defaultValue = "" + Double.MAX_VALUE) double prezzoMax,
                                          @RequestParam(required = false) String nome,
                                          @RequestParam(required = false) String categoria,
                                          @RequestParam(required = false, defaultValue = "0") int quantita,
                                          @RequestParam(value = "numPagina", defaultValue = "0") int numPagina,
                                          @RequestParam(value = "dimPagina", defaultValue = "20") int dimPagina,
                                          @RequestParam(required = false, defaultValue = "prezzo") String ordinamento) {
        try {
            LinkedList<String> ordinamentoValido = new LinkedList<>();
            ordinamentoValido.addAll(Arrays.asList("nome", "categoria", "descrizione", "prezzo", "quantita"));
            if (numPagina < 0 || dimPagina <= 0 || !ordinamentoValido.contains(ordinamento)) {
                System.out.println("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI");
                return new ResponseEntity<>(new ResponseMessage("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI"), HttpStatus.BAD_REQUEST);
            }
            List<Prodotto> listaProdottiFiltrati = prodottoService.ricercaAvanzata(numPagina, dimPagina, prezzoMin, prezzoMax, nome, categoria, quantita, ordinamento);
            if (listaProdottiFiltrati.isEmpty()){
                System.out.println("NESSUN RISULTATO");
                return new ResponseEntity<>(new ResponseMessage("NESSUN RISULTATO"), HttpStatus.OK);
            }
            return new ResponseEntity<>(listaProdottiFiltrati, HttpStatus.OK);
        } catch (FasciaDiPrezzoNonValidaException e) {
            System.out.println("FASCIA DI PREZZO NON VALIDA");
            return new ResponseEntity<>(new ResponseMessage("FASCIA DI PREZZO NON VALIDA"), HttpStatus.BAD_REQUEST);
        }

    }


    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("{idProdotto}")
    public ResponseEntity rimuoviProdotto(@PathVariable int idProdotto) {
        try {
            prodottoService.rimuoviProdotto(idProdotto);
            return new ResponseEntity<>(new ResponseMessage("RIMOZIONE ANDATA A BUON FINE"), HttpStatus.OK);
        } catch (ProdottoGiaEliminatoException e) {
            System.out.println("PRODOTTO GIA ELIMINATO");
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO GIA ELIMINATO"), HttpStatus.BAD_REQUEST);
        } catch (ProdottoNonPresenteNelDbExceptions e) {
            System.out.println("PRODOTTO NON ESISTENTE");
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO NON ESISTENTE"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.out.println("ERRORE NELLA RIMOZIONE");
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELLA RIMOZIONE"), HttpStatus.BAD_REQUEST);
        }
    }

}

