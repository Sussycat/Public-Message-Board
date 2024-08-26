/**
 * Assignment 2
 * Hung Nguyen, Cat Luong, Chau Nguyen
 **/

import java.io.*;
import java.net.* ;
import java.util.* ;
import java.time.*;
import WebServer.*;

public final class WebServer {

    public static void main(String argv[]) throws Exception {
        // Set the port number
        //int port = 5000;
        int port = 6789;
        int UID = 0;
        // Establish the listen socket
        ServerSocket Lsocket = new ServerSocket(port);
        // Establish the list of thread to talk to clients
        ArrayList<ServerThread> Clients = new ArrayList<>();
        // Establish the list of messages
        ArrayList<Message> Mrepo = new ArrayList<>();
        // Process TCP requests in an infinite loop.
        while (true) {
            // Listen for a TCP connection request.
            Socket TCPsocket = Lsocket.accept();
            //Generate an unique User ID
            while (containsID(Clients,UID)){
                UID++;
                if (UID > 2000000000){//Maximum number of users
                    UID = 0;
                }
            }
            // Construct an object to act as socket to connect to user
            ServerThread Sthread = new ServerThread(TCPsocket,Clients,Mrepo,UID);
            // Add the thread to list of threads
            Clients.add(Sthread);
            // Start the thread;
            Sthread.start();
        }
    }
    //Check if list of groups contains user with UID
    static boolean containsID(ArrayList<ServerThread> Clients, Integer UID) {
        return Clients.stream().anyMatch(p -> p.getID().equals(UID));
    }
}

final class ServerThread extends Thread {
    private ArrayList<ServerThread> Clients;
    private ArrayList<Message> Mrepo;
    final static String CRLF = "\r\n";
    private Socket Csocket;
    private Boolean status;
    public int ID;
    private ArrayList<Integer> groups;
    public DataOutputStream os;
    public BufferedReader br;
    private String delim = ";;";

