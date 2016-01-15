package Main;

import Constants.Communication;
import Model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by Emanuele on 15/10/2015.
 */
public class EchoThread extends Thread {

    protected Socket socket;
    private int number;
    private Connection conn;
    private boolean auth=false;
    private boolean user=false;
    private boolean pw=false;
    private String username;
    private String password;
    //Object that contains user's info
    private User currentUser;

    private ArrayList <Player> titolari;
    private ArrayList <Player> riserve;

    private ArrayList<Player> team;

    //Instance of Echo Server
    private EchoServer mainInstance;

    public EchoThread(Socket clientSocket, int number,Connection conn, EchoServer mainInstance) {
        this.socket = clientSocket;
        this.number = number;
        this.conn = conn;
        this.mainInstance = mainInstance;
    }

    public void run() {
        //InputStreamReader in = null;
        BufferedReader in = null;
        PrintWriter out = null;
        //SendWelcome();
        //Main.EchoServer.mossa = 0;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //reader = new BufferedReader(new InputStreamReader(in));
            out = new PrintWriter(socket.getOutputStream(), true);
            String line;
            //SendEmailToUser();
                while ((line = in.readLine()) != null) {
                    /*System.out.println(line);
                    out.println("Ciao client numero " + number + " Hai scritto " + line);
                    */
                    System.out.println(line);

                    switch (line){
                        case (Communication.Auth):
                            out.println(Communication.OK);
                            PreAuthClient(in,out);
                            break;

                        case (Communication.GETDATA):
                            SendPor();
                            break;
                        case (Communication.OKPOR):
                            SendDef();
                            break;
                        case (Communication.OKDEF):
                            SendCen();
                            break;
                        case (Communication.OKCEN):
                            SendAtt();
                            break;
                        case(Communication.SENDTEAM):
                            ReceiveNewTeam(in,out);
                            break;
                        case (Communication.GETTEAM):
                            SendUserTeam();
                            break;
                        case (Communication.SENDTITOLARI):
                            getTitolari(in,out);
                            break;
                        case (Communication.GETCLASSIFICA):
                            SendClassifica(in,out);
                            break;
                        case(Communication.GETALLPLAYERS):
                            SendAllPlayers(in,out);
                            break;
                        case (Communication.SENDMODIFIEDTEAM):
                            ReceiveModifiedTeam(in,out);
                            break;
                        case (Communication.GETVOTI):
                            SendVoti(in,out);
                            break;
                        case (Communication.GETGIORNATE):
                            SendGiornate(in,out);
                            break;
                        case (Communication.GETLASTDAY):
                            SendLastDay(in,out);
                            break;
                        case (Communication.GETUSER):
                            SendUser(in,out);
                            break;
                        case(Communication.SENDFILE):
                            GetFileFromUser(in,out);
                            break;
                        case (Communication.GETIMAGE):
                            SendFileToUser(in,out);
                            break;
                        case (Communication.GETRESULTS):
                            SendResultsToUser(in,out);
                            break;
                        case (Communication.SENDMODIFIEDUSER):
                            GetModifiedUser(in,out);
                            break;
                        case (Communication.GETINFO):
                            SendInfo(in,out);
                            break;
                        /*default: if(auth && !user){
                            System.out.println("OKuser");
                            out.println("OKUSER");
                            username = line;
                            user = true;
                        }
                            else {
                            System.out.println("OKPW");
                            password = line;
                            //InsertClientToDB(username, password);
                            AuthClient(username,password);
                            auth = false;
                            user = false;
                            pw = false;
                        }
                            break;
                            */
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                RemoveUser(currentUser,out);
            //Connection reset
            }

    }

    private void SendInfo(BufferedReader in, PrintWriter out) {
        out.println(Communication.READYFORINFO);
        InfoClass infoClass = new InfoClass();
        try {
            Statement s = conn.createStatement();
            //TODO: Migliorare la Query
            //creo view con media
            s.execute("CREATE VIEW MEDIE(cognome,media) as SELECT squadre.Cognome as cognome, AVG (Voto) as media FROM squadre JOIN votogiocatore on squadre.idGioc=votogiocatore.idGioc WHERE squadre.username='"+currentUser.getUserName()+"' AND squadre.TeamName = '"+currentUser.getTeamName()+"' GROUP BY squadre.Cognome");
            ResultSet res = s.executeQuery("SELECT MEDIE.cognome, media from MEDIE WHERE media = (SELECT max(media) from MEDIE)");
            while (res.next()){
                infoClass.setBestPlayer(res.getString("cognome"));
                infoClass.setMediabest(res.getFloat("media"));
            }
            ResultSet res2 = s.executeQuery("SELECT MEDIE.cognome, media from MEDIE WHERE media= (SELECT min(media) from medie)");
            while (res2.next()){
                infoClass.setWorstPlayer(res2.getString("cognome"));
                infoClass.setMediaWorst(res2.getFloat("media"));
            }
            s.execute("DROP VIEW MEDIE");
            s.execute("CREATE VIEW PRES(cognome,pres) as SELECT Cognome, COUNT(*) as pres FROM formazione WHERE formazione.userName='"+currentUser.getUserName()+"' AND formazione.nomeSquadra = '"+currentUser.getTeamName()+"' and (formazione.Titolare='1' or formazione.Entrato='1') GROUP BY Cognome");
            ResultSet res3 = s.executeQuery("SELECT PRES.cognome, pres from PRES WHERE pres=(SELECT max(pres) from PRES);");
            while (res3.next()){
                infoClass.setMostPresPlayer(res3.getString("cognome"));
                infoClass.setPres(res3.getInt("pres"));
            }
            s.execute("DROP VIEW PRES");
            Gson gson = new Gson();
            String infoString = gson.toJson(infoClass);
            out.println(infoString);
        } catch (SQLException e) {
            e.printStackTrace();
            Statement s2 = null;
            try {
                s2 = conn.createStatement();
                s2.execute("DROP VIEW MEDIE");
                s2.execute("DROP VIEW PRES");
            } catch (SQLException e1) {
                e1.printStackTrace();
            }

        }
    }

    private void GetModifiedUser(BufferedReader in, PrintWriter out) {
        out.println(Communication.READYFORMODIFIEDUSER);
        Gson gson = new Gson();
        try {
            String userString = in.readLine();
            User user = gson.fromJson(userString,User.class);
            boolean alreadyExist = SearchForExistingUser(user);
            if(!alreadyExist){
                RemoveUser(currentUser,out);
                ChangePhotoName(user.getUserName());
                ChangeInClient(user);
                ChangeInFormazione(user);
                ChangeInPunteggi(user);
                ChangeInSquadre(user);
                this.currentUser=user;
                mainInstance.addUser(currentUser,out);
                out.println(Communication.USEROK);
            }
            else {
                out.println(Communication.USERNO);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ChangeInSquadre(User user) {
        try {
            Statement s = conn.createStatement();
            s.execute("UPDATE squadre SET username ='"+user.getUserName()+"', TeamName='"+user.getTeamName()+"'  WHERE username ='"+currentUser.getUserName()+"' AND TeamName='"+currentUser.getTeamName()+"' ");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ChangeInPunteggi(User user) {
        try {
            Statement s = conn.createStatement();
            s.execute("UPDATE punteggi SET UserName ='"+user.getUserName()+"', TeamName='"+user.getTeamName()+"'  WHERE UserName ='"+currentUser.getUserName()+"' AND TeamName='"+currentUser.getTeamName()+"' ");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ChangeInFormazione(User user) {
        try {
            Statement s = conn.createStatement();
            s.execute("UPDATE formazione SET userName ='"+user.getUserName()+"', nomeSquadra='"+user.getTeamName()+"'  WHERE userName ='"+currentUser.getUserName()+"' AND nomeSquadra='"+currentUser.getTeamName()+"' ");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ChangeInClient(User user) {
        try {
            Statement s = conn.createStatement();
            s.execute("UPDATE client SET Username ='"+user.getUserName()+"', TeamName='"+user.getTeamName()+"', email='"+user.getEmail()+"', DataNascita ='"+user.getDataNacita()+"' WHERE Username ='"+currentUser.getUserName()+"' AND TeamName='"+currentUser.getTeamName()+"' ");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //return true if the user already exist.
    private boolean SearchForExistingUser(User user) {
        int cont=0;
        //Se nome non è cambiato posso modificare
        if(user.getUserName().equals(this.currentUser.getUserName())){
            return false;
        }
        else {
            try {
                Statement s = conn.createStatement();
                ResultSet res = s.executeQuery("SELECT * from client WHERE Username ='"+user.getUserName()+"'");
                while (res.next()){
                    cont++;
                }
                if(cont>0){
                    return true;
                }
                else {
                    return false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


        }



        return false;
    }

    private void ChangePhotoName(String userName) {
        File file = new File("C:\\Users\\Emanuele\\Desktop\\userPhoto\\" + currentUser.getUserName() + ".jpg");
        if(!file.exists()){
            System.out.println("File not found");
        }
        else {
            File newFile = new File("C:\\Users\\Emanuele\\Desktop\\userPhoto\\" + userName + ".jpg");
            file.renameTo(newFile);
            System.out.println("Rinominato con successo");
        }

    }

    private void RemoveUser(User currentUser, PrintWriter out) {
        if(currentUser!=null){
            mainInstance.removeUser(currentUser,out);
        }
    }

    private void SendResultsToUser(BufferedReader in, PrintWriter out) {
        out.println(Communication.READYFORRESULTS);
        try {
            Statement s = conn.createStatement();

            //ResultSet res = s.executeQuery("SELECT giornate.giornata as Giornata, sum(votogiocatore.Voto) as Somma FROM (giornate left JOIN formazione on giornate.giornata = formazione.Giornata) left JOIN votogiocatore ON formazione.giornata = votogiocatore.giornata AND formazione.Cognome=votogiocatore.Cognome WHERE userName = '"+currentUser.getUserName()+"' AND formazione.Titolare='1' OR formazione.Titolare is NULL GROUP BY giornata ASC ;");
            ResultSet res = s.executeQuery("SELECT giornate.giornata as Giornata, punteggio as Somma FROM giornate left join punteggi ON giornate.giornata = punteggi.giornata WHERE userName = '"+currentUser.getUserName()+"' or UserName is NULL GROUP BY giornata ASC");
            ArrayList<Results> results = new ArrayList<>();
            while (res.next()){
                results.add(new Results(res.getInt("Giornata"),res.getFloat("Somma")));
            }

            //Send to Client
            if(in.readLine().equals(Communication.READYFORRESULTS)){
                Gson gson = new Gson();
                String resultsString = gson.toJson(results);
                out.println(resultsString);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void SendFileToUser(BufferedReader in, PrintWriter out) {

        File myFile = new File("C:\\Users\\Emanuele\\Desktop\\userPhoto\\"+currentUser.getUserName()+".jpg");
        if(myFile.exists()) {
            out.println(Communication.READYFORIMAGE);
            try {
                if (in.readLine().equals(Communication.READYFORIMAGE)) {
                    try {
                        OutputStream outputStream = socket.getOutputStream();
                        BufferedImage image = ImageIO.read(myFile);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        ImageIO.write(image, "jpg", byteArrayOutputStream);
                        byteArrayOutputStream.flush();

                        byte[] bytes = byteArrayOutputStream.toByteArray();
                        byteArrayOutputStream.close();

                        System.out.println("Sending image to client. :" + bytes.length);
                        DataOutputStream dos = new DataOutputStream(outputStream);

                        dos.writeInt(bytes.length);
                        dos.write(bytes, 0, bytes.length);
                        System.out.println("Image sent to client. ");


                        System.out.println("Transfer Complete");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }else {
            out.println(Communication.NOIMAGE);
        }
    }

    private void PreAuthClient(BufferedReader in, PrintWriter out) {
        try {
            String userString = in.readLine();
            Gson gson = new Gson();
            SimpleUser user = gson.fromJson(userString,SimpleUser.class);
            AuthClient(user.getUserName(),user.getPassword());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void SendUser(BufferedReader in, PrintWriter out) {
        Gson gson = new Gson();
        String userString = gson.toJson(currentUser);
        out.println(userString);
        //sync is used to synchronize client and server
        try {
            String sync = in.readLine();
            if(sync.equals(Communication.CANSENDTEAM)){
                int nextGiornata=0;

                Date minData = new Date(2015,8,15);
                Statement s = conn.createStatement();

                //Find the next day
                ResultSet res = s.executeQuery("SELECT max(Giornata) as maxGiornata FROM votogiocatore");

                while (res.next())
                    nextGiornata = 1 + res.getInt("maxGiornata");

                ResultSet res2 = s.executeQuery("SELECT min(data) as minData FROM calendario WHERE giornata="+nextGiornata+"");

                while (res2.next()){
                    minData = res2.getDate("minData");
                }

                java.util.Date datastr = new java.util.Date(minData.getTime());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = format.format(datastr);
                System.out.println("MinData : "+date);
                out.println(date);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void SendLastDay(BufferedReader in, PrintWriter out) {
        out.println(Communication.READYFORLASTDAY);
        try {
            String line = in.readLine();
            if(line.equals(Communication.READYFORLASTDAY)){
                Statement s = conn.createStatement();
                ArrayList<SimpleTeam> simpleTeams = new ArrayList<>();
                // OLD VERSION
                //ResultSet res = s.executeQuery("SELECT nomeSquadra,userName, sum(Voto) as somma FROM formazione JOIN votogiocatore on formazione.Cognome = votogiocatore.Cognome and formazione.giornata = votogiocatore.giornata WHERE formazione.Titolare=1 GROUP BY userName,nomeSquadra ORDER BY somma DESC ");
                //ResultSet res = s.executeQuery("SELECT client.UserName,client.TeamName, sum(Voto) as somma FROM (client LEFT JOIN formazione on client.Username = formazione.userName and client.TeamName = formazione.nomeSquadra) LEFT JOIN votogiocatore on formazione.Cognome = votogiocatore.Cognome and formazione.giornata = votogiocatore.giornata WHERE (formazione.Titolare=1 OR formazione.Titolare IS NULL) AND formazione.giornata = (SELECT max(giornata) FROM votogiocatore) GROUP BY userName,teamName ORDER BY somma DESC ");
                ResultSet res = s.executeQuery("SELECT UserName,TeamName, punteggi.punteggio as somma FROM punteggi WHERE  punteggi.giornata = (SELECT max(giornata) FROM votogiocatore) GROUP BY userName,teamName ORDER BY somma DESC ");

                while (res.next()){
                    simpleTeams.add(new SimpleTeam(res.getInt("somma"),res.getString("TeamName"),res.getString("UserName")));
                }
                Gson gson = new Gson();
                String classifica = gson.toJson(simpleTeams);
                out.println(classifica);
                System.out.println(classifica);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void SendGiornate(BufferedReader in, PrintWriter out) {
        out.println("READYFORGIORNATE");
        try {
            String line = in.readLine();
            if(line.equals("READYFORGIORNATE")) {
                Statement s = conn.createStatement();
                ResultSet res = s.executeQuery("SELECT max(Giornata) as maxGiornata from votogiocatore");

                int giornata = 0;
                while (res.next()) {
                    giornata = res.getInt("maxGiornata");
                }
                out.println("" + giornata);
                System.out.println("Max giornata = " + giornata);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void SendVoti(BufferedReader in, PrintWriter out) {
        out.println(Communication.READYFORVOTI);
        ArrayList votesPlayer = new ArrayList();
        try {
            String giornata= in.readLine();
            Statement s = conn.createStatement();
            ResultSet res = s.executeQuery("SELECT formazione.Cognome, votogiocatore.Voto, formazione.Titolare, giocatori.Ruolo, formazione.Entrato,formazione.Sostituito FROM (formazione left join votogiocatore on formazione.Cognome = votogiocatore.Cognome and formazione.giornata = votogiocatore.Giornata) JOIN giocatori on formazione.Cognome = giocatori.Cognome and formazione.idGioc=giocatori.id WHERE formazione.userName = '"+currentUser.getUserName()+"' and formazione.nomeSquadra='"+currentUser.getTeamName()+"' and formazione.giornata='"+giornata+"' order by formazione.Titolare DESC");
            while (res.next()){
                votesPlayer.add(new PlayerVoto(res.getString("Ruolo").charAt(0),res.getString("Cognome"),res.getBoolean("Titolare"),res.getFloat("Voto"),res.getBoolean("Entrato"),res.getBoolean("Sostituito")));
            }
            Gson gson = new Gson();
            String toSend = gson.toJson(votesPlayer);
            System.out.println("VOTI: " + toSend);
            out.println(toSend);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ReceiveModifiedTeam(BufferedReader in, PrintWriter out) {
        out.println(Communication.READYFORMODIFIEDTEAM);
        try {
            String team = in.readLine();
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Player>>() {
            }.getType();
            this.team = gson.fromJson(team,listType);
            System.out.println("Team: " + team.toString());
            ModifyTeamInDB();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void ModifyTeamInDB() {
        try {
            Statement s = conn.createStatement();
            //Delete previous team
            s.execute("DELETE FROM squadre WHERE TeamName='"+currentUser.getTeamName()+"' and username='"+currentUser.getUserName()+"'");
            //Insert the correct team
            for(int i = 0; i< team.size(); i++){
                boolean val = s.execute("INSERT INTO squadre(Username,TeamName,Cognome,Pos,idGioc) VALUE ('" + currentUser.getUserName() + "','" + currentUser.getTeamName() + "','" + team.get(i).getCognome() + "','"+team.get(i).getPos()+"','"+team.get(i).getId()+"')");
            }
            //Look for a team for the next day and delete it
            s.execute("DELETE FROM formazione WHERE userName='"+currentUser.getUserName()+"' and nomeSquadra='"+currentUser.getTeamName()+"' and giornata = 1 + (SELECT max(giornata) FROM votogiocatore)");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void SendAllPlayers(BufferedReader in, PrintWriter out) {
        out.println(Communication.READYFORALLPLAYERS);
        try {
            String line = in.readLine();
            if(line.equals(Communication.READYFORALLPLAYERS)) {
                Statement s = conn.createStatement();
                ArrayList allplayers = new ArrayList();
                ResultSet res = s.executeQuery("SELECT * from giocatori");
                while (res.next()){
                    allplayers.add(new Player(res.getString("Cognome"), res.getInt("Costo"), res.getInt("id"), res.getString("Ruolo").charAt(0), res.getString("Squadra")));
                }
                Gson gson = new Gson();
                String toSend = gson.toJson(allplayers);
                out.println(toSend);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void SendClassifica(BufferedReader in, PrintWriter out) {
        out.println(Communication.READYFORCLASSIFICA);
        try {
            String line = in.readLine();
            if(line.equals(Communication.READYFORCLASSIFICA)){
                Statement s = conn.createStatement();
                ArrayList<SimpleTeam> simpleTeams = new ArrayList<>();
                // OLD VERSION
                //ResultSet res = s.executeQuery("SELECT nomeSquadra,userName, sum(Voto) as somma FROM formazione JOIN votogiocatore on formazione.Cognome = votogiocatore.Cognome and formazione.giornata = votogiocatore.giornata WHERE formazione.Titolare=1 GROUP BY userName,nomeSquadra ORDER BY somma DESC ");
                //ResultSet res = s.executeQuery("SELECT client.UserName,client.TeamName, sum(Voto) as somma FROM (client LEFT JOIN formazione on client.Username = formazione.userName and client.TeamName = formazione.nomeSquadra) LEFT JOIN votogiocatore on formazione.Cognome = votogiocatore.Cognome and formazione.giornata = votogiocatore.giornata WHERE formazione.Titolare=1 OR formazione.Titolare IS NULL GROUP BY userName,teamName ORDER BY somma DESC ");
                ResultSet res = s.executeQuery("SELECT UserName, TeamName, sum(punteggio) as somma FROM punteggi GROUP BY UserName,TeamName ORDER BY somma DESC ");
                while (res.next()){
                    //TODO: Gestire le riserve
                    simpleTeams.add(new SimpleTeam(res.getInt("somma"),res.getString("teamName"),res.getString("userName")));
                }
                Gson gson = new Gson();
                String classifica = gson.toJson(simpleTeams);
                out.println(classifica);
                System.out.println(classifica);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void SendUserTeam() {
        PrintWriter out = null;
        try {
            Statement st = conn.createStatement();
            Statement st2 = conn.createStatement();
            //System.out.println(currentUser.getTeam().size());
            ArrayList <Player> players = new ArrayList<>();
            ResultSet res = st.executeQuery("SELECT * FROM squadre WHERE TeamName='" + currentUser.getTeamName() + "' AND username ='"+currentUser.getUserName()+"'");
            while (res.next()) {
                ResultSet playerInfo = st2.executeQuery("SELECT * FROM giocatori WHERE Cognome = '" + res.getString("Cognome") + "' AND id="+res.getInt("idGioc")+"");
                Player player = new Player();
                while (playerInfo.next()){
                     player = new Player(res.getString("Cognome"), playerInfo.getInt("Costo"), playerInfo.getInt("id"), res.getString("Pos"), playerInfo.getString("Ruolo").charAt(0), playerInfo.getString("Squadra"));
            }
                players.add(player);
            }
            out = new PrintWriter(socket.getOutputStream(),true);
            //out.println("Inserimento Avvenuto");
            Gson gson = new Gson();
            String json = gson.toJson(players);
            System.out.println("OK, FATTO");
            out.println(json);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void ReceiveNewTeam(BufferedReader in, PrintWriter out) {
        out.println(Communication.READY);
        try {
            String userJson = in.readLine();
            Gson gson = new Gson();
            User newUser = gson.fromJson(userJson, User.class);
            AddNewUserToDb(newUser,in,out);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void AddNewUserToDb(User newUser, BufferedReader in, PrintWriter out) {
        System.out.println("CONTROLLO :" + newUser.getUserName());
        if(InsertClientToDB(newUser)) {
            //SendEmailToUser();
            InsertTeamToDB(newUser, in, out);

            //Calls a thread that send an email to user
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    SendEmailToUser();
                }
            });
            thread.start();
        }
        else {
            System.out.println("Authno");
            out.println(Communication.AUTHNO);
        }

    }



    private void SendEmailToUser() {
        // Recipient's email ID needs to be mentioned.
        String to = currentUser.getEmail();

        // Sender's email ID needs to be mentioned
        String from = "fantadeveloper@gmail.com";

        // Assuming you are sending email from gmail
        String host = "smtp.gmail.com";

        // Get system properties
        Properties properties = System.getProperties();

        //setup properties
        properties.put("mail.smtp.user",from);
        properties.put("mail.smtp.password", "fantaDeveloper94");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.debug", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable","true");
        properties.put("mail.smtp.EnableSSL.enable","true");


        // Setup mail server
        properties.setProperty("mail.smtp.host", host);

        // Get the default Session object with AUTHENTICATOR. IMPORTANT!
        Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication("fantadeveloper@gmail.com","fantaDeveloper94");
            }
        });

        try{
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("Registrazione Fantacalcio");

            // Now set the actual message
            message.setText("Registrazione avvenuta con successo. Ecco i tuoi dati: user: "+currentUser.getUserName()+" password: "+currentUser.getPassword());



            Transport tr = session.getTransport("smtp");
            tr.connect(session.getProperty("mail.smtp.host"),  session.getProperty("mail.smtp.user"), session.getProperty("mail.smtp.password"));


            tr.sendMessage(message, message.getAllRecipients());
            tr.close();
        }catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    private void InsertTeamToDB(User newUser,BufferedReader in, PrintWriter out) {
        try {
            Statement st = conn.createStatement();
            System.out.println(newUser.getTeam().size());
            for (int i = 0; i < newUser.getTeam().size();i++){
                Player player = newUser.getTeam().get(i);
                boolean val = st.execute("INSERT INTO squadre(Username,TeamName,Cognome,idGioc) VALUE ('" + newUser.getUserName() + "','" + newUser.getTeamName() + "','" + player.getCognome() + "','"+player.getId()+"')");
                System.out.println("Inserito");
                //out = new PrintWriter(socket.getOutputStream(), true);
                //out.println("Autenticazione Avvenuta");
            }
            this.currentUser = newUser;
            GetFileFromUser(in,out);
            System.out.println("Salvataggio file e inserimento avvenuto");
            out.println(Communication.AUTHOK);
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }

    private void GetFileFromUser(BufferedReader in, PrintWriter out) {

        File file;
        out.println(Communication.FILE);
        if(currentUser!=null) {
             file = new File("C:\\Users\\Emanuele\\Desktop\\userPhoto\\" + currentUser.getUserName() + ".jpg");
        }
        else {
             file = new File("C:\\Users\\Emanuele\\Desktop\\userPhoto\\" + "Prova"+ ".jpg");

        }


            if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            InputStream inputStream = socket.getInputStream();
            DataInputStream dis = new DataInputStream(inputStream);

            int len = dis.readInt();
            System.out.println("Image Size: " + len/1024 + "KB");
            byte[] data = new byte[len];
            dis.readFully(data);


            InputStream ian = new ByteArrayInputStream(data);
            BufferedImage bImage = ImageIO.read(ian);

            System.out.println("COMPLETE!");
            ImageIO.write(bImage,"jpg",file);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private void SendAtt() {
        System.out.println("SONO QUA");
        PrintWriter out = null;
        ArrayList<Player> players = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery("SELECT * FROM giocatori WHERE Ruolo='A'");
            while (res.next()) {
                players.add(new Player(res.getString("Cognome"), res.getInt("Costo"), res.getInt("id"), res.getString("Ruolo").charAt(0), res.getString("Squadra")));
            }
            System.out.println("Inserito");
            players.sort(new Comparator<Player>() {
                             @Override
                             public int compare(Player o1, Player o2) {
                                 return ((Player)o1).getCognome().compareTo(((Player)o2).getCognome());
                             }
                         });
                    out = new PrintWriter(socket.getOutputStream(), true);
            Gson gson = new Gson();
            out.println(gson.toJson(players));
            //output.flush();
            //output.close();
            System.out.println(players.toString());
            System.out.println("Fine");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SendCen() {
        System.out.println("SONO QUA");
        PrintWriter out = null;
        ArrayList<Player> players = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery("SELECT * FROM giocatori WHERE Ruolo='C'");
            while (res.next()) {
                players.add(new Player(res.getString("Cognome"), res.getInt("Costo"), res.getInt("id"), res.getString("Ruolo").charAt(0), res.getString("Squadra")));
            }
            System.out.println("Inserito");
            out = new PrintWriter(socket.getOutputStream(), true);
            players.sort(new Comparator<Player>() {
                @Override
                public int compare(Player o1, Player o2) {
                    return ((Player)o1).getCognome().compareTo(((Player)o2).getCognome());
                }
            });
            Gson gson = new Gson();
            out.println(gson.toJson(players));
            //output.flush();
            //output.close();
            System.out.println(players.toString());
            System.out.println("Fine");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SendDef() {
        System.out.println("SONO QUA");
        PrintWriter out = null;
        ArrayList<Player> players = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery("SELECT * FROM giocatori WHERE Ruolo='D'");
            while (res.next()) {
                players.add(new Player(res.getString("Cognome"), res.getInt("Costo"), res.getInt("id"), res.getString("Ruolo").charAt(0), res.getString("Squadra")));
            }
            System.out.println("Inserito");
            players.sort(new Comparator<Player>() {
                @Override
                public int compare(Player o1, Player o2) {
                    return ((Player)o1).getCognome().compareTo(((Player)o2).getCognome());
                }
            });
            out = new PrintWriter(socket.getOutputStream(), true);
            Gson gson = new Gson();
            out.println(gson.toJson(players));
            System.out.println(players.toString());
            System.out.println("Fine");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SendPor() {
        System.out.println("SONO QUA");
        PrintWriter out = null;
        ArrayList<Player> players = new ArrayList<>();
        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery("SELECT * FROM giocatori WHERE Ruolo='P'");
            while(res.next()){
                players.add(new Player(res.getString("Cognome"),res.getInt("Costo"),res.getInt("id"), res.getString("Ruolo").charAt(0),res.getString("Squadra")));
            }
            players.sort(new Comparator<Player>() {
                @Override
                public int compare(Player o1, Player o2) {
                    return ((Player)o1).getCognome().compareTo(((Player)o2).getCognome());
                }
            });
            out = new PrintWriter(socket.getOutputStream(),true);
            Gson gson = new Gson();
            out.println(gson.toJson(players));
            //output.flush();
            //output.close();
            System.out.println(players.toString());
            System.out.println("Fine");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean InsertClientToDB(User user){
        PrintWriter out = null;
        user.setImagePath("C:\\Users\\Emanuele\\Desktop\\userPhoto\\"+user.getUserName()+".jpg");
        System.out.println(user.getImagePath());
        try {
            Statement st = conn.createStatement();
            boolean val = st.execute("INSERT INTO client(Username,Pw,TeamName,email,imagePath,DataNascita) VALUE ('"+user.getUserName()+"','"+user.getPassword()+"','"+user.getTeamName()+"','"+user.getEmail()+"','"+user.getImagePath()+"','"+user.getDataNacita()+"')");
            System.out.println("Inserito");
            out = new PrintWriter(socket.getOutputStream(), true);
            return true;
        }
        catch (SQLException e){
            //TODO: improve
            System.out.println("false");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("false");
        return false;
    }

    private void AuthClient(String user,String pw){
        PrintWriter out = null;
        int resSize=0;
        try {
            Statement st = conn.createStatement();
            ResultSet res = st.executeQuery("SELECT * FROM client WHERE Username='" + user + "' AND Pw='" + pw + "'");
            while (res.next()) {
                if (res.getDate("DataNascita") != null){
                    java.util.Date datastr = new java.util.Date(res.getDate("DataNascita").getTime());
                LocalDate date = datastr.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                this.currentUser = new User(date, res.getString("email"), res.getString("imagePath"), res.getString("Pw"), null, res.getString("TeamName"), res.getString("Username"));

                }
                else {
                    Date date = new Date(2015,8,15);
                    LocalDate localDate = date.toLocalDate();
                    this.currentUser = new User(localDate, res.getString("email"), res.getString("imagePath"), res.getString("Pw"), null, res.getString("TeamName"), res.getString("Username"));
                }
                resSize++;
            }

            if(resSize>0 && !AlreadyAuth(currentUser)) {
                System.out.println("Client Authorized");
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println(Communication.AUTHOK);
                mainInstance.addUser(currentUser,out);
            }
            else  {
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println(Communication.AUTHNO);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }


    //returns true if the user is already authenticated

    private boolean AlreadyAuth(User currentUser) {
        ArrayList<UserSocket> user = mainInstance.getUserArrayList();
        System.out.println("Size"+user.size());
        for (int i = 0; i<user.size();i++){
            if(user.get(i).getUser().getUserName().equals(currentUser.getUserName())){
                return true;
            }
        }
        return false;
    }

    public void getTitolari(BufferedReader in, PrintWriter out) {
        out.println(Communication.READYFORTIT);
        try {
            String stringTitolari = in.readLine();
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Player>>() {
            }.getType();
            this.titolari = gson.fromJson(stringTitolari,listType);
            System.out.println("Titolari: " + titolari.toString());

            out.println(Communication.READYFORRIS);
            String stringRiserve = in.readLine();
            this.riserve = gson.fromJson(stringRiserve,listType);
            System.out.println("Riserve: "+riserve.size());

            InsertTitolariToDB();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void InsertTitolariToDB() {
        try {
            Statement st = conn.createStatement();
            ResultSet resultSet = st.executeQuery("SELECT MAX(Giornata) AS maxGiornata FROM votogiocatore");
            resultSet.next();
            int giornata = resultSet.getInt("maxGiornata");
            giornata++;
            //Delete previous team
            st.execute("DELETE FROM formazione WHERE giornata="+giornata+" and nomeSquadra='"+currentUser.getTeamName()+"' AND userName='"+currentUser.getUserName()+"'");

            //Insert team
            for(int i = 0; i < titolari.size(); i ++){
                st.execute("INSERT INTO formazione(giornata, userName, nomeSquadra, Cognome, Titolare,idGioc) VALUE ("+giornata+",'"+currentUser.getUserName()+"','"+currentUser.getTeamName()+"','"+titolari.get(i).getCognome()+"',TRUE,"+titolari.get(i).getId()+")");
                //Insert pos into db
                st.execute("UPDATE squadre SET squadre.Pos='"+titolari.get(i).getPos()+"' WHERE squadre.username = '"+currentUser.getUserName()+"' AND squadre.TeamName ='"+currentUser.getTeamName()+"' AND squadre.idGioc = "+titolari.get(i).getId()+"");
            }


            /** Find Riserve **/
            /*
            ResultSet res = st.executeQuery("SELECT squadre.Cognome FROM squadre WHERE squadre.username='"+currentUser.getUserName()+"' AND squadre.TeamName='"+currentUser.getTeamName()+"' AND squadre.Cognome NOT IN (SELECT formazione.Cognome FROM formazione WHERE formazione.giornata="+giornata+" AND formazione.userName='"+currentUser.getUserName()+"' AND formazione.nomeSquadra='"+currentUser.getTeamName()+"')");
            while (res.next()){
                riserve.add(res.getString("Cognome"));
            }
            */

            for (int i = 0; i<riserve.size(); i++) {
                //Insert riserve into db
                st.execute("INSERT INTO formazione(giornata, userName, nomeSquadra, Cognome, Titolare,PosRiserva,idGioc) VALUE (" + giornata + ",'" + currentUser.getUserName() + "','" + currentUser.getTeamName() + "','" + riserve.get(i).getCognome() + "',FALSE,'"+riserve.get(i).getPos()+"',"+riserve.get(i).getId()+" )");
                //Insert pos into db
                st.execute("UPDATE squadre SET squadre.Pos='"+riserve.get(i).getPos()+"' WHERE squadre.username = '"+currentUser.getUserName()+"' AND squadre.TeamName ='"+currentUser.getTeamName()+"' AND squadre.idGioc = "+riserve.get(i).getId()+"");
            }

            //Find Tribuna
            ArrayList<Integer> arrayList = new ArrayList<>();
            ResultSet res = st.executeQuery("SELECT squadre.Cognome,squadre.idGioc FROM squadre WHERE squadre.username='"+currentUser.getUserName()+"' AND squadre.TeamName='"+currentUser.getTeamName()+"' AND squadre.idGioc NOT IN (SELECT formazione.idGioc FROM formazione WHERE formazione.giornata="+giornata+" AND formazione.userName='"+currentUser.getUserName()+"' AND formazione.nomeSquadra='"+currentUser.getTeamName()+"')");
            while (res.next()){
                arrayList.add(res.getInt("idGioc"));
            }

            for (int i = 0; i< arrayList.size(); i++){
                //Insert pos into db
                st.execute("UPDATE squadre SET squadre.Pos='"+0+"' WHERE squadre.username = '"+currentUser.getUserName()+"' AND squadre.TeamName ='"+currentUser.getTeamName()+"' AND squadre.idGioc = "+arrayList.get(i).intValue()+"");
            }


            System.out.println("TITOLARI "+titolari.toString());
            System.out.println("Riserve "+ riserve.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
