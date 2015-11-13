package in.bits.blackjackdecider.game;

import in.bits.blackjackdecider.bean.Message;
import in.bits.blackjackdecier.communication.Server;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class GameController {
    
    HashMap<Socket, ObjectOutputStream> playing;
    HashMap<Socket, ObjectOutputStream> folded;
    Server server;
    
    public GameController(Server server){
        playing = new HashMap<>();
        folded = new HashMap<>();
        this.server = server;
    }
    
    public void hit(Message message){
        server.sendToDealer(message);
    }
    
    public void fold(Socket socket){
        folded.put(socket, playing.get(socket));
        playing.remove(socket);
    }
    
}
