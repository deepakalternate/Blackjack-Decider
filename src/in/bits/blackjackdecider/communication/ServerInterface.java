package in.bits.blackjackdecider.communication;

import in.bits.blackjack.bean.Message;
import java.net.Socket;

public interface ServerInterface {
    public void listen(int port);
    public void broadcast(Message message);
    public void closeConnection(Socket socket);
    //public void sendClientList();
    public void unicast(Message message);
}
