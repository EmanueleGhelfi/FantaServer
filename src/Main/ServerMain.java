package Main;

import Controllers.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

/**
 * Created by Emanuele on 29/12/2015.
 */
public class ServerMain extends Application {

    private MainController mainController;
    private EchoServer task;
    private Thread thread;


    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/FXML/main.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("FantaServer");
        primaryStage.getIcons().add(new Image("/Images/server.png"));
        primaryStage.setScene(new Scene(root, 500, 500));
        mainController = loader.getController();
        mainController.init(this);
        primaryStage.show();


        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                System.out.println("STO CHIUDENDO BOTTEGA");
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public void OnStartServer() {
        task = new EchoServer(this);
         thread = new Thread(task);
        thread.start();
    }

    public void OnStopServer() {
        try {
            thread.stop();
            if(task.getServerSocket()!=null)
                task.getServerSocket().close();
            for(int i = 0;i<task.getSocketArray().size();i++){
                task.getSocketArray().get(i).close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendNotification() {
        task.SendNotification();
    }
}
