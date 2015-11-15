package in.bits.blackjackdecier.communication;

import in.bits.blackjackdecider.bean.Message;
import java.net.Socket;

public interface ServerInterface {
    public void listen(int port);
    public void broadcast(Message message);
    public void closeConnection(Socket socket);
    //public void sendClientList();
    public void unicast(Message message);
}
