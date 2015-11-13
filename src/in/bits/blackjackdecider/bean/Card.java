package in.bits.blackjackdecider.bean;

import java.io.Serializable;

public class Card implements Serializable {
    
    private String suit;
    private int displayValue;
    private int scoreValue;
    
    public Card(String suit, int displayValue, int scoreValue) {
        this.suit = suit;
        this.displayValue = displayValue;
        this.scoreValue = scoreValue;
    }

    /**
     * @return the suit
     */
    public String getSuit() {
        return suit;
    }

    /**
     * @return the displayValue
     */
    public int getDisplayValue() {
        return displayValue;
    }

    /**
     * @return the scoreValue
     */
    public int getScoreValue() {
        return scoreValue;
    }
    
    
    
}
