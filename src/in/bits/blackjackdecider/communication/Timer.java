package in.bits.blackjackdecider.communication;

import in.bits.blackjack.bean.Message;
import in.bits.blackjack.bean.Type;
import in.bits.blackjackdecider.game.GameController;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Timer extends Thread{
    
    private Server server;
    private GameController gameController;
    private boolean runTimer = true;
    
    public Timer (Server server, GameController gameController) {
        this.server = server;
        this.gameController = gameController;
        start();
    }

    public void run() {
        while (runTimer) {
            System.out.println("Tick");
            try {
                sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Timer.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(server.isDealerStatus() == true && server.getCurrentlyActive() >= 2 && server.isGameStatus() == false && System.currentTimeMillis() >= server.getLastJoin() + 30000){
                server.setGameStatus(true);
                gameController.setPlaying();
                server.broadcastActive(new Message(null, null, Type.GAMEBEGIN, null, 0, null), 0);
                server.sendPlayerList();
                runTimer = false;
            }
        }
    }

    public void setRunTimer(boolean runTimer) {
        this.runTimer = runTimer;
    }
    
    
    
    
}
