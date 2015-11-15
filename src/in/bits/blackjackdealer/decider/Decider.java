package in.bits.blackjackdealer.decider;

import in.bits.blackjackdecider.bean.Message;
import in.bits.blackjackdecider.bean.Result;
import in.bits.blackjackdecider.bean.Type;
import in.bits.blackjackdecider.game.GameController;
import in.bits.blackjackdecier.communication.Server;
import java.util.HashMap;
import java.util.Map;

public class Decider extends Thread {
    
    GameController gameController;
    Server server;
    HashMap<String, Integer> scores;
    HashMap<String, Result> result;
    
    public Decider(GameController gameController, Server server){
        this.gameController = gameController;
        this.server = server;
        
        scores = new HashMap<>();
        result = new HashMap<>();
        
        start();
    }
    
    public void run(){
        while (true) {            
            if (gameController.isEveryoneDone()){
                server.broadcastActive(new Message(null, null, Type.RESULT, null, 0, declareWinner()));
                resetGameController();
                server.resetGameCounters();
                server.broadcast(new Message(null, null, Type.READY, null, 0, null));
            }
        }
    }
    
    public HashMap declareWinner(){
        int max = 0;
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if(entry.getValue() <= 21 && max < entry.getValue()){
                max = entry.getValue();
            }
        }
        
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if(entry.getValue() > 21){
                result.put(entry.getKey(), new Result(entry.getValue(), "BUST"));
            }
            else if (max == entry.getValue()) {
                result.put(entry.getKey(), new Result(entry.getValue(), "WINNER"));
            }
            else {
                result.put(entry.getKey(), new Result(entry.getValue(), "LOSER"));
            }
        }
        
        return result;
    }
    
    public void resetGameController(){
        scores.clear();
        result.clear();
    }
    
    public void putInScore(Message message){
        scores.put(message.getSender(), message.getScore());
    }
    
}
