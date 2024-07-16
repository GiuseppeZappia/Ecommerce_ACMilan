package com.example.provamiavuota.controllers;

import com.example.provamiavuota.entities.Carrello;
import com.example.provamiavuota.entities.DettaglioCarrello;
import com.example.provamiavuota.entities.Ordine;
import com.example.provamiavuota.services.CarrelloService;
import com.example.provamiavuota.supports.ResponseMessage;
import com.example.provamiavuota.supports.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/carrello")
public class CarrelloController {
    @Autowired
    private CarrelloService carrelloService;


    @PreAuthorize("hasRole('utente')")
    @GetMapping("{idUtente}")
    public ResponseEntity getAll(@PathVariable int idUtente,
                                 @RequestParam(value = "numPagina", defaultValue = "0") int numPagina,
                                 @RequestParam(value = "dimPagina", defaultValue = "20") int dimPagina,
                                 @RequestParam(value = "ordinamento", defaultValue = "quantita") String ordinamento
    ) {

        try {
            LinkedList<String> ordinamentoValido = new LinkedList<>();
            ordinamentoValido.addAll(Arrays.asList("quantita", "prezzo"));
            if (numPagina < 0 || dimPagina <= 0 || !ordinamentoValido.contains(ordinamento)) {
                return new ResponseEntity<>(new ResponseMessage("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI"), HttpStatus.BAD_REQUEST);
            }
            List<DettaglioCarrello> listaItem = carrelloService.mostraTutti(numPagina, dimPagina, ordinamento, idUtente);
            if (listaItem.isEmpty()) {
                return new ResponseEntity<>(new ResponseMessage("NESSUN RISULTATO O NUMERO DI PAGINA NON VALIDO"), HttpStatus.OK);
            }
            return new ResponseEntity<>(listaItem, HttpStatus.OK);
        } catch (UtenteNonEsistenteONonValido e) {
            return new ResponseEntity<>(new ResponseMessage("UTENTE NON ESISTENTE O NON VALIDO"), HttpStatus.OK);
        } catch (CarrelloNonValidoException e) {
            return new ResponseEntity<>(new ResponseMessage("CARRELLO NON VALIDO"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELLA RICHIESTA"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('utente')")
    //EVENTUALMENTE INSERISCO IN UN DTO CHE RICEVO NEL BODY?
    @PostMapping("/aggiungi/{idCarrello}/{idUtente}/{quantita}")
    public ResponseEntity aggiungiProdottoAcarrello(@PathVariable int idCarrello, @PathVariable int idUtente,@PathVariable int quantita) {
        try {
            if (idCarrello < 0 || idUtente < 0) {
                return new ResponseEntity<>(new ResponseMessage("CARRELLO O UTENTE NON VALIDO"), HttpStatus.BAD_REQUEST);
            }
            Carrello aggiornato = carrelloService.aggiungiProdotto(idCarrello, idUtente,quantita);
            return new ResponseEntity<>(aggiornato, HttpStatus.OK);
        } catch (CarrelloNonValidoException e) {
            return new ResponseEntity<>(new ResponseMessage("CARRELLO NON VALIDO"), HttpStatus.BAD_REQUEST);
        } catch (ProdottoNonValidoException e) {
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO NON VALIDO"), HttpStatus.BAD_REQUEST);
        }catch(QuantitaProdottoNonDisponibile e){
            return new ResponseEntity<>(new ResponseMessage("QUANTITA NON VALIDA"), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELLA RICHIESTA"), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('utente')")
    @PostMapping("/acquista/{idUtente}/{puntiUsati}")
    public ResponseEntity acquista(@PathVariable int idUtente,@PathVariable int puntiUsati) {
        try {
            if (idUtente < 0) {
                throw new UtenteNonEsistenteONonValido();
            }
            if (puntiUsati < 0) {
                throw new OrdineNonValido();
            }
            Ordine ordine = carrelloService.acquista(idUtente, puntiUsati);
            return new ResponseEntity<>(ordine, HttpStatus.OK);
        }catch (ProdottoNonValidoException e){
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO NON VALIDO"), HttpStatus.BAD_REQUEST);
        }catch(OrdineNonValido e){
            return new ResponseEntity<>(new ResponseMessage("ORDINE O PUNTI NON VALIDI"), HttpStatus.BAD_REQUEST);
        }catch(CarrelloNonValidoException e){
            return new ResponseEntity<>(new ResponseMessage("CARRELLO NON VALIDO"), HttpStatus.BAD_REQUEST);
        }catch (UtenteNonEsistenteONonValido e){
            return new ResponseEntity<>(new ResponseMessage("UTENTE NON VALIDO"), HttpStatus.BAD_REQUEST);
        } catch (QuantitaProdottoNonDisponibile e) {
            return new ResponseEntity<>(new ResponseMessage("QUANTITA NON VALIDA"), HttpStatus.BAD_REQUEST);
        } catch (MinimoPuntiRichiestoNonSoddisfatto e) {
            return new ResponseEntity<>(new ResponseMessage("MINIMO PUNTI NON SODDISFATTO"), HttpStatus.BAD_REQUEST);
        } catch (ProdottoNonDisponibileAlMomento e) {
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO NON DISPONIBILE"), HttpStatus.BAD_REQUEST);
        } catch (PuntiFedeltaNonDisponibili e) {
            return new ResponseEntity<>(new ResponseMessage("PUNTI NON DISPONIBILI"), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELL'ACQUISTO"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}




