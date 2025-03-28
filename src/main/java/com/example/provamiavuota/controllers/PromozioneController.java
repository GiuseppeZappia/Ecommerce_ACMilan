package com.example.provamiavuota.controllers;

import com.example.provamiavuota.entities.Promozione;
import com.example.provamiavuota.services.PromozioneService;
import com.example.provamiavuota.supports.ResponseMessage;
import com.example.provamiavuota.supports.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/promozioni")
public class PromozioneController {
    @Autowired
    private PromozioneService promozioneService;

    @GetMapping("/elenco")
    public ResponseEntity elenco(@RequestParam(value = "numPagina", defaultValue = "0") int numPagina,
                                 @RequestParam(value = "dimPagina", defaultValue = "20") int dimPagina,
                                 @RequestParam(value = "ordinamento", defaultValue = "fine") String ordinamento) {

        LinkedList<String> ordinamentoValido=new LinkedList<>();
        ordinamentoValido.addAll(Arrays.asList("nome","inizio","fine"));
        if(numPagina<0 || dimPagina<=0 || !ordinamentoValido.contains(ordinamento)) {
            System.out.println("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI");
            return new ResponseEntity<>(new ResponseMessage("PAGINAZIONE NON VALIDA PER I PARAMETRI PASSATI"), HttpStatus.BAD_REQUEST);
        }
        List<Promozione> listaPromo= promozioneService.elencoPromozioni(numPagina, dimPagina, ordinamento);
        if (listaPromo.isEmpty()) {
            System.out.println("NESSUN RISULTATO O NUMERO DI PAGINA NON VALIDO");
            return new ResponseEntity<>(new ResponseMessage("NESSUN RISULTATO O NUMERO DI PAGINA NON VALIDO"), HttpStatus.OK);
        }
        return new ResponseEntity<>(listaPromo, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping("/aggiuntaApromo/{idPromozione}/{idProdotto}/{percentualeSconto}")
    public ResponseEntity aggiungiProdottoAPromo(@PathVariable int idPromozione,@PathVariable int idProdotto,@PathVariable int percentualeSconto) {
        try {
            Promozione modificata = promozioneService.aggiungiProdotto(idPromozione, idProdotto,percentualeSconto);
            if (modificata == null) {
                System.out.println("ERRORE NELL'AGGIUNTA");
                return new ResponseEntity<>(new ResponseMessage("ERRORE NELL'AGGIUNTA"), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(modificata, HttpStatus.OK);
        }catch (PercentualeScontoNonValidaExceptions e){
            System.out.println("PERCENTUALE SCONTO NON VALIDA");
            return new ResponseEntity<>(new ResponseMessage("PERCENTUALE SCONTO NON VALIDA"),HttpStatus.BAD_REQUEST);
        }catch (ProdottoNonPresenteNelDbExceptions e) {
            System.out.println("PRODOTTO NON PRESENTE NEL DB");
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO NON PRESENTE NEL DB"), HttpStatus.BAD_REQUEST);
        }catch (PromozioneNonPresenteException e) {
            System.out.println("PROMOZIONE NON PRESENTE");
            return new ResponseEntity<>(new ResponseMessage("PROMOZIONE NON PRESENTE "), HttpStatus.BAD_REQUEST);
        }catch(ProdottoGiaPresenteNellaPromozioneException e){
            System.out.println("PRODOTTO GIA PRESENTE NELLA PROMOZIONE");
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO GIA PRESENTE NELLA PROMOZIONE "), HttpStatus.BAD_REQUEST);
        } catch(PromozioneNonAttivaException e){
            System.out.println("PROMOZIONE NON ATTIVA");
            return new ResponseEntity<>(new ResponseMessage("PROMOZIONE NON ATTIVA "), HttpStatus.BAD_REQUEST);
        }catch(Exception e){
            System.out.println("ERRORE NELL'AGGIUNTA DEL PRODOTTO ALLA PROMO");
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELL'AGGIUNTA DEL PRODOTTO ALLA PROMO"),HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping("/creaNuova")
    public ResponseEntity creaNuovaPromozione(@RequestBody @Valid Promozione promozione){
        try {
            Promozione p = promozioneService.creaNuovaPromo(promozione);
            return new ResponseEntity<>(p, HttpStatus.OK);
        }catch(PromozioneNonValida e){
            System.out.println("PROMOZIONE NON VALIDA");
            return new ResponseEntity<>(new ResponseMessage("PROMOZIONE NON VALIDA"), HttpStatus.BAD_REQUEST);
        }catch (PromozioneGiaPresenteException e){
            System.out.println("PROMOZIONE GIA PRESENTE");
            return new ResponseEntity<>(new ResponseMessage("PROMOZIONE GIA PRESENTE "), HttpStatus.BAD_REQUEST);
        }catch(ProdottoNonPresenteNelDbExceptions e){
            System.out.println("PRODOTTO NON PRESENTE NEL DB");
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO NON PRESENTE NEL DB"), HttpStatus.BAD_REQUEST);
        }catch (PromozioneNonPresenteException e) {
            System.out.println("PROMOZIONE NON PRESENTE");
            return new ResponseEntity<>(new ResponseMessage("PROMOZIONE NON PRESENTE "), HttpStatus.BAD_REQUEST);
        }catch(ProdottoGiaPresenteNellaPromozioneException e){
            System.out.println("PRODOTTO GIA PRESENTE NELLA PROMOZIONE");
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO GIA PRESENTE NELLA PROMOZIONE "), HttpStatus.BAD_REQUEST);
        } catch(PromozioneNonAttivaException e){
            System.out.println("PROMOZIONE NON ATTIVA");
            return new ResponseEntity<>(new ResponseMessage("PROMOZIONE NON ATTIVA "), HttpStatus.BAD_REQUEST);
        } catch (PercentualeScontoNonValidaExceptions e){
            System.out.println("PERCENTUALE SCONTO NON VALIDA");
            return new ResponseEntity<>(new ResponseMessage("PERCENTUALE SCONTO NON VALIDA"),HttpStatus.BAD_REQUEST);
        }catch(Exception e){
            System.out.println("ERRORE NELLA CREAZIONE DELLA PROMOZIONE");
            return new ResponseEntity<>(new ResponseMessage("ERRORE NELLA CREAZIONE DELLA PROMOZIONE "), HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('utente')")
    @GetMapping("/coinvolto/{idProdotto}")
    public ResponseEntity coinvolto(@PathVariable int idProdotto){
        try {
            boolean ret = promozioneService.coinvolto(idProdotto);
            return new ResponseEntity(ret, HttpStatus.OK);
        }catch(ProdottoNonValidoException e){
            System.out.println("PRODOTTO NON VALIDO");
            return new ResponseEntity<>(new ResponseMessage("PRODOTTO NON VALIDO"), HttpStatus.BAD_REQUEST);
        }
        catch (Exception e){
            System.out.println("ERRORE");
            return new ResponseEntity(new ResponseMessage("ERRORE"),HttpStatus.BAD_REQUEST);
        }
    }


}
