package com.example.provamiavuota.controllers;

import com.example.provamiavuota.authentication.Utils;
import com.example.provamiavuota.dto.UtenteRegistrDTO;
import com.example.provamiavuota.services.RegistrazioneService;
import com.example.provamiavuota.supports.ResponseMessage;
import com.example.provamiavuota.supports.exceptions.ErroreNellaRegistrazioneUtenteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequestMapping("utenti")
public class RegistrazioneController {

    @Autowired
    private RegistrazioneService registrazioneService;

    @PostMapping
    private ResponseEntity createUser(@RequestBody UtenteRegistrDTO utenteRegistrDTO) {
        try{
        return registrazioneService.registraNuovoUtente(utenteRegistrDTO);
    }
        catch(ErroreNellaRegistrazioneUtenteException e){
            return new ResponseEntity<>(new ResponseMessage("PROBLEMA NELLA REGISTRAZIONE DELL'UTENTE"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity getUtente(Principal principal) {
//        try {
        System.out.println(principal);
        System.out.println(principal.getName());
        System.out.println(Utils.getEmail());
            return new ResponseEntity<>(registrazioneService.getUserById(principal.getName()), HttpStatus.OK);

//        }catch (ErroreLoginException e){
//            return new ResponseEntity<>(new ResponseMessage("PROBLEMA NEL LOGIN"), HttpStatus.BAD_REQUEST);
//        }
    }




}
