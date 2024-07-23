package com.example.provamiavuota.entities;


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
@Table(name="carrello",schema="e_commerce_milan",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_utente", "attivo"})
})
public class Carrello {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idcarrello",nullable = false)
    @ToString.Exclude
    private int id;

    @OneToOne //un utente puo avere un solo carrello e un carrello corrisponde ad un solo utente
    @JoinColumn(name = "id_utente")
    private Utente utente;

    @Basic
    @Column(name="attivo",length=1)
    private int attivo;

    @OneToMany(mappedBy = "carrello",cascade = CascadeType.MERGE)
    private List<DettaglioCarrello> listaDettagliCarrello;

}
