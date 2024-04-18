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

    @Override
    public void consumeData(byte[] bytes, int i, InetAddress ia, int port) {
        int command = bytes[0] & 0xFF;
        command = command * 256;
        command += bytes[1] & 0xFF;
        switch (command){
            case 1 -> { //read
                //vuoto
            }
            case 2 -> { //write
                //vuoto
            }
            case 3 -> { //data packet
                int block = bytes[2] & 0xFF;
                block = block * 256;
                block += bytes[3] & 0xFF;
                byte[] data = new byte[i-4];
                System.arraycopy(bytes, 4, data,0, data.length);
                try {
                    consumer.data(block, data, ia, port);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case 4 -> { //ack
                int block = bytes[2] & 0xFF;
                block = block * 256;
                block += bytes[3] & 0xFF;
                consumer.ack(block, ia, port);
            }
            case 5 -> { //error

            }
            case 6 -> { //OACK packet

            }
            case 7 -> { //file list request
                byte[] data = getData(bytes, 2);
                String fileName = new String (data, StandardCharsets.UTF_8);
                consumer.recList(fileName);
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
}
