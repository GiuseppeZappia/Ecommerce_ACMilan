package com.example.provamiavuota.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@Table(name="dettaglio_carrello", schema = "e_commerce_milan",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idcarrello", "idprodotto"})
})

public class DettaglioCarrello {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_dettaglio_carrello",nullable = false)
    private int id;

    @ManyToOne
    @JoinColumn(name="idcarrello")
    @JsonIgnore
    //stesso motivo dettaglio ordine e ordine
    @ToString.Exclude
    private Carrello carrello;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name="idprodotto")
    private Prodotto prodotto;

    @Basic
    @Column(name = "quantita",nullable = true)
    private int quantita;

    @Basic
    @Column(name="prezzounitario")
    private Double prezzoUnitario;


}
