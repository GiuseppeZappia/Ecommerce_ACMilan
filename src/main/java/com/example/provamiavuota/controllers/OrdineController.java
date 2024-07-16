package com.example.provamiavuota.controllers;

import com.example.provamiavuota.entities.DettaglioOrdine;
import com.example.provamiavuota.entities.Ordine;
import com.example.provamiavuota.entities.Utente;
import com.example.provamiavuota.services.OrdineService;
import com.example.provamiavuota.supports.ResponseMessage;
import com.example.provamiavuota.supports.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/ordini")
public class OrdineController {

    @Autowired
    private OrdineService ordineService;

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/elencoOrdini")
    public ResponseEntity getAll(@RequestParam(value = "numPagina", defaultValue = "0") int numPagina,
                                 @RequestParam(value = "dimPagina", defaultValue = "20") int dimPagina,
                                 @RequestParam(value = "ordinamento", defaultValue = "data") String ordinamento) {
        LinkedList<String> ordinamentoValido=new LinkedList<>();
        ordinamentoValido.addAll(Arrays.asList("data","puntiusati","utente","totale"));
        if(numPagina<0 || dimPagina<=0 || !ordinamentoValido.contains(ordinamento)) {
            return new ResponseEntity<>(new ResponseMessage("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI"), HttpStatus.BAD_REQUEST);
        }
        List<Ordine> listaOrdini = ordineService.mostraTutti(numPagina, dimPagina, ordinamento);
        if (listaOrdini.isEmpty()) {
            return new ResponseEntity<>(new ResponseMessage("NESSUN RISULTATO O NUMERO DI PAGINA NON VALIDO"), HttpStatus.OK);
        }
        return new ResponseEntity<>(listaOrdini, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping("/inserimento")
    public ResponseEntity inserimento(@RequestBody @Valid Ordine ordine) {
        try {
            Date dataOdierna = new Date();
            //qua magari vedere se un ordine uguale a zero si puo' fare
            //controllare bene quando ammettere data ordine
            if (ordine.getData() == null || ordine.getTotale() < 0 || ordine.getData().after(dataOdierna)) {//se inserisco non in data odierna
                throw new OrdineNonValido();
            }
            Ordine ordineRestituito = ordineService.salvaOrdine(ordine);
            return new ResponseEntity<>(ordineRestituito, HttpStatus.OK);
        } catch (UtenteNonEsistenteONonValido e) {
            return new ResponseEntity<>(new ResponseMessage("UTENTE NON ESISTENTE"), HttpStatus.BAD_REQUEST);
        } catch (OrdineNonValido o) {
            return new ResponseEntity<>(new ResponseMessage("ORDINE NON VALIDO"), HttpStatus.BAD_REQUEST);
        } catch (PuntiFedeltaNonDisponibili e) {
            return new ResponseEntity<>(new ResponseMessage("PUNTI FEDELTA' NON DISPONIBII"), HttpStatus.BAD_REQUEST);
        } catch (MinimoPuntiRichiestoNonSoddisfatto e) {
            return new ResponseEntity<>(new ResponseMessage("MINIMA QUANTITA DI PUNTI FEDELTA DA USARE NON SODDISFATTA"), HttpStatus.BAD_REQUEST);
        } catch (QuantitaProdottoNonDisponibile e) {
            return new ResponseEntity<>(new ResponseMessage("QUANTITA PRODOTTO NON DISPONIBILE"), HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELL'AGGIUNTA'"), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('utente')")
    @GetMapping("/elencoOrdini/{utente}/{DataInizio}/{DataFine}")
    public ResponseEntity getOrdiniNelPeriodo(@Valid @PathVariable("utente") Utente u,
                                              @PathVariable("DataInizio") @DateTimeFormat(pattern = "dd-MM-yyyy") Date DataInizio,
                                              @PathVariable("DataFine") @DateTimeFormat(pattern = "dd-MM-yyyy") Date DataFine,
                                              @RequestParam(value = "numPagina", defaultValue = "0") int numPagina,
                                              @RequestParam(value = "dimPagina", defaultValue = "20") int dimPagina,
                                              @RequestParam(value = "ordinamento", defaultValue = "data") String ordinamento) {
        try {
            LinkedList<String> ordinamentoValido=new LinkedList<>();
            ordinamentoValido.addAll(Arrays.asList("data","puntiusati","utente","totale"));

            if(numPagina<0 || dimPagina<=0 || !ordinamentoValido.contains(ordinamento)) {
                return new ResponseEntity<>(new ResponseMessage("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI"), HttpStatus.BAD_REQUEST);
            }
            //METTO DATAFINE A 23:59 COSI È COMPRESO SENNO LE QUERY LO ESCLUDONO
            Calendar cal = Calendar.getInstance();
            cal.setTime(DataFine);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            DataFine = cal.getTime();
            List<Ordine> listaOrdini = ordineService.getOrdiniInPeriodo(u, DataInizio, DataFine, numPagina, dimPagina, ordinamento);
            if (listaOrdini.isEmpty()) {
                return new ResponseEntity<>(new ResponseMessage("NESSUN ACQUISTO EFFETTUATO DURANTE QUESTO PERIODO O PRESENTE IN QUESTA PAGINA"), HttpStatus.OK);
            }
            return new ResponseEntity<>(listaOrdini, HttpStatus.OK);
        } catch (RangeDateNonAccettabile r) {
            return new ResponseEntity<>(new ResponseMessage("DATE FORNITE ERRATE"), HttpStatus.BAD_REQUEST);
        } catch (UtenteNonEsistenteONonValido e) {
            return new ResponseEntity<>(new ResponseMessage("UTENTE NON ESISTENTE O NON VALIDO"), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage("RICHIESTA ERRATA"), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('utente')")
    @GetMapping("/elencoOrdini/perUtente")
    public ResponseEntity getOrdineByUtente(@RequestBody @Valid Utente utente,
                                            @RequestParam(value = "numPagina", defaultValue = "0") int numPagina,
                                            @RequestParam(value = "dimPagina", defaultValue = "20") int dimPagina,
                                            @RequestParam(value = "ordinamento", defaultValue = "data") String ordinamento
                                            ) {
        try {
            LinkedList<String> ordinamentoValido = new LinkedList<>();
            ordinamentoValido.addAll(Arrays.asList("data", "puntiusati", "utente", "totale"));
            if (numPagina < 0 || dimPagina <= 0 || !ordinamentoValido.contains(ordinamento)) {
                return new ResponseEntity<>(new ResponseMessage("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI"), HttpStatus.BAD_REQUEST);
            }

            List<Ordine> listaOrdini = ordineService.ordiniCliente(utente, numPagina, dimPagina, ordinamento);
            if (listaOrdini.isEmpty()) {
                return new ResponseEntity<>(new ResponseMessage("NESSUN RISULTATO O NUMERO DI PAGINA NON VALIDO"), HttpStatus.OK);
            }
            return new ResponseEntity<>(listaOrdini, HttpStatus.OK);
        }catch (UtenteNonEsistenteONonValido e){
            return new ResponseEntity<>(new ResponseMessage("UTENTE NON ESISTENTE O NON VALIDO "), HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELLA RICERCA"), HttpStatus.BAD_REQUEST);
        }
    }


    @PreAuthorize("hasRole('utente')")
    @DeleteMapping("{idOrdine}")
    public ResponseEntity rimozioneOrdine(@PathVariable int idOrdine){
        try{
            ordineService.rimuoviOrdine(idOrdine);
            return new ResponseEntity<>(new ResponseMessage("RIMOZIONE ANDATA A BUON FINE"),HttpStatus.OK );
        }catch (OrdineNonPiuAnnullabileException e){
            return new ResponseEntity<>(new ResponseMessage("L'ORDINE NON È PIU' ANNULLABILE, È TRASCORSA PIU' DI UN'ORA DALLA SUA ACCETTAZIONE"),HttpStatus.BAD_REQUEST);
        }catch (UtenteNonEsistenteONonValido e){
            return new ResponseEntity<>(new ResponseMessage("UTENTE NON ESISTENTE O NON VALIDO"),HttpStatus.BAD_REQUEST);
        }catch (DettaglioOrdineNonValido e){
            return new ResponseEntity<>(new ResponseMessage("DETTAGLIO ORDINE NON ESISTENTE O NON VALIDO"),HttpStatus.BAD_REQUEST);
        }catch (ProdottoNonValidoException e){
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO NON ESISTENTE O NON VALIDO"),HttpStatus.BAD_REQUEST);
        } catch (OrdineNonPresenteNelDbExceptions e){
            return new ResponseEntity<>(new ResponseMessage("ORDINE NON PRESENTE NEL DATABASE"),HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELLA RIMOZIONE"), HttpStatus.BAD_REQUEST);
        }

    }

    @PreAuthorize("hasRole('utente')")
    @GetMapping("dettagliOrdine/{idOrdine}")
    public ResponseEntity getDettaglioOrdine(@PathVariable int idOrdine,
                                             @RequestParam(value = "numPagina", defaultValue = "0") int numPagina,
                                             @RequestParam(value = "dimPagina", defaultValue = "20") int dimPagina,
                                             @RequestParam(value = "ordinamento", defaultValue = "prezzoUnitario") String ordinamento
    ){
        try{
            LinkedList<String> ordinamentoValido = new LinkedList<>();
            ordinamentoValido.addAll(Arrays.asList("quantita", "prezzoUnitario"));
            if (numPagina < 0 || dimPagina <= 0 || !ordinamentoValido.contains(ordinamento)) {
                return new ResponseEntity<>(new ResponseMessage("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI"), HttpStatus.BAD_REQUEST);
            }
            List<DettaglioOrdine> listaDettagli=ordineService.trovaDettagliOrdine(idOrdine,numPagina,dimPagina,ordinamento);
            if(listaDettagli.isEmpty()){
                return new ResponseEntity<>(new ResponseMessage("NESSUN DETTAGLIO ASSOCIATO A QUESTO ORDINE O PAGINA VUOTA"),HttpStatus.OK);
            }
            return new ResponseEntity<>(listaDettagli, HttpStatus.OK);
        }catch(OrdineNonPresenteNelDbExceptions e){
        return new ResponseEntity(new ResponseMessage("ORDINE NON PRESENTE "), HttpStatus.BAD_REQUEST);
        }catch(Exception e){
            return new ResponseEntity(new ResponseMessage("ERRORE NELLA RICERCA"), HttpStatus.BAD_REQUEST);
        }
    }

}
