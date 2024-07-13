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
}
