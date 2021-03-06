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
