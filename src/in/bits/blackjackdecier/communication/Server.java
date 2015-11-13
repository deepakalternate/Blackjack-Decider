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
    private HashMap<String, Socket> clientList;
    private long lastJoin;
    private int gameStatus = 0;
    //private ServerUpdateThread sut;
    
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
            //sut = new ServerUpdateThread(this);
            
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connected to :" + socket);
                ObjectOutputStream buf = new ObjectOutputStream(socket.getOutputStream());
                if(clients.size() <= 6){
                    clients.put(socket, buf);
                    setLastJoin(System.currentTimeMillis());
                    if(getGameStatus() == 0) {
                        activePlayers.put(socket, buf);
                    }
                    else {
                        waitingPlayers.put(socket, buf);
                    }
                }
                else {
                    
                }
                new ServerThread(this, socket);
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
     * @return the gameStatus
     */
    public int getGameStatus() {
        return gameStatus;
    }

    /**
     * @param gameStatus the gameStatus to set
     */
    public void setGameStatus(int gameStatus) {
        this.gameStatus = gameStatus;
    }
    
    
    
}
