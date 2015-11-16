package in.bits.blackjackdecider.communication;

import in.bits.blackjackdecider.game.GameController;
import in.bits.blackjack.bean.Message;
import in.bits.blackjack.bean.Type;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread extends Thread {
    
    private Server server;
    private Socket socket;
    private GameController gameController;
    
    //Constructor
    public ServerThread(Server server, Socket socket, GameController gameController) {
        this.server = server;
        this.socket = socket;
        this.gameController = gameController;
        
        start();
    }
    
    public void run(){
        System.out.println("Thread is running");
        Message message;
        try {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            while (true) {
           
                message = (Message)input.readObject();
                System.out.println(message);
                
                /*
                System.out.println("Current: "+System.currentTimeMillis()+" || Last Join: "+server.getLastJoin());
                System.out.println(server.isDealer(socket) == false);
                System.out.println(server.isDealerStatus() == true);
                System.out.println(server.getCurrentlyActive());
                System.out.println(server.getCurrentlyActive() >- 2);
                System.out.println(server.isGameStatus() == false);
                
                synchronized(this){
                    if(server.isDealer(socket) == false && server.isDealerStatus() == true && server.getCurrentlyActive() >= 2 && server.isGameStatus() == false && System.currentTimeMillis() >= server.getLastJoin() + 30000) {
                        
                        System.out.println("Do we reach inside?");
                        
                        server.setGameStatus(true);
                        server.raiseCount();
                        
                        
                        if(server.getCount() == 1) {
                            gameController.setPlaying();
                            System.out.println("Are we broadcasting?");
                            server.broadcast(new Message(null, null, Type.GAMEBEGIN, null, 0, null));
                        }
                    }
                }
                */
                
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
                    server.unicast(message);
                }
                else if(message.getType().getTypeOfMessage().equalsIgnoreCase("HIT")) {
                    gameController.hit(message);
                }
                else if(message.getType().getTypeOfMessage().equalsIgnoreCase("FOLD")) {
                    gameController.fold(socket);
                }
                else if(message.getType().getTypeOfMessage().equalsIgnoreCase("FOREVAL")) {
                    gameController.forwardForEvaluation(message);
                }
                else if(message.getType().getTypeOfMessage().equalsIgnoreCase("ACCEPT")) {
                    if(server.isGameStatus() == false) {
                        server.addToActive(socket);
                    }
                    else {
                        server.unicast(new Message(null, null, Type.WAIT, message.getSender(), 0, null));
                    }
                }
                else if(message.getType().getTypeOfMessage().equalsIgnoreCase("REJECT")) {
                    server.quitGame(message.getSender(), socket);
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
