package com.example.provamiavuota;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

@SpringBootApplication
@RestController
public class ProvamiavuotaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProvamiavuotaApplication.class, args);
    }

    @GetMapping("/")
    public String saluto(){
        return "CIAAAO";
    }

    @GetMapping("/scommessa")
    public String dettaglio(){
        return "Aggiungi l'ID della tua prenotazione all'URL per vedere i dettagli di quella scommessa";
    }

    @GetMapping("/pisnelo")
    public LinkedList<String> pisnelo(){
        LinkedList<String> ret=new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            ret.add("ciao "+i);
        }
        return ret;
    }
    @RequestMapping(value="/scommessa/{numero}",method = RequestMethod.GET)
    public Scommessa scomm(@PathVariable ("numero") int numero ) throws UnknownHostException {
        return new Scommessa(numero,"scommGoldBet",151, InetAddress.getLocalHost().getHostAddress());
    }
}
