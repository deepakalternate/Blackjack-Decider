package in.bits.blackjackdecider.bean;

public enum Type {
    HIT("HIT"),
    FOLD("FOLD"),
    CARD("CARD"),
    CARDREQ("CARDREQ"),
    ISDEALER("ISDEALER"),
    WAIT("WAIT"),
    READY("READY"),
    JOIN("JOIN"),
    DISCONNECT("DISCONNECT"),
    EXIT("EXIT"),
    FOREVAL("FOREVAL"),
    RESULT("RESULT"),
    LIST("LIST");
    
    private String typeOfMessage;
    
    Type(String typeOfMessage){
        this.typeOfMessage = typeOfMessage;
    }
    
    public String getTypeOfMessage(){
       return typeOfMessage;
    }
}
    
