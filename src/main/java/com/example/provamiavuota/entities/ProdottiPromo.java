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
@Table(name="prodotti_promo",schema = "e_commerce_milan")
public class ProdottiPromo {
    @Id
    @Column(name = "id_prodotti_promo",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Basic
    @Column(name = "sconto")
    private int sconto;

    @ManyToOne()//cascade = CascadeType.MERGE)
    @JoinColumn(name = "id_promozione")
    @ToString.Exclude
    @JsonIgnore
    private Promozione promozione;

    @ManyToOne()//cascade=CascadeType.MERGE)
    @JoinColumn(name="id_prodotto",unique = true)
    private Prodotto prodotto;



}
