package Model;

import java.io.PrintWriter;

/**
 * Created by Emanuele on 05/01/2016.
 */
public class UserSocket {
    private User user;
    private PrintWriter out;

    public UserSocket(PrintWriter out, User user) {
        this.out = out;
        this.user = user;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
