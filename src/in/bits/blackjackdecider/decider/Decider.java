package in.bits.blackjackdecider.decider;

import in.bits.blackjack.bean.Message;
import in.bits.blackjack.bean.Result;
import in.bits.blackjack.bean.Type;
import in.bits.blackjackdecider.game.GameController;
import in.bits.blackjackdecider.communication.Server;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            
            try {
                sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Decider.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            System.out.println("I'm here, number of players: "+gameController.getNoOfPlayers()+". Is everyone done? " + gameController.isEveryoneDone());
            
            System.out.println(scores);
            
            if (gameController.getNoOfPlayers() > 0 && scores.size() == gameController.getNoOfPlayers()){
                System.out.println("Inside if");
                
                declareWinner();
                server.sendResult(result);
                
                resetScoreboard();
                
            }
        }
    }
    
    public void declareWinner(){
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
    }
    
    public void resetScoreboard(){
        scores.clear();
        result.clear();
    }
    
    public void putInScore(Message message){
        scores.put(message.getSender(), message.getScore());
    }
    
    public HashMap<String, Result> getResult(){
        return result;
    }
    
}
