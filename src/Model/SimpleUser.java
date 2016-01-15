package Model;

/**
 * Created by Emanuele on 17/12/2015.
 */
public class SimpleUser {
    private String userName;
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public SimpleUser(String password, String userName) {
        this.password = password;
        this.userName = userName;
    }
}
