package com.example.provamiavuota.entities;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@Table(name="promozione",schema = "e_commerce_milan")
public class Promozione {
    @Id
    @Column(name = "id_promozione",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Basic
    @Column(name="nome",unique = true)
    private String nome;

    @Basic
    @Column(name="dettagli")
    private String dettagli;

    @Basic
    @Column(name="inizio")
    private Date inizio;

    @Basic
    @Column(name="fine")
    private Date fine;

    @Basic
    @Column(name="attiva",length=1)
    private int attiva;

    @OneToMany(targetEntity = ProdottiPromo.class, mappedBy = "promozione")
    private List<ProdottiPromo> prodottiPromozione;

}
