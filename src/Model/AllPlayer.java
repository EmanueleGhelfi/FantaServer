package Model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Emanuele on 20/01/2016.
 */
public class AllPlayer implements Serializable {
    private ArrayList<Player> goalkeeper;
    private ArrayList<Player> defensors;
    private ArrayList<Player> midfielders;
    private ArrayList<Player> strikers;

    public AllPlayer(ArrayList<Player> defensors, ArrayList<Player> goalkeeper, ArrayList<Player> midfielders, ArrayList<Player> strikers) {
        this.defensors = defensors;
        this.goalkeeper = goalkeeper;
        this.midfielders = midfielders;
        this.strikers = strikers;
    }

    public ArrayList<Player> getDefensors() {
        return defensors;
    }

    public void setDefensors(ArrayList<Player> defensors) {
        this.defensors = defensors;
    }

    public ArrayList<Player> getGoalkeeper() {
        return goalkeeper;
    }

    public void setGoalkeeper(ArrayList<Player> goalkeeper) {
        this.goalkeeper = goalkeeper;
    }

    public ArrayList<Player> getMidfielders() {
        return midfielders;
    }

    public void setMidfielders(ArrayList<Player> midfielders) {
        this.midfielders = midfielders;
    }

    public ArrayList<Player> getStrikers() {
        return strikers;
    }

    public void setStrikers(ArrayList<Player> strikers) {
        this.strikers = strikers;
    }
}
