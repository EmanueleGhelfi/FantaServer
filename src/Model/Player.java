package Model;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Emanuele on 21/10/2015.
 */
public class Player implements Serializable{

    private String cognome;
    private int id;
    private String squadra;
    private int costo;
    private char ruolo;
    private String pos;

    public Player(String cognome, int costo, int id, char ruolo, String squadra) {
        this.cognome = cognome;
        this.costo = costo;
        this.id = id;
        this.ruolo = ruolo;
        this.squadra = squadra;
        this.pos="0";
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public int getCosto() {
        return costo;
    }

    public void setCosto(int costo) {
        this.costo = costo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public char getRuolo() {
        return ruolo;
    }

    public void setRuolo(char ruolo) {
        this.ruolo = ruolo;
    }

    public String getSquadra() {
        return squadra;
    }

    public void setSquadra(String squadra) {
        this.squadra = squadra;
    }

    public Player() {
    }

    public Player(String cognome, int costo, int id, String pos, char ruolo, String squadra) {
        this.cognome = cognome;
        this.costo = costo;
        this.id = id;
        this.pos = pos;
        this.ruolo = ruolo;
        this.squadra = squadra;
    }


    @Override
    public String toString() {
        return "Player{" +
                "cognome='" + cognome + '\'' +
                ", id=" + id +
                ", squadra='" + squadra + '\'' +
                ", costo=" + costo +
                ", ruolo=" + ruolo +
                ", pos='" + pos + '\'' +
                '}';
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }
}
