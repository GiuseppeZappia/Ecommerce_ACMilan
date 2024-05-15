package com.example.provamiavuota.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@Table(name="prodotto",schema="e_commerce_milan",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"nome", "categoria"})
})
public class Prodotto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_prodotto",nullable = false)
    private int id;

    @Version
    @Column(name="version",nullable = false)
    @JsonIgnore
    private int version;

    @Basic
    @Column(name="nome",nullable = true,unique = true)
    private String nome;

    @Basic
    @Column(name="categoria",nullable = true)
    private String categoria;

    @Basic
    @Column(name="descrizione",nullable = true)
    private String descrizione;

    @Basic
    @Column(name="prezzo",nullable = true)
    private Double prezzo;

    @Basic
    @Column(name="quantita",nullable = true)
    private int quantita;

    @Basic
    @Column(name="immagine",nullable = true)
    private int immagine;

    @Basic
    @Column(name="hidden",length = 1)
    private int nascosto;

    @Basic
    @Column(name="preferito",length = 1)
    private int preferito;

    @OneToMany(targetEntity = DettaglioOrdine.class, mappedBy="prodotto",cascade = CascadeType.MERGE)
    @JsonIgnore//cosi evito i cicli (guarda la cosa LAZY che diceva prof pure)
    @ToString.Exclude
    private List<DettaglioOrdine> dettaglioOrdini;

    @OneToMany(targetEntity = ProdottiPromo.class, mappedBy="prodotto",cascade = CascadeType.MERGE)
    @JsonIgnore//cosi evito i cicli (guarda la cosa LAZY che diceva prof pure)
    @ToString.Exclude
    private List<ProdottiPromo> dettaglioPromozioni;
}
