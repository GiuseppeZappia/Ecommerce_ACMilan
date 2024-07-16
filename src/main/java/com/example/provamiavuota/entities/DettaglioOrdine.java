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
@Table(name="dettaglio_ordine", schema = "e_commerce_milan",uniqueConstraints = {
                                                            @UniqueConstraint(columnNames = {"id_ prodotto", "id_ordine"})
                                                            })
public class DettaglioOrdine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_dettaglio_ordine",nullable = false)
    private int id;

    @Basic
    @Column(name = "quantita",nullable = true)
    private int quantita;

    @Basic
    @Column(name="prezzo_unitario")
    private Double prezzoUnitario;

    @ManyToOne
    @JoinColumn(name="id_ordine")
    @JsonIgnore
    //perche a me interessa che nell'ordine si vedano i dettagli, mica vedere per ogni dettaglio lo stesso ordine e per evitare cicli al solito???
    @ToString.Exclude
    private Ordine ordine;

    @ManyToOne(cascade=CascadeType.MERGE)
    @JoinColumn(name="id_prodotto")
    private Prodotto prodotto;

}
