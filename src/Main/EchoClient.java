package Main; /**
 * Created by Emanuele on 15/10/2015.
 */

import java.net.*;
import java.io.*;
public class EchoClient {
    public static void main(String[] args) throws IOException{
        String hostName = "localhost";
        int portNumber = 4444;

        try (
                Socket echoSocket = new Socket(hostName,portNumber);
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(),true);
                BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

                )
        {
            String userInput;
            while ((userInput = stdIn.readLine())!=null){
                out.println(userInput);
                System.out.println("Server: " + in.readLine());
            }

        }
        catch (UnknownHostException e){

        }
        //catch (IOException exception){

        //}

    }
}
