package Model;

/**
 * Created by Emanuele on 08/12/2015.
 */
public class PlayerVoto {
    private float voto;
    private String cognome;
    private boolean titolare;
    private char ruolo;
    private String posizione;
    private boolean entrato;
    private boolean sostituito;

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public float getVoto() {
        return voto;
    }

    public void setVoto(float voto) {
        this.voto = voto;
    }

    public PlayerVoto(char ruolo, String cognome, boolean titolare, float voto, boolean entrato, boolean sostituito) {
        this.ruolo = ruolo;
        this.cognome = cognome;
        this.titolare = titolare;
        this.voto = voto;
        this.entrato = entrato;
        this.sostituito = sostituito;
    }

    public boolean isEntrato() {
        return entrato;
    }

    public void setEntrato(boolean entrato) {
        this.entrato = entrato;
    }


    public boolean isSostituito() {
        return sostituito;
    }

    public void setSostituito(boolean sostituito) {
        this.sostituito = sostituito;
    }

    public PlayerVoto(char ruolo, String cognome, boolean titolare, float voto, String posizione) {
        this.ruolo = ruolo;
        this.cognome = cognome;
        this.titolare = titolare;
        this.voto = voto;
        this.posizione = posizione;
    }



    public String getPosizione() {
        return posizione;
    }

    public void setPosizione(String posizione) {
        this.posizione = posizione;
    }

    public char getRuolo() {
        return ruolo;
    }

    public void setRuolo(char ruolo) {
        this.ruolo = ruolo;
    }

    public boolean isTitolare() {
        return titolare;
    }

    public void setTitolare(boolean titolare) {
        this.titolare = titolare;
    }
}
