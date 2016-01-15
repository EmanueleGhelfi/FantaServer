package Model;

import Model.Player;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Emanuele on 27/10/2015.
 */
public class User {

    private String teamName;
    private String password;
    private String userName;
    private ArrayList<Player> team;
    private String email;
    private String imagePath;
    private LocalDate dataNacita;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<Player> getTeam() {
        return team;
    }

    public void setTeam(ArrayList<Player> team) {
        this.team = team;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public User(String password, ArrayList<Player> team, String teamName, String userName) {
        this.password = password;
        this.team = team;
        this.teamName = teamName;
        this.userName = userName;
    }

    public User(LocalDate dataNacita, String email, String imagePath, String password, ArrayList<Player> team, String teamName, String userName) {
        this.dataNacita = dataNacita;
        this.email = email;
        this.imagePath = imagePath;
        this.password = password;
        this.team = team;
        this.teamName = teamName;
        this.userName = userName;
    }

    public User(LocalDate dataNacita, String email, String password, ArrayList<Player> team, String teamName, String userName) {
        this.dataNacita = dataNacita;
        this.email = email;
        this.password = password;
        this.team = team;
        this.teamName = teamName;
        this.userName = userName;
    }

    public LocalDate getDataNacita() {
        return dataNacita;
    }

    public void setDataNacita(LocalDate dataNacita) {
        this.dataNacita = dataNacita;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}