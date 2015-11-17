package in.bits.blackjackdecider.communication;

import in.bits.blackjackdecider.game.GameController;
import in.bits.blackjack.bean.Message;
import in.bits.blackjack.bean.Result;
import in.bits.blackjack.bean.Type;
import in.bits.blackjackdecider.decider.Decider;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements ServerInterface{
    
    //Handlers
    private ServerSocket serverSocket;
    private GameController gameController;
    private Timer timer;
    private Decider decider;
    
    //Data storage
    private HashMap<Socket, ObjectOutputStream> clients;
    private HashMap<Socket, ObjectOutputStream> activePlayers;
    private HashMap<Socket, ObjectOutputStream> waitingPlayers;
    private Socket dealer;
    private ObjectOutputStream dealerOutput;
    private HashMap<String, Socket> clientList;
    private HashMap<Socket, String> nameList;
    
    //Flags and Counters
    private long lastJoin;
    private boolean gameStatus = false;
    private boolean dealerStatus = false;
    private int currentlyActive = 0;
    private int currentlyWaiting = 0;
    private int count = 0;
    
    //Constructor Start
    public Server(int port) throws IOException {
        clients = new HashMap<>();
        clientList = new HashMap<>();
        activePlayers = new HashMap<>();
        waitingPlayers = new HashMap<>();
        nameList = new HashMap<>();
        gameController = new GameController(this);
        timer = new Timer(this, gameController);
        decider = new Decider(gameController, this);

        listen(port);
    }
    //Constructor End
    
//Communication Methods Start
    
    //Listen Start 
    @Override
    public void listen(int port) {
        try {
            
            serverSocket = new ServerSocket(port);
            System.out.println("Listening on port :" + port);
            
            
            while (true) {
                
                Socket socket = serverSocket.accept();
                System.out.println("Connected to :" + socket);
                
                ObjectOutputStream buf = new ObjectOutputStream(socket.getOutputStream());
                
                clients.put(socket, buf);
                
                if(currentlyActive + currentlyWaiting <= 5){
                                     
                    
                    if(isGameStatus() == false) {
                        
                        setLastJoin(System.currentTimeMillis());
                        getActivePlayers().put(socket, buf);
                        currentlyActive += 1;
                        System.out.println("Inside active set");
                        
                    }
                    else {
                        
                        waitingPlayers.put(socket, buf);
                        currentlyWaiting +=1;
                        System.out.println("Inside waiting set");
                        
                    }
                    
                    System.out.println("Just before launching thread.");
                    new ServerThread(this, socket, gameController);
                    System.out.println("Just after launching thread");
                }
                else {
                    //Send message to user that the room is full
                    closeConnection(socket);
                }
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //Listen End
    
    //Close Connection Start
    @Override
    public void closeConnection(Socket socket) {
        
        try {
            clients.get(socket).close();
            clients.remove(socket);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                socket.close();

            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    //Close Connection End
    
    //Broadcast Start
    @Override
    public synchronized void broadcast(Message message) {
        
        for (Map.Entry<Socket, ObjectOutputStream> entry : getActivePlayers().entrySet()) {
            try {
                entry.getValue().writeObject(message);
                System.out.println(message.getType());
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        for (Map.Entry<Socket, ObjectOutputStream> entry : waitingPlayers.entrySet()) {
            try {
                entry.getValue().writeObject(message);
                System.out.println(message.getType());
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    //Broadcast End
    
    //Broadcast Active Start
    public synchronized void broadcastActive(Message message, int val) {
        
        System.out.println("Iterating the message");
        System.out.println(activePlayers);
        //Adjust iteration to avoid broadcasting to Dealer
        for (Map.Entry<Socket, ObjectOutputStream> entry : getActivePlayers().entrySet()) {
            try {
                System.out.println("inside iterator");
                entry.getValue().writeObject(message);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Broadcast finished" + val);
        
        if (val == 1) {
            System.out.println("Inside val = 1");
            gameController.resetGameController();
            resetGameCounters();
            broadcast(new Message(null, null, Type.RESTART, null, 0, null));
        }
        
    }
    //Broadcast Active End

    //Unicast Start
    @Override
    public void unicast(Message message) {
        
        try {
            clients.get(clientList.get(message.getReceiver())).writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    //Unicast End
    
    //Send To Dealer Start
    public synchronized void sendToDealer(Message message) {
        
        try {
            dealerOutput.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    //Send To Dealer End
    
    public void closeDealer() {
        try {
            dealerOutput.close();
            dealerOutput = null;
            clients.remove(dealer);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                dealer.close();
                dealer = null;
                dealerStatus = false;

            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    
//Getters and Setters Start
    
    //Getter for lastJoin
    public long getLastJoin() {
        return lastJoin;
    }

    //Setter for lastJoin
    public void setLastJoin(long lastJoin) {
        this.lastJoin = lastJoin;
    }
    
    //Getter for gameStatus
    public boolean isGameStatus() {
        return gameStatus;
    }
    
    //Setter for gameStatus
    public void setGameStatus(boolean gameStatus) {
        this.gameStatus = gameStatus;
    }
    
    //Setter for dealer
    public void setDealer(Socket socket) {
        System.out.println("Did trigger this.");
        currentlyActive -= 1;
        activePlayers.remove(socket);
        dealer = socket;
        dealerOutput = clients.get(socket);
        dealerStatus = true;
    }

    //Getter for dealerStatus
    public boolean isDealerStatus() {
        return dealerStatus;
    }

    //Getter for currentlyActive
    public int getCurrentlyActive() {
        return currentlyActive;
    }

    //Getter for currentlyWaiting
    public int getCurrentlyWaiting() {
        return currentlyWaiting;
    }
    
    //Getter for activePlayers
    public HashMap<Socket, ObjectOutputStream> getActivePlayers() {
        return activePlayers;
    }
    
//Getters and Setters End
    
//Generic and Miscellaneous Start
    
    //Check if player is active or not
    public boolean isPlayerActive(Socket socket) {
        return activePlayers.containsKey(socket);
    }
    
    //Check if the client is dealer or not
    public boolean isDealer(Socket socket) {
        return socket == dealer;
    }
    
    //Add client to clientList
    public void addToList(String name, Socket socket){
        clientList.put(name, socket);
        nameList.put(socket, name);
    }
    
    //Quit Game
    public void quitGame(String name, Socket socket) {
        
        if(socket == dealer){
            closeDealer();
        }
        else{
            clientList.remove(name);
            //clients.remove(socket);
            nameList.remove(socket);

            if (activePlayers.containsKey(socket)) {
                activePlayers.remove(socket);
                currentlyActive -= 1;
            }
            else if (waitingPlayers.containsKey(socket)) {
                waitingPlayers.remove(socket);
                currentlyWaiting -= 1;
            }

            closeConnection(socket);
        }
    }
    
    //Add to active list
    public synchronized void addToActive(Socket socket){
        activePlayers.put(socket, waitingPlayers.get(socket));
        waitingPlayers.remove(socket);
        currentlyActive += 1;
        currentlyWaiting -= 1;
        setLastJoin(System.currentTimeMillis());
    }
    
    //Get the name of the player
    public String getPlayerName(Socket socket){
        return nameList.get(socket);
    }
    
    //Raise the counter variable count
    public void raiseCount(){
        count += 1;
    }
    
    //Reset the counter variable count
    public void resetCount(){
        count = 0;
    }
    
    //Get the counter variable count
    public int getCount(){
        return count;
    }
    
    //Reset game counters to replay game
    public void resetGameCounters(){
        count = 0;
        gameStatus = false;
        Timer tock = new Timer(this, gameController);
        lastJoin = System.currentTimeMillis();
        
        for (Map.Entry<Socket, ObjectOutputStream> entry : activePlayers.entrySet()) {
            waitingPlayers.put(entry.getKey(), entry.getValue());
            //activePlayers.remove(entry.getKey());
            currentlyWaiting += 1;
            currentlyActive -= 1;
        }
        activePlayers.clear();
        
        broadcast(new Message(null, null, Type.RESTART, null, 0, null));
        
    }
    
    public void sendPlayerList(){
        String list = "";

        for (Map.Entry<String, Socket> entry : clientList.entrySet()) {
            if (activePlayers.containsKey(entry.getValue())){
                list += entry.getKey() + ",";
            }
        }
        System.out.println("Sending List-->" + list);
        broadcastActive(new Message(null, list, Type.LIST, null, count, null), 0);
    }
    
    public void sendResult(HashMap<String, Result> result){
        System.out.println("Inside send result");
        sendToDealer(new Message(null, null, Type.RESTART, null, 0, null));
        broadcastActive(new Message(null, null, Type.RESULT, null, 0, result), 1);
        
    }
    
//Generic and Miscellaneous End
    
}
