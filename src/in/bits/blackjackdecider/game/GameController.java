package in.bits.blackjackdecider.game;

import in.bits.blackjackdecider.decider.Decider;
import in.bits.blackjack.bean.Message;
import in.bits.blackjackdecider.communication.Server;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class GameController {
    
    private HashMap<Socket, ObjectOutputStream> playing;
    private HashMap<Socket, ObjectOutputStream> folded;
    private Server server;
    private Decider decider;
    private int noOfPlayers = 0;
    
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
    
    public void forwardForEvaluation(Message message){
        decider.putInScore(message);
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
    
    public void setPlaying(){
        playing = server.getActivePlayers();
        noOfPlayers = playing.size();
    }
    
}
