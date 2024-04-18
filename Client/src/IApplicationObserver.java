import java.io.IOException;
import java.net.InetAddress;

public interface IApplicationObserver {
    void data(int block, byte[] bytes, InetAddress address, int port) throws IOException;
    void ack(int block, InetAddress address, int port);
    void addFileToGUI(String filename);
}
