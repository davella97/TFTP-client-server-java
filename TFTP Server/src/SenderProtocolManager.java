import edu.avo.udplibcom.Sender;
import java.net.*;
import java.util.Arrays;
import java.util.List;

public class SenderProtocolManager {

    Sender sender;
    String mode = "octat";

    public SenderProtocolManager(Sender sender) {
        this.sender = sender;
    }
    private byte[] separateValue(int value){
        byte[] data = new byte[2];
        data[0] = (byte) ((value / 256) & 0xFF);
        data[1] = (byte) ((value & 0xFF) % 256);
        return data;
    }

    public void sendAck(int blocco, InetAddress address, int port){
        byte [] opcode = separateValue(4);
        byte[] bloccoA = separateValue(blocco);
        byte[] message = new byte[4];
        message[0] = opcode[0];
        message[1] = opcode[1];
        message[2] = bloccoA[0];
        message[3] = bloccoA[1];
        sender.send(message, address, port);
    }
    public void sendRead(byte[] data, int block, InetAddress address, int port){
        byte [] opcode = separateValue(3);
        byte[] bloccoA = separateValue(block);
        byte[] message = new byte[data.length + 4];
        message[0] = opcode[0];
        message[1] = opcode[1];
        message[2] = bloccoA[0];
        message[3] = bloccoA[1];
        System.arraycopy(data, 0, message, 4, data.length);
        sender.send(message, address, port);
    }
    public void sendList(List<String> files, InetAddress address, int port){
        byte [] opcode = separateValue(7);
        byte[] modeByte = mode.getBytes();
        for (String file : files) {
            byte[] filenameByte = file.getBytes();
            byte[] message = new byte[filenameByte.length + 4 + modeByte.length];
            message[0] = opcode[0];
            message[1] = opcode[1];
            System.arraycopy(filenameByte, 0, message, 2, filenameByte.length);
            message[filenameByte.length + 2] = 0;
            System.arraycopy(modeByte, 0, message, filenameByte.length + 3, modeByte.length);
            message[modeByte.length + filenameByte.length+ 3] = 0;
            sender.send(message, address, port);
        }
    }
}
