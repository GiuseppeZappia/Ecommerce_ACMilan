package com.example.provamiavuota.controllers;

import com.example.provamiavuota.dto.LoginDTO;
import com.example.provamiavuota.dto.UtenteRegistrDTO;
import com.example.provamiavuota.entities.Utente;
import com.example.provamiavuota.services.RegistrazioneService;
import com.example.provamiavuota.supports.ResponseMessage;
import com.example.provamiavuota.supports.exceptions.ErroreLoginException;
import com.example.provamiavuota.supports.exceptions.ErroreLogoutException;
import com.example.provamiavuota.supports.exceptions.ErroreNellaRegistrazioneUtenteException;
import com.example.provamiavuota.supports.exceptions.UtenteNonEsistenteONonValido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.net.URI;

@RestController
@RequestMapping("utenti")
public class RegistrazioneController {

    @Autowired
    private RegistrazioneService registrazioneService;

    @PostMapping("/registrazione")
    private ResponseEntity createUser(@NotNull @RequestBody UtenteRegistrDTO utenteRegistrDTO) {
        try {
            return registrazioneService.registraNuovoUtente(utenteRegistrDTO);
        } catch (ErroreNellaRegistrazioneUtenteException e) {
            System.out.println("PROBLEMA NELLA REGISTRAZIONE DELL'UTENTE");
            return new ResponseEntity<>(new ResponseMessage("PROBLEMA NELLA REGISTRAZIONE DELL'UTENTE"), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            System.out.println("PROBLEMA NELLA REGISTRAZIONE DELL'UTENTE");
            return new ResponseEntity<>(new ResponseMessage("PROBLEMA NELLA REGISTRAZIONE DELL'UTENTE"), HttpStatus.BAD_REQUEST);
        }
    }



    @GetMapping("/trovaUtente")
    private ResponseEntity getUtente() {
        try {
            Utente u=registrazioneService.trovaUtente();
            if(u==null) {
                return null;
            }
            return new ResponseEntity<>(u, HttpStatus.OK);
        }catch (UtenteNonEsistenteONonValido e) {
            System.out.println("NESSUN UTENTE AUTENTICATO");
            return new ResponseEntity<>(new ResponseMessage("NESSUN UTENTE AUTENTICATO"),HttpStatus.BAD_REQUEST);
        }catch (Exception e){
//            System.out.println("ERRORE OTTENIMENTO UTENTE");
            return new ResponseEntity<>(new ResponseMessage("ERRORE OTTENIMENTO UTENTE"),HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/logout/{refreshToken}")
    public ResponseEntity logoutUtente(@NotNull @PathVariable String refreshToken) {
        try {
            //QUESTE DUE RIGHE SERVONO PER RIMANDARMI ALLA PAGINA DI LOGIN DOPO LOGOUT
            registrazioneService.logoutUser(refreshToken);
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/login")).build();
        }catch (ErroreLogoutException e){
            System.out.println("ERRORE DURANTE IL LOGOUT");
            return new ResponseEntity<>(new ResponseMessage("ERRORE DURANTE IL LOGOUT"), HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            System.out.println("ERRORE");
            return new ResponseEntity<>(new ResponseMessage("ERRORE"), HttpStatus.BAD_REQUEST);
        }
    }

}
