package com.example.provamiavuota.controllers;

import com.example.provamiavuota.dto.CarrelloDto;
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

import javax.validation.constraints.NotNull;
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
            ordinamentoValido.addAll(Arrays.asList("quantita", "prezzounitario"));
            if (numPagina < 0 || dimPagina <= 0 || !ordinamentoValido.contains(ordinamento)) {
                System.out.println("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI");
                return new ResponseEntity<>(new ResponseMessage("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI"), HttpStatus.BAD_REQUEST);
            }
            List<DettaglioCarrello> listaItem = carrelloService.mostraTutti(numPagina, dimPagina, ordinamento, idUtente);
            if (listaItem.isEmpty()) {
                System.out.println("NESSUN RISULTATO O NUMERO DI PAGINA NON VALIDO");
                return new ResponseEntity<>(new ResponseMessage("NESSUN RISULTATO O NUMERO DI PAGINA NON VALIDO"), HttpStatus.OK);
            }
            return new ResponseEntity<>(listaItem, HttpStatus.OK);
        } catch (UtenteNonEsistenteONonValido e) {
            System.out.println("UTENTE NON ESISTENTE O NON VALIDO");
            return new ResponseEntity<>(new ResponseMessage("UTENTE NON ESISTENTE O NON VALIDO"), HttpStatus.OK);
        } catch (CarrelloNonValidoException e) {
            System.out.println("CARRELLO NON VALIDO");
            return new ResponseEntity<>(new ResponseMessage("CARRELLO NON VALIDO"), HttpStatus.OK);
        } catch (TentativoNonAutorizzato e) {
            System.out.println("TENTATIVO NON AUTORIZZATO");
            return new ResponseEntity<>(new ResponseMessage("TENTATIVO NON AUTORIZZATO"), HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("ERRORE NELLA RICHIESTA");
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELLA RICHIESTA"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('utente')")
    //EVENTUALMENTE INSERISCO IN UN DTO CHE RICEVO NEL BODY COME FACCIO PER ACQUISTO
    @PostMapping("/aggiungi/{idUtente}/{idProdotto}/{quantita}")
    public ResponseEntity aggiungiProdottoAcarrello(@PathVariable int idUtente, @PathVariable int idProdotto, @PathVariable int quantita) {
        try {
            if (idUtente < 0 || idProdotto < 0) {
                return new ResponseEntity<>(new ResponseMessage("CARRELLO O UTENTE NON VALIDO"), HttpStatus.BAD_REQUEST);
            }
            Carrello aggiornato = carrelloService.aggiungiProdotto(idUtente, idProdotto, quantita);
            return new ResponseEntity<>(aggiornato, HttpStatus.OK);
        } catch (CarrelloNonValidoException e) {
            System.out.println("CARRELLO NON VALIDO");
            return new ResponseEntity<>(new ResponseMessage("CARRELLO NON VALIDO"), HttpStatus.BAD_REQUEST);
        } catch (ProdottoNonValidoException e) {
            System.out.println("PRODOTTO NON VALIDO");
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO NON VALIDO"), HttpStatus.BAD_REQUEST);
        } catch (QuantitaProdottoNonDisponibile e) {
            System.out.println("QUANTITA' NON VALIDA");
            return new ResponseEntity<>(new ResponseMessage("QUANTITA NON VALIDA"), HttpStatus.BAD_REQUEST);
        } catch (TentativoNonAutorizzato e) {
            System.out.println("TENTATIVO NON AUTORIZZATO");
            return new ResponseEntity<>(new ResponseMessage("TENTATIVO NON AUTORIZZATO"), HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("ERRORE NELLA RICHIESTA");
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELLA RICHIESTA"), HttpStatus.BAD_REQUEST);
        }
    }


    @PreAuthorize("hasRole('utente')")
    @PostMapping("/acquista/{puntiUsati}")
    public ResponseEntity acquista(@RequestBody @NotNull CarrelloDto carrelloDto, @PathVariable int puntiUsati) {
        try {
            if (puntiUsati < 0) {
                throw new OrdineNonValido();
            }
            Ordine ordine = carrelloService.acquista(carrelloDto, puntiUsati);
            return new ResponseEntity<>(ordine, HttpStatus.OK);
        }catch (QuantitaProdottoNonDisponibile e){
            System.out.println("QUANTITA NON DISPONIBILE");
            return new ResponseEntity<>(new ResponseMessage("QUANTITA NON DISPONIBILE"), HttpStatus.BAD_REQUEST);
        }
        catch (OrdineNonValido e){
            System.out.println("ORDINE NON VALIDO");
            return new ResponseEntity<>(new ResponseMessage("ORDINE NON VALIDO"), HttpStatus.BAD_REQUEST);
        }catch (MinimoPuntiRichiestoNonSoddisfatto e){
            System.out.println("MINIMO PUNTI RICHIESTO NON SODDISFATTO");
            return new ResponseEntity(new ResponseMessage("MINIMO PUNTI RICHIESTO NON SODDISFATTO"), HttpStatus.BAD_REQUEST);
        }catch (PuntiFedeltaNonDisponibili e){
            System.out.println("PUNTI FEDELTA NON DISPONIBILI");
            return new ResponseEntity<>(new ResponseMessage("PUNTI FEDELTA NON DISPONIBILI"), HttpStatus.BAD_REQUEST);
        }catch (UtenteNonEsistenteONonValido e){
            System.out.println("UTENTE NON ESISTENTE O NON VALIDO");
            return new ResponseEntity(new ResponseMessage("UTENTE NON ESISTENTE O NON VALIDO"),HttpStatus.BAD_REQUEST);
        }catch (ProdottoNonValidoException e){
            System.out.println("PRODOTTO NON VALIDO");
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO NON VALIDO"), HttpStatus.BAD_REQUEST);
        }catch (CarrelloNonValidoException e){
            System.out.println("CARRELLO NON VALIDO");
            return new ResponseEntity<>(new ResponseMessage("CARRELLO NON VALIDO"), HttpStatus.BAD_REQUEST);
        }catch (TentativoNonAutorizzato e){
            System.out.println("TENTATIVO NON AUTORIZZATO");
            return new ResponseEntity(new ResponseMessage("TENTATIVO NON AUTORIZZATO"),HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            System.out.println("ERRORE NELL'ACQUISTO");
            return new ResponseEntity(new ResponseMessage("ERRORE NELL'ACQUISTO"), HttpStatus.BAD_REQUEST);
        }
    }

}




