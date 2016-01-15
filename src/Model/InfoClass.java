package Model;

/**
 * Created by Emanuele on 04/01/2016.
 */
public class InfoClass {
    private String bestPlayer;
    private float mediabest;
    private String worstPlayer;
    private float mediaWorst;
    private String mostPresPlayer;
    private int pres;

    public InfoClass(String bestPlayer, float mediabest, float mediaWorst, String mostPresPlayer, int pres, String worstPlayer) {
        this.bestPlayer = bestPlayer;
        this.mediabest = mediabest;
        this.mediaWorst = mediaWorst;
        this.mostPresPlayer = mostPresPlayer;
        this.pres = pres;
        this.worstPlayer = worstPlayer;
    }

    public InfoClass() {
    }

    public String getBestPlayer() {
        return bestPlayer;
    }

    public void setBestPlayer(String bestPlayer) {
        this.bestPlayer = bestPlayer;
    }

    public float getMediabest() {
        return mediabest;
    }

    public void setMediabest(float mediabest) {
        this.mediabest = mediabest;
    }

    public float getMediaWorst() {
        return mediaWorst;
    }

    public void setMediaWorst(float mediaWorst) {
        this.mediaWorst = mediaWorst;
    }

    public String getMostPresPlayer() {
        return mostPresPlayer;
    }

    public void setMostPresPlayer(String mostPresPlayer) {
        this.mostPresPlayer = mostPresPlayer;
    }

    public int getPres() {
        return pres;
    }

    public void setPres(int pres) {
        this.pres = pres;
    }

    public String getWorstPlayer() {
        return worstPlayer;
    }

    public void setWorstPlayer(String worstPlayer) {
        this.worstPlayer = worstPlayer;
    }
}
