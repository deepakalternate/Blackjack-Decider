/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package in.bits.blackjackdecider.main;

import in.bits.blackjackdecier.communication.Server;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tarun
 */
public class Main {
    public static void main(String args[]){
        try {
            Server server = new Server(Integer.parseInt(args[0]));
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
