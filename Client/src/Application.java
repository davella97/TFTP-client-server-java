import java.io.IOException;
import java.net.InetAddress;

public class Application implements ICommandConsumer {

    private IApplicationObserver obs;

    public Application(IApplicationObserver obs) {
        this.obs = obs;
    }

    @Override
    public void data(int block, byte[] bytes, InetAddress address, int port) throws IOException {
        obs.data(block, bytes, address, port);
    }

    @Override
    public void ack(int block, InetAddress address, int port) {
        obs.ack(block, address, port);
    }

    @Override
    public void recList(String filename) {
        obs.addFileToGUI(filename);
    }
}
