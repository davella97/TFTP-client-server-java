import java.io.IOException;
import java.net.InetAddress;


/**
 *
 * @author MULTI01
 */
public interface ICommandConsumer {
    void data(int block, byte[] bytes, InetAddress address, int port) throws IOException;
    void ack(int block, InetAddress address, int port);
    void recList(String filename);
}
