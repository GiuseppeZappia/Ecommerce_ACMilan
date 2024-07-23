package com.example.provamiavuota.entities;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@Table(name="ordine",schema="e_commerce_milan")
public class Ordine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_ordine",nullable = false)
    @ToString.Exclude
    private int id;

    @Basic
    @CreationTimestamp
    @Column(name="data")
    private Date data;

    @Basic
    @Column(name="totale")
    private Double totale;

    @Basic
    @Column(name = "puntiusati")
    private Integer puntiusati;

    @ManyToOne
    @JoinColumn(name = "id_utente")
    private Utente utente;

    @OneToMany(mappedBy = "ordine",cascade = CascadeType.MERGE)
    private List<DettaglioOrdine> listaDettagliOrdine;

}
