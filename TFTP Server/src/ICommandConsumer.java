import java.io.IOException;
import java.net.InetAddress;

public interface ICommandConsumer {
    void read(String filename, String mode, InetAddress address, int port) throws IOException; //
    void writeRequest(String filename, String mode, InetAddress address, int port) throws IOException; //
    void data(int block, byte[] data, InetAddress address, int port) throws IOException; //
    void ackReceive(int block, InetAddress address, int port); //
    void error(); //
    void sendList(InetAddress address, int port);
}
