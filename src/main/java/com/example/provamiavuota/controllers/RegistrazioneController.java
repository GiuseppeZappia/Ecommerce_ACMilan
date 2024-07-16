package com.example.provamiavuota.controllers;

import com.example.provamiavuota.dto.LoginDTO;
import com.example.provamiavuota.dto.UtenteRegistrDTO;
import com.example.provamiavuota.services.RegistrazioneService;
import com.example.provamiavuota.supports.ResponseMessage;
import com.example.provamiavuota.supports.exceptions.ErroreLoginException;
import com.example.provamiavuota.supports.exceptions.ErroreLogoutException;
import com.example.provamiavuota.supports.exceptions.ErroreNellaRegistrazioneUtenteException;
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
            return new ResponseEntity<>(new ResponseMessage("PROBLEMA NELLA REGISTRAZIONE DELL'UTENTE"), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/login")
    public ResponseEntity loginUtente(@NotNull @RequestBody LoginDTO loginDTO) {
        try {
            return registrazioneService.loginUser(loginDTO);
        } catch (ErroreLoginException e) {
            return new ResponseEntity<>(new ResponseMessage("CREDENZIALI NON VALIDE"), HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }

    }


    @PostMapping("/logout/{refreshToken}")
    public ResponseEntity logoutUtente(@NotNull @PathVariable String refreshToken) {
        try {
//            return registrazioneService.logoutUser(refreshToken);
            registrazioneService.logoutUser(refreshToken);//QUESTE DUE RIGHE SOSTITUISCONO LA COMMENTATA, DOVREBBERO SERVIRE PER RIMANDARMI ALLA PAGINA DI LOGIN DOPO LOGOUT
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/login")).build();
        }catch (ErroreLogoutException e){
            return new ResponseEntity<>(new ResponseMessage("ERRORE DURANTE IL LOGOUT"), HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

}
