package in.bits.blackjackdecier.communication;

import in.bits.blackjackdecider.game.GameController;
import in.bits.blackjackdecider.bean.Message;
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
        gameController = new GameController(this);

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
                
                if(currentlyActive + currentlyWaiting <= 5){
                    
                    clients.put(socket, buf);                 
                    
                    if(isGameStatus() == false) {
                        
                        setLastJoin(System.currentTimeMillis());
                        getActivePlayers().put(socket, buf);
                        currentlyActive += 1;
                        
                    }
                    else {
                        
                        waitingPlayers.put(socket, buf);
                        currentlyWaiting +=1;
                        
                    }
                    
                    new ServerThread(this, socket, gameController);
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
            String name = "";
            for (Map.Entry<String, Socket> entry : clientList.entrySet()) {
                if (entry.getValue().equals(socket)) {
                    name = entry.getKey();
                    break;
                }
            }

            clientList.remove(name);

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
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        for (Map.Entry<Socket, ObjectOutputStream> entry : waitingPlayers.entrySet()) {
            try {
                entry.getValue().writeObject(message);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    //Broadcast End
    
    //Broadcast Active Start
    public synchronized void broadcastActive(Message message) {
        
        //Adjust iteration to avoid broadcasting to Dealer
        for (Map.Entry<Socket, ObjectOutputStream> entry : getActivePlayers().entrySet()) {
            try {
                entry.getValue().writeObject(message);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
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
    public void sendToDealer(Message message) {
        
        try {
            dealerOutput.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    //Send To Dealer End

    
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
        
        clientList.remove(name);
        clients.remove(socket);
        nameList.remove(socket);
        
        if (activePlayers.containsKey(socket)) {
            activePlayers.remove(socket);
            currentlyActive -= 1;
        }
        else if (waitingPlayers.containsKey(socket)) {
            waitingPlayers.remove(socket);
            currentlyWaiting -= 1;
        }
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
    
    public void resetGameCounters(){
        count = 0;
        gameStatus = false;
        
        for (Map.Entry<Socket, ObjectOutputStream> entry : activePlayers.entrySet()) {
            waitingPlayers.put(entry.getKey(), entry.getValue());
            activePlayers.remove(entry.getKey());
            currentlyWaiting += 1;
            currentlyActive -= 1;
        }
        
    }
    
//Generic and Miscellaneous End
    
}
