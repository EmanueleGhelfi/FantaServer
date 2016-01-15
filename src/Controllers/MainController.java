package Controllers;


import Main.ServerMain;
import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * Created by Emanuele on 29/12/2015.
 */
public class MainController {

    @FXML
    Text txStatus;

    @FXML Text txHost;

    @FXML
    Circle circle;

    @FXML
    JFXButton startButton;

    private ServerMain serverMain;

    private boolean started;

    public void init(ServerMain serverMain){
        this.serverMain = serverMain;
        started=false;
        try {
            txHost.setText(""+Inet4Address.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            txHost.setText("localhost");
        }

    }

    public void startServer(ActionEvent actionEvent) {

        if(!started) {
            serverMain.OnStartServer();
            circle.setStroke(Color.GREEN);
            circle.setFill(Color.GREEN);
            started=true;
            startButton.setText("Stop!");
            startButton.setStyle("-fx-background-color: red");
            txStatus.setText("RUNNING");
        }
        else {
            serverMain.OnStopServer();
            txStatus.setText("STOPPED");
            circle.setStroke(Paint.valueOf("e83939"));
            circle.setFill(Paint.valueOf("e83939"));
            started=false;
            startButton.setText("Start!");
            startButton.setStyle("-fx-background-color:  #1e90ff");
        }
    }

    public void SendNotification(ActionEvent actionEvent) {
        serverMain.sendNotification();
    }
}
