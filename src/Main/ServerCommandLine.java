package Main;

/**
 * Created by Emanuele on 25/01/2016.
 */
public class ServerCommandLine {
    private Server task;
    private Thread thread;

    public ServerCommandLine(Server task, Thread thread) {
        this.task = task;
        this.thread = thread;
    }

    public Server getTask() {
        return task;
    }

    public void setTask(Server task) {
        this.task = task;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public ServerCommandLine() {
    }

    public static void main(String[] args){
        ServerCommandLine serverCommandLine = new ServerCommandLine();
        serverCommandLine.task = new Server();
        serverCommandLine.thread = new Thread(serverCommandLine.task);
        serverCommandLine.thread.start();
    }
}
