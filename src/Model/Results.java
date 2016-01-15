package Model;

/**
 * Created by Emanuele on 20/12/2015.
 */
public class Results {
    private int giornata;
    private float point;

    public Results(int giornata, float point) {
        this.giornata = giornata;
        this.point = point;
    }

    public int getGiornata() {
        return giornata;
    }

    public void setGiornata(int giornata) {
        this.giornata = giornata;
    }

    public float getPoint() {
        return point;
    }

    public void setPoint(float point) {
        this.point = point;
    }
}
