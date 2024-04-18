import edu.avo.udplibcom.Receiver;
import edu.avo.udplibcom.Sender;

import java.net.*;

public class Main {
    public static void main(String[] args) throws SocketException {
        DatagramSocket socket=new DatagramSocket();
        Sender sender =new Sender(socket);
        SenderProtocolManager spm=new SenderProtocolManager(sender);
        IApplicationObserver observer = new View(spm);
        ICommandConsumer app = new Application(observer);
        Receiver receiver=new Receiver(socket, 517);
        ReceiverProtocolManager rpm=new ReceiverProtocolManager(app);
        receiver.setConsumer(rpm);
    }
}