    public ServerThread(Socket socket, ArrayList<ServerThread> CList, ArrayList<Message> Mr, int UID) throws Exception {
        this.Csocket = socket;
        this.Clients = CList;
        this.ID = UID;
        this.Mrepo = Mr;
        this.groups = new ArrayList<>();
        this.status = true;
        // Set up output stream filters.
        this.os = new DataOutputStream(Csocket.getOutputStream());
        // Set up input stream filters.
        this.br = new BufferedReader(new InputStreamReader(Csocket.getInputStream()));
    }
    @Override
    public void run(){
        try {
            int GID, MID;
            String Sub, Body;
            Message M;
            String cmd;

            while (status) {
                // Get the request line of client message
                String requestLine = br.readLine();

                // Display the request line.
                System.out.println("User " + ID + " request: " + requestLine);
                try{
                //Split string using delimiter ;;
                StringTokenizer tokens = new StringTokenizer(requestLine,delim);
                if (requestLine == ""){
                    cmd = "";
                }else {
                    cmd = tokens.nextToken();
                }
                //Handle each user inputted command
                    switch (cmd) {
                        case "%join":
                            System.out.println("User " + ID + " is joining public group");
                            Join(0);
                            break;
                        case "%post":
                            System.out.println("User " + ID + " is posting to public group");
                            Sub = tokens.nextToken();
                            Body = tokens.nextToken();
                            Post(Sub, Body, 0);
                            break;
                        case "%users":
                            System.out.println("User " + ID + " is requesting user list of public group");
                            Users(0);
                            os.writeBytes(CRLF);
                            break;
                        case "%leave":
                            System.out.println("User " + ID + " is leaving public group");
                            Leave(0);
                            os.writeBytes(CRLF);
                            break;
                        case "%message":
                            System.out.println("User " + ID + " is requesting message content");
                            MID = Integer.parseInt(tokens.nextToken());
                            MSS(MID, 0);
                            break;
                        case "%exit":
                            System.out.println("User " + ID + " is exiting");
                            Exit();
                            return;
                        case "%groups":
                            System.out.println("User " + ID + " is requesting list of private groups");
                            os.writeBytes("200 OK");
                            os.writeBytes("List of group ids: 1,2,3,4,5");
                            os.writeBytes(CRLF);
                            break;
                        case "%groupjoin":
                            System.out.println("User " + ID + " is joining private group");
                            GID = Integer.parseInt(tokens.nextToken());
                            Join(GID);
                            break;
                        case "%grouppost":
                            System.out.println("User " + ID + " is posting to private group");
                            GID = Integer.parseInt(tokens.nextToken());
                            Sub = tokens.nextToken();
                            Body = tokens.nextToken();
                            Post(Sub, Body, GID);
                            break;
                        case "%groupusers":
                            System.out.println("User " + ID + " is requesting user list of public group");
                            GID = Integer.parseInt(tokens.nextToken());
                            Users(GID);
                            os.writeBytes(CRLF);
                            break;
                        case "%groupleave":
                            System.out.println("User " + ID + " is leaving private group");
                            GID = Integer.parseInt(tokens.nextToken());
                            Leave(GID);
                            os.writeBytes(CRLF);
                            break;
                        case "%groupmessage":
                            System.out.println("User " + ID + " is requesting private message content");
                            GID = Integer.parseInt(tokens.nextToken());
                            MID = Integer.parseInt(tokens.nextToken());
                            MSS(MID, GID);
                            break;
                        default:
                            InvalidCommand();
                            break;
                    }
                } catch (Exception e) {
                    InvalidCommand();
                    System.out.println(e);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Terminating connection with User " + ID);
            SClose();
        }
    }
    //Close socket and its streams them remove this client from list
    private void SClose(){
        try {
            this.os.close();
            this.br.close();
            Csocket.close();
        } catch (Exception e){
            System.out.println(e);
        }
        Clients.remove(this);
    }
    private void InvalidCommand() throws IOException {
        System.out.println("User " + ID + " is inputting invalid commands");
        os.writeBytes("You have inputted an invalid command");
        os.writeBytes(CRLF);
    }

    //Output message to all but current client, if boolean all is true then send to all other connected clients
    private void BroadcastOthers(String msg, int GID, Boolean All) throws IOException {
        for (ServerThread C: Clients){
            if (C.ID != this.ID && (All || C.groups.contains(Integer.valueOf(GID)))){
                C.os.writeBytes(msg);
            }
        }
    }
    //Output message to all clients, if boolean all is true then send to all connected clients
    private void BroadcastAll(String msg, int GID, Boolean All) throws IOException {
        for (ServerThread C: Clients){
            if(All || C.groups.contains(Integer.valueOf(GID))) {
                C.os.writeBytes(msg);
            }
        }
    }
    //Get message with MID
    private Message MGet(int MID){
        for (Message M : Mrepo){
            if(M.MID == MID){
                return M;
            }
        }
        return null;
    }
    // Send N most recent messages from the group to user
    private void PRMes(int GID, int N) throws Exception{
        int i = 0;
        String outp;
        for (Message M : Mrepo){
            if(M.GID == GID){
                outp = delim + M.MID + ", " + M.UID + ", " + M.Pdate + ", " + M.Sub;
                os.writeBytes(outp);
                i++;
            }
            if (i == N){
                break;
            }
        }
    }
    //Check if user is in group GID
    private int IGroup(int GID){
        if (this.groups.contains(Integer.valueOf(GID))){
            if (GID == 0){
                return 2; //User is in public group
            }
            return 3; //User is in private group
        } else if (GID > 5 || GID < 0) {
            return 4; //Group not exist
        }else if(GID == 0){
            return 0; //User not in public group
        }else {
            return 1; //User not in private group
        }
    }
    //Create a message and store in repository
    public synchronized Message CMess(String Sub, String Body, int GID){
        LocalDate localDate = LocalDate.now(); // Get the current date
        Date Pdate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        int MID = Mrepo.size();// Get the ID of the message
        //Create message, add to repository then return message
        Message M = new Message(MID,this.ID,GID,Sub,Body,Pdate);
        Mrepo.add(M);
        return M;
    }
    //Make user join group
    private boolean Join(int GID) throws Exception{
        switch (IGroup(GID)){
            case 0:
                os.writeBytes("You have joined public group");
                groups.add(Integer.valueOf(GID));
                os.writeBytes(delim);
                Users(GID); //Get list of group users
                PRMes(GID,2); // Print 2 most recent group message
                os.writeBytes(CRLF);
                BroadcastOthers("User " + ID + " is joining public group" + CRLF,GID,true);
                return true;
            case 1:
                os.writeBytes("You have joined private group " + GID);
                groups.add(Integer.valueOf(GID));
                os.writeBytes(delim);
                Users(GID); //Get list of group users
                PRMes(GID,2); // Print 2 most recent group message
                os.writeBytes(CRLF);
                BroadcastOthers("User " + ID + " is joining private group " + GID + CRLF,GID,false);
                return true;
            case 2:
            case 3:
                os.writeBytes("You are already in this group");
                os.writeBytes(CRLF);
                return false;
            default:
                os.writeBytes("Invalid Group ID");
                os.writeBytes(CRLF);
                return false;
        }
    }

    //Post a message to all clients in the group
    private boolean Post(String Sub, String Body, int GID) throws IOException{
        switch (IGroup(GID)){
            case 0:// User is not in group
                os.writeBytes("You must join public group to post");
                os.writeBytes(CRLF);
                return false;
            case 1:
                os.writeBytes("You must join private group " + GID + " to post");
                os.writeBytes(CRLF);
                return false;
            case 2:
            case 3:
                //Create a message, store it in Mrepo broadcast to other users in the group
                Message M = CMess(Sub,Body,GID);
                String outp = M.MID + ", " + M.UID + ", " + M.Pdate + ", " + M.Sub + CRLF ;
                BroadcastAll(outp,GID,false);
                return true;
            default:// User inputted incorrect group ID
                os.writeBytes("Invalid Group ID");
                os.writeBytes(CRLF);
                return false;
        }
    }
    //Get a list of all users in group
    private boolean Users(int GID) throws Exception{
        int i = 0;
        switch (IGroup(GID)){
            case 1:
                os.writeBytes("You must join private group " + GID + " to see this group's list of users");
                os.writeBytes(CRLF);
                return false;
            case 0:
            case 2:
            case 3:
                //Return list of users in public or private group
                if(GID == 0){
                    os.writeBytes("Users in public group: ");
                }else {
                    os.writeBytes("Users in private group " + GID + ": ");
                }
                for (ServerThread C: Clients){
                    if(C.groups.contains(Integer.valueOf(GID))){
                        if (i == 0){
                            this.os.writeBytes(String.valueOf(C.ID));
                            i++;
                        }else {
                            this.os.writeBytes(", " + C.ID);
                        }
                    }
                }
                return true;
            default:
                os.writeBytes("Invalid Group ID");
                os.writeBytes(CRLF);
                return false;
        }
    }
    //Remove a user from group GID
    private boolean Leave(int GID) throws Exception{
        switch (IGroup(GID)){
            case 0:
            case 1:
                os.writeBytes("You haven't joined this group yet");
                os.writeBytes(CRLF);
                return false;
            case 2:
                os.writeBytes("You have left public group");
                this.groups.remove(Integer.valueOf(GID));
                BroadcastOthers("User " + ID + " has left public group" + CRLF, GID ,true);
                return true;
            case 3:
                os.writeBytes("You have left private group" + GID);
                this.groups.remove(Integer.valueOf(GID));
                BroadcastOthers("User " + ID + " has left private group " + GID + CRLF,GID,false);
                return true;
            default:
                os.writeBytes("Invalid Group ID");
                os.writeBytes(CRLF);
                return false;
        }
    }
    //Get the group message if possible
    private boolean MSS(int MID, int GID) throws Exception{
        switch (IGroup(GID)){
            case 1:
                os.writeBytes("You must join private group " + GID + " to see this message");
                os.writeBytes(CRLF);
                return false;
            case 0:
            case 2:
            case 3:
                //Try to get the message, report to user if message not found
                Message M = MGet(MID);
                if (M != null) {
                    os.writeBytes("Message "+ MID + " content: ");
                    os.writeBytes(M.Body);
                }else {
                    os.writeBytes("Message not found");
                }
                os.writeBytes(CRLF);
                return true;
            default:
                os.writeBytes("Invalid Group ID");
                os.writeBytes(CRLF);
                return false;
        }
    }
    //Leave all groups then exit program
    private void Exit() throws Exception{
        os.writeBytes("Leaving message board");
        os.writeBytes(CRLF);
        for (Integer g: groups){
            Leave(g);
        }
        this.status = false;
        SClose();
    }

    public Integer getID() {
        return this.ID;
    }
}

