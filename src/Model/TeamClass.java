package Model;

import java.util.ArrayList;

/**
 * Created by Emanuele on 20/01/2016.
 */
public class TeamClass {
    private ArrayList<Player> titolari;
    private ArrayList <Player> riserve;

    public TeamClass(ArrayList<Player> riserve, ArrayList<Player> titolari) {
        this.riserve = riserve;
        this.titolari = titolari;
    }

    public ArrayList<Player> getRiserve() {
        return riserve;
    }

    public void setRiserve(ArrayList<Player> riserve) {
        this.riserve = riserve;
    }

    public ArrayList<Player> getTitolari() {
        return titolari;
    }

    public void setTitolari(ArrayList<Player> titolari) {
        this.titolari = titolari;
    }
}