package in.bits.blackjackdealer.decider;

import in.bits.blackjackdecider.game.GameController;
import in.bits.blackjackdecier.communication.Server;
import java.util.HashMap;
import java.util.Map;

public class Decider extends Thread {
    
    GameController gameController;
    Server server;
    HashMap<String, Integer> scores;
    HashMap<String, String> result;
    
    public Decider(GameController gameController, Server server){
        this.gameController = gameController;
        this.server = server;
        start();
    }
    
    public void run(){
        while (true) {            
            if (gameController.isEveryoneDone()){
                
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
                result.put(entry.getKey(), "BUST");
            }
            else if (max == entry.getValue()) {
                result.put(entry.getKey(), "WINNER");
            }
            else {
                result.put(entry.getKey(), "LOSER");
            }
        }
        
        return result;
    }
    
}
