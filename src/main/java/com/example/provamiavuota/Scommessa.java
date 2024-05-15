package com.example.provamiavuota;

import java.net.InetAddress;
import java.util.Objects;

public class Scommessa {
    private int id;
    private String nome;
    private long puntata;
    private String scommettitoreIp;

    public Scommessa(int id, String nome, long puntata, String scommettitoreIp) {
        this.id = id;
        this.nome = nome;
        this.puntata = puntata;
        this.scommettitoreIp = scommettitoreIp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scommessa scommessa = (Scommessa) o;
        return id == scommessa.id && puntata == scommessa.puntata && Objects.equals(nome, scommessa.nome) && Objects.equals(scommettitoreIp, scommessa.scommettitoreIp);
    }

    @Override
    public String toString() {
        return "Scommessa{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", puntata=" + puntata +
                ", scommettitoreIp=" + scommettitoreIp +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nome, puntata, scommettitoreIp);
    }

    public String getNome() {
        return nome;
    }

    public long getPuntata() {
        return puntata;
    }

    public String getScommettitoreIp() {
        return scommettitoreIp;
    }

    public int getId() {
        return id;
    }
}
