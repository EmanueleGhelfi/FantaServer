package Main; /**
 * Created by Emanuele on 15/10/2015.
 */

import Constants.SecurityClass;
import Model.CommunicationInfo;
import Model.PlayerVoto;
import Model.User;
import Model.UserSocket;
import com.google.gson.Gson;
import javafx.concurrent.Task;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.*;
import java.net.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server extends Task<Void> {

    //public static int mossa;

    private ArrayList<UserSocket> userArrayList = new ArrayList<>();
    private int portNumber;
    private ServerSocket serverSocket;
    private ArrayList<Socket> socketArray = new ArrayList<>();

    public Server() {
        userArrayList = new ArrayList<>();
        portNumber = 4444;
        socketArray = new ArrayList<>();
    }

    @Override
    protected Void call() throws Exception {

        serverSocket = null;
        Socket clientSocket = null;
        int number = 0;
        Connection conn;
        System.out.println("SONO il thread");

        try {
            serverSocket = new ServerSocket(portNumber);

            //Server connect to MySQL Server
            conn = ConnectToDB();

            //Ogni ora cerca di scaricare le nuove giornate se ce ne sono e aggiorna il DB dei giocatori
            Timer timer = new Timer();
            TimerTask hourlyTask = new TimerTask() {
                @Override
                public void run() {
                    InsertPlayerToDB(conn);
                    updateVoti(conn);

                }
            };

            // schedule the task to run starting now and then every hour...
            timer.schedule(hourlyTask, 0l, 1000 * 60 * 60);

            while (true) {
                try {
                    // wait a connection
                    clientSocket = serverSocket.accept();
                    number++;
                    //create and starts a CommunicationThread, a thread wich communicate with the client
                    new CommunicationThread(clientSocket, number, conn, this).start();
                    socketArray.add(clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }



    public ArrayList<UserSocket> getUserArrayList() {
        return userArrayList;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public ArrayList<Socket> getSocketArray() {
        return socketArray;
    }

    public void setSocketArray(ArrayList<Socket> socketArray) {
        this.socketArray = socketArray;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public Server(ArrayList<UserSocket> userArrayList) {
        this.userArrayList = userArrayList;
    }

    public void setUserArrayList(ArrayList<UserSocket> userArrayList) {
        this.userArrayList = userArrayList;
    }

    private static void DownloadCalendar(Connection conn) {
        try {
        Statement s = conn.createStatement();
        for (int i = 1; i<39;i++){
            Document doc = null;

                doc = Jsoup.connect("http://www.legaseriea.it/it/serie-a-tim/calendario-e-risultati/2015-16/UNICO/UNI/"+i).get();
                Elements name = doc.getElementsByClass("datipartita");
                for (int k =0; k<name.size();k++){
                    Element element = name.get(k);
                    String string = element.text();
                    System.out.println("Non Pulita: " + string);
                    System.out.println("Pulita: "+ string.substring(0,16));

                    String dateStr = string.substring(0,16).replaceAll("/","-");

                    //Format date with sql syntax
                    SimpleDateFormat newdateformat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    SimpleDateFormat olddateformat= new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

                    java.util.Date date = olddateformat.parse(dateStr+":00");
                    String newDateStr = newdateformat.format(date);

                    boolean val = s.execute("INSERT INTO calendario(giornata,data) VALUE (" + i + ",'" + newDateStr+ "')");

                }
        }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void updateVoti(Connection conn) {
        int giornata = 0;
        //Dice se ho trovato una nuova giornata
        boolean findNewDay=false;
        System.out.println("STO CERCANDO UPDATE...");
        try {
            Statement s = conn.createStatement();
            //Cerco l'ultima giornata presente nel DB
            ResultSet res = s.executeQuery("SELECT MAX(Giornata) AS maxGiornata FROM votogiocatore");
            boolean needToContinue = true;
            res.next();
            giornata = res.getInt("maxGiornata");
            //Devo scaricare la giornata successiva
            giornata++;
            /* Ciclo che a partire dalla giornata corrente cerca i voti di tutte le giornate successive, se presenti.
                Appena trova ua pagina senza nomi si ferma perchè non ci sono giornate successive.
             */
            while (needToContinue) {
                Document doc = Jsoup.connect("http://www.gazzetta.it/calcio/fantanews/voti/serie-a-2015-16/giornata-"+giornata).get();
                Elements name = doc.getElementsByClass("playerNamein");
                Elements role = doc.getElementsByClass("playerRole");
                if(name.size()==0){
                    //Se non sono presenti voti non devo più andare avanti nel download
                    needToContinue=false;
                }
                else {
                    //Ho trovato una nuova giornata, devo aggiornare i voti e la classifica
                    findNewDay=true;
                    Elements voto = doc.getElementsByClass("fvParameter");
                    for (int k = 0; k < voto.size(); k++) {
                        Element element = voto.get(k);
                        String string = element.text();
                        if (!string.equals("FV")) {
                            //Non faccio niente
                        } else {
                            //Rimuovo il voto siccome non è il vero voto ma è un testo
                            voto.remove(k);
                        }
                    }
                    //Controllo dimensione degli array
                    System.out.println("Name dopo" + name.size());
                    System.out.println("Voti dopo " + voto.size());
                    //Indice nell'array dei ruoli, siccome ogni ruolo è ripetuto due volte di seguito
                    int k =0;
                    // Inserimento nel DB. Solo metà array siccome i nomi sono ripetuti 2 volte
                    for (int j = 0; j < name.size()/2; j++) {

                        String votoStringa = voto.get(j).text();
                        if(votoStringa.equals("-") || votoStringa.equals("FV"))
                                votoStringa="0";
                        float votoNumero = Float.parseFloat(votoStringa);
                        //Prendo il ruolo successivo se è più lungo di un carattere
                        String trueRole = role.get(k).text();
                        while (trueRole.length()>1){
                            k++;
                            trueRole=role.get(k).text();
                        }
                        //Correzione ruolo, se è T imposto C
                        if(trueRole.equals("T")){
                            trueRole="C";
                        }

                        boolean val = s.execute("INSERT INTO votogiocatore(Cognome,Giornata,Voto,role) VALUE ('" + name.get(j).text() + "'," + giornata + "," + votoNumero + ",'"+trueRole+"')");
                        k++;
                    }
                    // Aggiorno l'id dei giocatori nella tabella votogiocatore
                    s.execute("UPDATE votogiocatore,giocatori set votogiocatore.idGioc=giocatori.id where votogiocatore.Cognome = giocatori.Cognome and votogiocatore.role=giocatori.Ruolo");
                    //Passo alla giornata successiva
                    giornata++;
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(findNewDay) {
            UpdateViewClassifica(giornata - 1, conn);
            //Manda notica e email dei nuovi voti
            SendNotification();
        }
    }


    public void SendNotification() {
        for(int i = 0; i< userArrayList.size();i++){
            SendCommunicationInfo(userArrayList.get(i).getOut(),"NOTIFICATION","");
            //userArrayList.get(i).getOut().println("NOTIFICATION");
        }
    }

    private void UpdateViewClassifica(int giornata, Connection conn) {
        int currentDay=0;
        try {
            Statement s0 = conn.createStatement();
            //Seleziono la giornata massima
            ResultSet res0 = s0.executeQuery("SELECT max(giornata) as giornata from punteggi");
            while (res0.next()){
                if(res0.getString("giornata")==null || res0.getString("giornata").equals("null"))
                    currentDay=0;
                else
                    currentDay = Integer.parseInt(res0.getString("giornata"));
            }
            //aggiorno i punteggi da currentDay +1 fino a giornata che viene passata a questa funzione
            for (int i =currentDay+1;i<=giornata;i++){
                //Seleziono tutti i giocatori per ogni giornata
                Statement s = conn.createStatement();
                Statement s2 = conn.createStatement();
                ResultSet res = s.executeQuery("SELECT DISTINCT Username,TeamName FROM client");
                ArrayList<String> users = new ArrayList<>();
                ArrayList<String> teams = new ArrayList<>();

                while (res.next()){
                    users.add(res.getString("Username"));
                    teams.add(res.getString("TeamName"));
                }

                for(int j=0;j<users.size();j++){
                    //Ottengo la squadra per ogni utente
                    ResultSet resTeam = s2.executeQuery("SELECT formazione.Cognome, Titolare,PosRiserva,votogiocatore.Voto, " +
                            "giocatori.Ruolo FROM (formazione left JOIN votogiocatore on " +
                            "formazione.Cognome = votogiocatore.Cognome AND formazione.giornata = votogiocatore.Giornata" +
                            " and formazione.idGioc=votogiocatore.idGioc) JOIN giocatori on " +
                            "formazione.Cognome = giocatori.Cognome and formazione.idGioc=giocatori.id " +
                            "WHERE formazione.giornata='"+i+"' and userName ='"+users.get(j)+"'");
                    ArrayList<PlayerVoto> players = new ArrayList<>();
                    while (resTeam.next()){
                        players.add(new PlayerVoto(resTeam.getString("Ruolo").charAt(0),resTeam.getString("Cognome"),resTeam.getBoolean("Titolare"),resTeam.getFloat("Voto"),resTeam.getString("PosRiserva")));
                    }
                    //Calcolo reale dei punti
                    CalculatePoint(users.get(j),teams.get(j),i,players,conn);
                }
            }
            System.out.println("FINE CALCOLO PUNTI");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void CalculatePoint(String user, String team, int giornata, ArrayList<PlayerVoto> players, Connection conn) {
        ArrayList<PlayerVoto> titolari = new ArrayList<>();
        int contSostituzioni=0;
        float somma = 0;
        String posGiusta= "";
        String posGiusta2 = "";
        boolean present = false;
        System.out.println(" "+user+" " +team+" giornata "+ giornata);
        for (int i = 0; i< players.size(); i++){
            if(players.get(i).isTitolare()){
                titolari.add(players.get(i));
                try {
                    Statement statement2;
                    statement2 = conn.createStatement();
                    statement2.execute("UPDATE formazione SET Sostituito='0' WHERE formazione.Cognome ='"+players.get(i).getCognome()+"' and formazione.giornata='"+giornata+"' AND formazione.userName = '"+user+"'");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else {
                Statement statement3 = null;
                try {
                    statement3 = conn.createStatement();
                    statement3.execute("UPDATE formazione SET Entrato='0' WHERE formazione.Cognome ='"+players.get(i).getCognome()+"' and formazione.giornata='"+giornata+"' AND formazione.userName = '"+user+"'");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int j = 0; j<titolari.size();j++){
            //se non ha giocato
            if(titolari.get(j).getVoto()==0.0 && contSostituzioni<3){
                switch (titolari.get(j).getRuolo()){
                    case 'P':
                        posGiusta="PP";
                        posGiusta2 = "PP";
                        break;
                    case 'D':
                        posGiusta="PD1";
                        posGiusta2="PD2";
                        break;
                    case 'C':
                        posGiusta = "PC1";
                        posGiusta2 = "PC2";
                        break;
                    case 'A':
                        posGiusta="PA1";
                        posGiusta2="PA2";
                        break;
                }
                //Search with the first position
                for (int i = 0; i< players.size(); i++){
                    if(!players.get(i).isTitolare() && (players.get(i).getPosizione())!= null && (players.get(i).getPosizione()).equals(posGiusta) && players.get(i).getVoto()!=0.0 ){
                        present = true;


                        //System.out.println("Entrato :" +players.get(i).getCognome() +" voto: "+players.get(i).getVoto()+" Pos: "+players.get(i).getPosizione());
                        //Update db with substitution
                        try {
                            Statement statement4 = conn.createStatement();
                            statement4.execute("UPDATE formazione SET Entrato='1' WHERE formazione.Cognome ='"+players.get(i).getCognome()+"' and formazione.giornata='"+giornata+"' AND formazione.userName = '"+user+"'");
                            statement4.execute("UPDATE formazione SET Sostituito='1' WHERE formazione.Cognome ='"+titolari.get(j).getCognome()+"' and formazione.giornata='"+giornata+"' AND formazione.userName = '"+user+"'");
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        somma = somma+players.get(i).getVoto();
                        players.remove(i);
                        contSostituzioni++;
                        //System.out.println("sostituz : "+contSostituzioni);

                    }
                }
                //Search with the second position
                if(!present){
                    for (int i = 0; i< players.size(); i++){
                        if(!players.get(i).isTitolare() && players.get(i).getPosizione()!=null && (players.get(i).getPosizione()).equals(posGiusta2) && players.get(i).getVoto()!=0.0 ){
                            present = true;
                            //System.out.println("Entrato :" +players.get(i).getCognome() +" voto: "+players.get(i).getVoto()+" Pos: "+players.get(i).getPosizione());
                            somma = somma+players.get(i).getVoto();
                            //Update db with substitution
                            try {
                                Statement statement5 = conn.createStatement();
                                statement5.execute("UPDATE formazione SET Entrato='1' WHERE formazione.Cognome ='"+players.get(i).getCognome()+"' and formazione.giornata='"+giornata+"' AND formazione.userName = '"+user+"'");
                                statement5.execute("UPDATE formazione SET Sostituito='1' WHERE formazione.Cognome ='"+titolari.get(j).getCognome()+"' and formazione.giornata='"+giornata+"' AND formazione.userName = '"+user+"'");
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            players.remove(i);
                            contSostituzioni++;
                            //System.out.println("sostituz : "+contSostituzioni);
                        }
                    }
                    present=false;
                }
                present=false;

            }
            else {

                somma = somma+titolari.get(j).getVoto();
                present=false;
            }
        }

        //Insert into DB
        try {
            Statement s6 = conn.createStatement();
            s6.execute("INSERT INTO punteggi(giornata, UserName, TeamName, punteggio) VALUE ('"+giornata+"','"+user+"','"+team+"','"+somma+"')");
            //System.out.println("Squadra "+team+" ha realizzato "+somma+"la giornata "+giornata);
            SendEmail(giornata,user, team,somma,conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void SendEmail(int giornata, String user, String team, float somma,Connection conn) {
        String receiver = null;
        try {
            Statement s = conn.createStatement();
            ResultSet res = s.executeQuery("SELECT email from client WHERE Username='" + user + "'");
            while (res.next()) {
                receiver = res.getString("email");
            }
            if (receiver != null) {
                // Recipient's email ID needs to be mentioned.
                String to = receiver;

                // Sender's email ID needs to be mentioned
                String from = SecurityClass.emailUser;

                // Assuming you are sending email from gmail
                String host = "smtp.gmail.com";

                // Get system properties
                Properties properties = System.getProperties();

                //setup properties
                properties.put("mail.smtp.user", from);
                properties.put("mail.smtp.password", SecurityClass.emailPassword);
                properties.put("mail.smtp.host", host);
                properties.put("mail.smtp.port", "587");
                properties.put("mail.debug", "true");
                properties.put("mail.smtp.auth", "true");
                properties.put("mail.smtp.starttls.enable", "true");
                properties.put("mail.smtp.EnableSSL.enable", "true");


                // Setup mail server
                properties.setProperty("mail.smtp.host", host);

                // Get the default Session object with AUTHENTICATOR. IMPORTANT!
                Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
                    @Override
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication(SecurityClass.emailUser, SecurityClass.emailPassword);
                    }
                });

                try {

                    // Create a default MimeMessage object.
                    MimeMessage message = new MimeMessage(session);

                    // Set From: header field of the header.
                    message.setFrom(new InternetAddress(from));

                    // Set To: header field of the header.
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

                    // Set Subject: header field
                    message.setSubject("Voti della "+giornata+" disponibili!");

                    // Now set the actual message
                    message.setText("Ciao "+user+"! \n I voti della "+ giornata+" giornata sono ora disponibili sull'applicazione! La tua squadra "+ team+" ha totalizzato "+somma+" punti! COMPLIMENTI!!! \n Email generata in automatico, non rispondere a questa email.");


                    Transport tr = session.getTransport("smtp");
                    tr.connect(session.getProperty("mail.smtp.host"), session.getProperty("mail.smtp.user"), session.getProperty("mail.smtp.password"));


                    tr.sendMessage(message, message.getAllRecipients());
                    tr.close();
                } catch (MessagingException mex) {
                    mex.printStackTrace();
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static Connection ConnectToDB(){
        String url ="jdbc:mysql://localhost:3306/dbfirst";
        String driver = "com.mysql.jdbc.Driver";

        String userName = SecurityClass.dbUser;
        String password = SecurityClass.dbPassword;

        try {
            Class.forName(driver).newInstance();
            Connection conn = DriverManager.getConnection(url, userName, password);
            return conn;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void InsertPlayerToDB(Connection conn) {

        try {
            //Download HTML
            Document doc = Jsoup.connect("http://www.gazzetta.it/calcio/fantanews/statistiche/serie-a-2015-16/all").get();
            java.sql.Statement st = conn.createStatement();
            Elements name = doc.getElementsByClass("field-giocatore");
            Elements team = doc.getElementsByClass("field-sqd");
            Elements price = doc.getElementsByClass("field-q");
            Elements role = doc.getElementsByClass("field-ruolo");
            //Imposto a 0 l'attributo presente di tutti i giocatori. In questo modo riconosco i giocatori non più presenti
            st.execute("UPDATE giocatori set presente='0'");
            for (int i = 0; i< name.size()-1; i++) {
                String ruolo = role.get(i+1).text().substring(0,1);
                //provo a inserire nel db
                if(role.get(i+1).text().substring(0,1).equals("T")){
                    ruolo="C";
                }
                try {
                    //Provo a inserirlo, se non ci sono eccezioni lo inserisco normalmente
                    boolean val = st.execute("INSERT INTO giocatori (Cognome,Squadra,Costo,Ruolo,presente)  VALUE ('"+name.get(i+1).text()+"','"+team.get(i+1).getElementsByClass("hidden-team-name").get(0).text()+"','"+price.get(i+1).text()+"','"+ruolo+"','1')");
                    if (val)
                        System.out.println("OK, inserito: "+ name.get(i+1).text());
                }
                catch (SQLException e){
                    System.out.println("Già presente: "+name.get(i+1).text());
                    //Se è già presente aggiorno i suoi parametri
                    boolean retry = st.execute("UPDATE giocatori set Squadra='"+team.get(i+1).getElementsByClass("hidden-team-name").get(0).text()+"', Costo='"+price.get(i+1).text()+"', presente='1' WHERE Cognome='"+name.get(i+1).text()+"' AND Ruolo='"+ruolo+"'");
                    if(retry)
                        System.out.println("Modificato");
                }

            }

            //Stampo giocatori aggiornati per controllo
            ResultSet res = st.executeQuery("SELECT * FROM giocatori");
            while (res.next()) {
                int id = res.getInt("id");
                String msg = res.getString("Cognome");
                String squadra = res.getString("Squadra");
                boolean presente = res.getBoolean("presente");
                System.out.println(id + "\t" + msg+"\t"+squadra+"\t"+presente); }
            //Aggiorno soldi client che possiede un giocatore non più presente
            st.execute("UPDATE client cl,(SELECT sum(Costo) as costo, squadre.username as tempUser FROM giocatori join squadre on giocatori.id = squadre.idGioc WHERE giocatori.presente = FALSE GROUP by squadre.username) temp set cl.Soldi= cl.Soldi+costo WHERE cl.Username = temp.tempUser");
            //Cancello giocatore dalla squadra corrispondente
            st.execute("DELETE squadre FROM squadre JOIN giocatori on squadre.idGioc = giocatori.id WHERE giocatori.presente = FALSE");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("NO connection");
            e.printStackTrace();
        }
    }

    public static void downloadVoti(Connection conn){
        try {
            Statement st = conn.createStatement();

        for (int i = 1; i < 11; i ++) {
            try {
                Document doc = Jsoup.connect("http://www.gazzetta.it/calcio/fantanews/voti/serie-a-2015-16/giornata-" + i).get();
                Elements name = doc.getElementsByClass("playerNamein");
                System.out.println("Name " + name.size());
                //Elements voto = doc.getElementsByClass("inParameter fvParameter");
                Elements voto = doc.getElementsByClass("fvParameter");
                System.out.println("Voti " + voto.size());

                //Cancel wrong result

                for (int k = 0; k < voto.size(); k++) {
                    Element element = voto.get(k);
                    String string = element.text();
                    if (!string.equals("FV")) {
                        //nothing
                    } else {
                        System.out.println("TROVATO UNO");
                        voto.remove(k);
                    }
                }

                    System.out.println("Name dopo" + name.size());
                    System.out.println("Voti dopo " + voto.size());

                    // Insert into DB
                    for (int j = 0; j < name.size()/2 - 1; j++) {
                        String votoStringa = voto.get(j).text();
                        if(votoStringa.equals("-"))
                            votoStringa="0";
                        float votoNumero = Float.parseFloat(votoStringa);
                        boolean val = st.execute("INSERT INTO votogiocatore(Cognome,Giornata,Voto) VALUE ('" + name.get(j).text() + "'," + i + "," + votoNumero + ")");

                    }
                    System.out.println("GIORNATA Scaricata " + i);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public void addUser(User currentUser,PrintWriter out) {
        System.out.println("Sto aggiungendo: "+currentUser.getUserName());
        userArrayList.add(new UserSocket(out,currentUser));
    }

    public void removeUser(User currentUser,PrintWriter out) {
        System.out.println("Sto rimuovendo: "+currentUser.getUserName());
        UserSocket userSocket = new UserSocket(out,currentUser);
       for(int i = 0; i< userArrayList.size();i++){
           if(userArrayList.get(i).getUser().getUserName().equals(currentUser.getUserName())){
               userArrayList.remove(i);
           }
       }
    }

    public void SendCommunicationInfo(PrintWriter out, String code, String toSend ) {
        CommunicationInfo communicationInfo = new CommunicationInfo(code,toSend);
        Gson gson = new Gson();
        String communicationToSend = gson.toJson(communicationInfo);
        out.println(communicationToSend);
    }
}
