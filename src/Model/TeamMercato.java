package Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Emanuele on 20/01/2016.
 */
public class TeamMercato implements Serializable{
    private ArrayList<Player> players;
    private int soldi;


    public TeamMercato(ArrayList<Player> players, int soldi) {
        this.players = players;
        this.soldi = soldi;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public int getSoldi() {
        return soldi;
    }

    public void setSoldi(int soldi) {
        this.soldi = soldi;
    }
}
