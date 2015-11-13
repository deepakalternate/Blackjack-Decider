package in.bits.blackjackdecider.game;

import in.bits.blackjackdealer.decider.Decider;
import in.bits.blackjackdecider.bean.Message;
import in.bits.blackjackdecier.communication.Server;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class GameController {
    
    HashMap<Socket, ObjectOutputStream> playing;
    HashMap<Socket, ObjectOutputStream> folded;
    Server server;
    Decider decider;
    private int noOfPlayers;
    
    public GameController(Server server){
        playing = new HashMap<>();
        folded = new HashMap<>();
        this.server = server;
        decider = new Decider(this, server);
    }
    
    public void hit(Message message){
        server.sendToDealer(message);
    }
    
    public void fold(Socket socket){
        folded.put(socket, playing.get(socket));
        playing.remove(socket);
    }

    /**
     * @return the noOfPlayers
     */
    public int getNoOfPlayers() {
        return noOfPlayers;
    }

    /**
     * @param noOfPlayers the noOfPlayers to set
     */
    public void setNoOfPlayers(int noOfPlayers) {
        this.noOfPlayers = noOfPlayers;
    }
    
    public boolean isEveryoneDone(){
        return folded.size() == noOfPlayers;
    }
    
}
