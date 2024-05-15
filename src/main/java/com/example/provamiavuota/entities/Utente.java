package com.example.provamiavuota.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "utente",schema = "e_commerce_milan")
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_utente",nullable = false)
    //@JsonIgnore
    private int id;

    @Basic
    @Column(name="nome")
    private String nome;

    @Basic
    @Column(name="cognome")
    private String cognome;

    @Basic
    @Column(name="email",unique=true)
    private String email;

    @Basic
    @Column(name="username",unique=true)
    private String username;

    @Basic
    @Column(name="password")
    //@JsonIgnore //non voglio lo serializzi sulla rete
    @ToString.Exclude
    private String password;

    @Basic
    @Column(name="punti_fedelta")
    private int puntifedelta;

    @OneToMany(mappedBy = "utente",cascade = CascadeType.MERGE)
    @JsonIgnore//non mi interessa vedere ogni volta che prendo l'utente i suoi ordini, al massimo poi faccio query
    @ToString.Exclude
    private List<Ordine> ordini;

}
