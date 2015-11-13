package in.bean.blackjackdecider.game;

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
    
    
    
}
