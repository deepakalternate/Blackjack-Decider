package in.bits.blackjackdecier.communication;

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
    
    private ServerSocket serverSocket;
    
    private HashMap<Socket, ObjectOutputStream> clients;
    private HashMap<Socket, ObjectOutputStream> activePlayers;
    private HashMap<Socket, ObjectOutputStream> waitingPlayers;
    private Socket dealer;
    private ObjectOutputStream dealerOutput;
    private HashMap<String, Socket> clientList;
    private HashMap<Socket, String> nameList;
    
    private long lastJoin;
    private boolean gameStatus = false;
    private boolean dealerStatus = false;
    private int currentlyActive = 0;
    private int currentlyWaiting = 0;
    private int count = 0;
    
    //Constructor
    public Server(int port) throws IOException {
        clients = new HashMap<>();
        clientList = new HashMap<>();

        listen(port);
    }

    
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
                        activePlayers.put(socket, buf);
                        currentlyActive += 1;
                        
                    }
                    else {
                        
                        waitingPlayers.put(socket, buf);
                        currentlyWaiting +=1;
                        
                    }
                    
                    new ServerThread(this, socket);
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

    @Override
    public synchronized void broadcast(Message message) {
        
        //Adjust iteration to avoid broadcasting to Dealer
        for (Map.Entry<Socket, ObjectOutputStream> entry : clients.entrySet()) {
            try {
                entry.getValue().writeObject(message);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

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

    @Override
    public void unicast(Message message) {
        
        try {
            clients.get(clientList.get(message.getReceiver())).writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void sendToDealer(Message message) {
        
        try {
            dealerOutput.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * @return the lastJoin
     */
    public long getLastJoin() {
        return lastJoin;
    }

    /**
     * @param lastJoin the lastJoin to set
     */
    public void setLastJoin(long lastJoin) {
        this.lastJoin = lastJoin;
    }

    /**
     * @param gameStatus the gameStatus to set
     */
    public void setGameStatus(boolean gameStatus) {
        this.gameStatus = gameStatus;
    }
    
    public void setDealer(Socket socket) {
        currentlyActive -= 1;
        activePlayers.remove(socket);
        dealer = socket;
        dealerOutput = clients.get(socket);
        dealerStatus = true;
    }

    /**
     * @return the gameStatus
     */
    public boolean isGameStatus() {
        return gameStatus;
    }

    /**
     * @return the dealerStatus
     */
    public boolean isDealerStatus() {
        return dealerStatus;
    }

    /**
     * @return the currentlyActive
     */
    public int getCurrentlyActive() {
        return currentlyActive;
    }

    /**
     * @return the currentlyWaiting
     */
    public int getCurrentlyWaiting() {
        return currentlyWaiting;
    }
    
    public boolean isPlayerActive(Socket socket) {
        return activePlayers.containsKey(socket);
    }
    
    public boolean isDealer(Socket socket) {
        return socket == dealer;
    }
    
    public void addToList(String name, Socket socket){
        clientList.put(name, socket);
        nameList.put(socket, name);
    }
    
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
    
    public String getPlayerName(Socket socket){
        return nameList.get(socket);
    }
    
    public void raiseCount(){
        count += 1;
    }
    
    public void resetCount(){
        count = 0;
    }
    
    public int getCount(){
        return count;
    }
    
}
