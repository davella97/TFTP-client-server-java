import edu.avo.udplibcom.IDataConsumer;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ReceiverProtocolManager implements IDataConsumer{

    ICommandConsumer consumer;

    public ReceiverProtocolManager(ICommandConsumer consumer) {
        this.consumer = consumer;
    }
    //TFTP packets headers.
    //primi 2 byte hanno il comand
    //nome file = dal terzo byte (incluso) fino a quando non trovo un byte tutto a 0 (solo con comando 1 o 2)
    @Override
    public void consumeData(byte[] bytes, int i, InetAddress address, int port) {
        int command = compactValue(bytes[0], bytes[1]);
        System.out.println(command);
        switch (command){
            case 1 -> { //read
                String fileName = new String (getData(bytes, 2), StandardCharsets.UTF_8).trim();
                try {
                    consumer.read(fileName, "octet", address, port);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case 2 -> { //write
                String fileName = new String (getData(bytes, 2), StandardCharsets.UTF_8).trim();
                try {
                    consumer.writeRequest(fileName, "octet", address, port);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case 3 -> { //data packet
                int block = compactValue(bytes[2], bytes[3]);
                byte[] data = new byte[i-4];
                System.arraycopy(bytes, 4, data,0, data.length);
                try {
                    consumer.data(block, data, address, port);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case 4 -> { //ack
                int block = compactValue(bytes[2], bytes[3]);
                consumer.ackReceive(block, address, port);
            }
            case 5 -> { //error

            }
            case 6 -> { //OACK packet

            }
            case 7 -> { //file list request
                consumer.sendList(address, port);
            }
        }
    }

    private byte[] getData(byte[] bytes, int firstPosition){
        int i = firstPosition;
        int j = 0;
        while(bytes[i] != 0){
            i++;
            j++;
        }
        byte[] data = new byte[j];
        System.arraycopy(bytes, firstPosition, data, 0, j);
        return data;
    }
    private int compactValue(byte MSB, byte LSB){
        int block = MSB & 0xFF;
        block = block * 256;
        block += LSB & 0xFF;
        return block;
    }

}
