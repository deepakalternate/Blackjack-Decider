package in.bits.blackjackdecier.communication;

import in.bits.blackjackdecider.bean.Message;
import in.bits.blackjackdecider.bean.Type;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread extends Thread {
    
    private Server server;
    private Socket socket;
    
    //Constructor
    public ServerThread(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        
        start();
    }
    
    public void run(){
        
        try {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            while (true) {
           
                Message message = (Message)input.readObject();
                
                synchronized(this){
                    if(server.isDealer(socket) == false && server.isDealerStatus() == true && server.getCurrentlyActive() >= 2 && server.isGameStatus() == false && System.currentTimeMillis() >= server.getLastJoin() + 30000) {
                        
                        server.setGameStatus(true);
                        server.raiseCount();
                        
                        if(server.getCount() == 1) {
                            server.broadcast(new Message(null, null, Type.GAMEBEGIN, null));
                        }
                    }
                }
                
                if(message.getType().getTypeOfMessage().equalsIgnoreCase("ISDEALER")) {
                    server.setDealer(socket);
                } 
                else if(message.getType().getTypeOfMessage().equalsIgnoreCase("JOIN")) {
                    server.addToList(message.getSender(), socket);
                }
                else if(message.getType().getTypeOfMessage().equalsIgnoreCase("EXIT")){
                    server.quitGame(message.getSender(), socket);
                }
                else if(message.getType().getTypeOfMessage().equalsIgnoreCase("CARD")){
                        //get receiver from message and unicast it to that bastard
                }
                else if(message.getType().getTypeOfMessage().equalsIgnoreCase("HIT")) {
                    server.sendToDealer(message);
                }
                else if(message.getType().getTypeOfMessage().equalsIgnoreCase("FOLD")) {
                        //Fold procedure
                }
                else if(message.getType().getTypeOfMessage().equalsIgnoreCase("FOREVAL")) {
                        //Send the hand value to the decider
                }
               
            }

        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            server.closeConnection(socket);
        }
    }
}
