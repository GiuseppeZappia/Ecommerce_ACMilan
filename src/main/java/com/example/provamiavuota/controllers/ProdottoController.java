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

        LinkedList<String> ordinamentoValido=new LinkedList<>();
        ordinamentoValido.addAll(Arrays.asList("nome","categoria","descrizione","prezzo","quantita"));
        if(numPagina<0 || dimPagina<=0 || !ordinamentoValido.contains(ordinamento)) {
            return new ResponseEntity<>(new ResponseMessage("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI"), HttpStatus.BAD_REQUEST);
        }

       List<Prodotto> listaProdotti= prodottoService.elencoProdotti(numPagina, dimPagina, ordinamento);
        if (listaProdotti.isEmpty()) {
            return new ResponseEntity<>(new ResponseMessage("NESSUN RISULTATO O NUMERO DI PAGINA NON VALIDO"), HttpStatus.OK);
        }
        return new ResponseEntity<>(listaProdotti, HttpStatus.OK);
    }

    @GetMapping("/percategoria")
//in questo modo a questo URL http://localhost:8080/prodotti/percategoria?categoria=maglie mi filtra solo maglie per esempio
    public ResponseEntity getProdottiByCategoria(@RequestParam(required = false) String categoria,
                                                 @RequestParam(value = "numPagina", defaultValue = "0") int numPagina,
                                                 @RequestParam(value = "dimPagina", defaultValue = "20") int dimPagina,
                                                 @RequestParam(value = "ordinamento", defaultValue = "prezzo") String ordinamento) {

        LinkedList<String> ordinamentoValido=new LinkedList<>();
        ordinamentoValido.addAll(Arrays.asList("nome","categoria","descrizione","prezzo","quantita"));
        if(numPagina<0 || dimPagina<=0 || !ordinamentoValido.contains(ordinamento)) {
            return new ResponseEntity<>(new ResponseMessage("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI"), HttpStatus.BAD_REQUEST);
        }
        List<Prodotto> listaProdotti = prodottoService.elencoProdottiPerCategoria(categoria, numPagina, dimPagina, ordinamento);
        if (listaProdotti.isEmpty()) {
            return new ResponseEntity<>(new ResponseMessage("NESSUN RISULTATO O NUMERO DI PAGINA NON VALIDO"), HttpStatus.OK);
        }
        return new ResponseEntity<>(listaProdotti, HttpStatus.OK);
    }

    @GetMapping("/perFasciaPrezzo")
    public ResponseEntity getProdottiByFasciaPrezzo(@RequestParam(required = false, defaultValue = "0.1") double minPrezzo,
                                                    @RequestParam(required = false, defaultValue = "" + Double.MAX_VALUE) double maxPrezzo,
                                                    @RequestParam(value = "numPagina", defaultValue = "0") int numPagina,
                                                    @RequestParam(value = "dimPagina", defaultValue = "20") int dimPagina) {
       return this.ricercaAvanzata(minPrezzo,maxPrezzo,null,null,0,0,20,"prezzo");

    }

    @GetMapping("/perNome/{nomeProdotto}")
    //AGGIUNGO UNIQUE ANCHE SUL SINGOLO NOME E NON ANCHE CON CATEGORIA? PERCHE ALLA FINE NON AVRO DUE MAGLIE CON IDENTICO NOME
    public ResponseEntity getProdottoByNomeProdotto(@PathVariable String nomeProdotto) {
        try {
            Prodotto prodotto = prodottoService.trovaProdottoByNome(nomeProdotto);
            if (prodotto == null)//CAMBIA QUI E METTI ECCEZONE MAGARI
                throw new ProdottoNonValidoException();
            return new ResponseEntity<>(prodotto, HttpStatus.OK);
        }catch (ProdottoNonValidoException p){
            return new ResponseEntity<>(new ResponseMessage("NESSUN PRODOTTO CON QUESTO NOME"), HttpStatus.OK);
        }
    }

    @GetMapping("/ricercaAvanzata")
    public ResponseEntity ricercaAvanzata(  @RequestParam(required = false, defaultValue = "0.1") double prezzoMin,
                                            @RequestParam(required = false, defaultValue = "" + Double.MAX_VALUE) double prezzoMax,
                                            @RequestParam(required = false) String nome,
                                            @RequestParam(required = false) String categoria,
                                            @RequestParam(required = false, defaultValue = "0") int quantita,
                                            @RequestParam(value = "numPagina", defaultValue = "0") int numPagina,
                                            @RequestParam(value = "dimPagina", defaultValue = "20") int dimPagina,
                                            @RequestParam(required = false, defaultValue = "prezzo") String ordinamento) {
        try {
            LinkedList<String> ordinamentoValido=new LinkedList<>();
            ordinamentoValido.addAll(Arrays.asList("nome","categoria","descrizione","prezzo","quantita"));
            if(numPagina<0 || dimPagina<=0 || !ordinamentoValido.contains(ordinamento)) {
                return new ResponseEntity<>(new ResponseMessage("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI"), HttpStatus.BAD_REQUEST);
            }
            List<Prodotto> listaProdottiFiltrati = prodottoService.ricercaAvanzata(numPagina, dimPagina, prezzoMin, prezzoMax, nome, categoria, quantita, ordinamento);
            if (listaProdottiFiltrati.isEmpty())
                return new ResponseEntity<>(new ResponseMessage("NESSUN RISULTATO"), HttpStatus.OK);
            return new ResponseEntity<>(listaProdottiFiltrati, HttpStatus.OK);
        } catch (FasciaDiPrezzoNonValidaException e) {
            return new ResponseEntity<>(new ResponseMessage("FASCIA DI PREZZO NON VALIDA"), HttpStatus.BAD_REQUEST);
        }

    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping
    public ResponseEntity salvaProdotto(@RequestBody @Valid Prodotto prodotto) {
        try{
            prodottoService.salvaProdotto(prodotto);//RESTITUISCO PRODOTTO?????
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO SALVATO CON SUCCESSO"),HttpStatus.OK);
        }catch (ProdottoGiaEsistenteException e){
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO GIA' ESISTENTE"), HttpStatus.BAD_REQUEST);
        }catch (ProdottoNonValidoException e){
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO NON VALIDO "), HttpStatus.BAD_REQUEST);
        }catch(FasciaDiPrezzoNonValidaException e){
            return new ResponseEntity<>(new ResponseMessage("FASCIA DI PREZZO NON VALIDA"), HttpStatus.BAD_REQUEST);
        }catch (CategoriaNonValidaException e){
            return new ResponseEntity<>(new ResponseMessage("CATEGORIA NON VALIDA"), HttpStatus.BAD_REQUEST);
        }catch(Exception e){
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELL'AGGIUNTA'"), HttpStatus.BAD_REQUEST);
        }
    }


    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("{idProdotto}")
    public ResponseEntity rimuoviProdotto(@PathVariable int  idProdotto) {
        try {
            prodottoService.rimuoviProdotto(idProdotto);
            return new ResponseEntity<>(new ResponseMessage("RIMOZIONE ANDATA A BUON FINE"),HttpStatus.OK );
        }catch(ProdottoGiaEliminatoException e){
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO GIA ELIMINATO"), HttpStatus.BAD_REQUEST);
        }
        catch(ProdottoNonPresenteNelDbExceptions e){
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO NON ESISTENTE"), HttpStatus.BAD_REQUEST);
        }catch(Exception e){
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELLA RIMOZIONE"), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('utente')")
    @GetMapping("/preferiti")
    public ResponseEntity getPreferiti(@RequestParam(value = "numPagina", defaultValue = "0") int numPagina,
                                       @RequestParam(value = "dimPagina", defaultValue = "20") int dimPagina,
                                       @RequestParam(value = "ordinamento", defaultValue = "prezzo") String ordinamento){
        LinkedList<String> ordinamentoValido=new LinkedList<>();
        ordinamentoValido.addAll(Arrays.asList("nome","categoria","descrizione","prezzo","quantita"));
        if(numPagina<0 || dimPagina<=0 || !ordinamentoValido.contains(ordinamento)) {
            return new ResponseEntity<>(new ResponseMessage("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI"), HttpStatus.BAD_REQUEST);
        }

        List<Prodotto> listaProdotti= prodottoService.getPreferiti(numPagina, dimPagina, ordinamento);
        if (listaProdotti.isEmpty()) {
            return new ResponseEntity<>(new ResponseMessage("LISTA PREFERITI VUOTA O NUMERO DI PAGINA NON VALIDO"), HttpStatus.OK);
        }
        return new ResponseEntity<>(listaProdotti, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('utente')")
    @PostMapping("{idProdotto}")
    public ResponseEntity aggiungiAiPreferiti(@PathVariable int  idProdotto) {
        try {
            String risultato=prodottoService.aggiuntaAiPreferiti(idProdotto);
            if (risultato.equals("aggiunto"))
                return new ResponseEntity<>(new ResponseMessage("PRODOTTO AGGIUNTO AI PREFERITI"), HttpStatus.OK);
            return new ResponseEntity<>(new ResponseMessage("RIMOSSO DAI PREFERITI"), HttpStatus.BAD_REQUEST);
        }catch(ProdottoNonPresenteNelDbExceptions e) {
            return new ResponseEntity<>(new ResponseMessage("PRODOTTTO NON ESISTENTE "), HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELL'OPERAZIONE "), HttpStatus.BAD_REQUEST);
        }
    }

}

