package Main;

/**
 * Created by Emanuele on 25/01/2016.
 */
public class ServerCommandLine {
    private static Server task;
    private static Thread thread;

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
        System.out.println("CIAO");
        task = new Server();
        thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        while (thread.isAlive()){
            System.out.println("alive");
        }

    }
}
