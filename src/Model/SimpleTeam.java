package Model;

/**
 * Created by Emanuele on 10/11/2015.
 */
public class SimpleTeam {
    private String teamName;
    private String utente;
    private int punteggio;

    public SimpleTeam(int punteggio, String teamName, String utente) {
        this.punteggio = punteggio;
        this.teamName = teamName;
        this.utente = utente;
    }

    public int getPunteggio() {
        return punteggio;
    }

    public void setPunteggio(int punteggio) {
        this.punteggio = punteggio;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getUtente() {
        return utente;
    }

    public void setUtente(String utente) {
        this.utente = utente;
    }
}
