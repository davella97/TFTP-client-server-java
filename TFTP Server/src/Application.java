import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Application implements ICommandConsumer {
    private SenderProtocolManager sender;
    private File directory;
    private String path = System.getProperty("user.dir") + File.separator + "files";
    private List<String> fileList = new ArrayList<String>();
    private HashMap<InetAddress, String> client = new HashMap<>(); //Address e nome del file
    private HashMap<InetAddress, ArrayList<byte[]>> toSendList = new HashMap<>(); //address e Arraylist contenente tutti gli array di byte in cui è diviso un file

    public Application(SenderProtocolManager sender) {
        this.sender = sender;
    }
    private int getUnsigned(byte value){
        return 0xFF & value;
    }
    @Override
    public void read(String filename, String mode, InetAddress address, int port) throws IOException { //prima richiesta di read e primo blocco dati
        if(!client.containsKey(address)) {
            ArrayList<byte[]> toSend = new ArrayList<>();
            toSendList.put(address, toSend);
            client.put(address, filename);
            FileInputStream inputStream = new FileInputStream(new File(this.path + File.separator + filename));
            byte[] data = new byte[512];
            int i;
            while ((i = inputStream.read(data, 0, 512)) > 0) {
                byte[] copy = new byte[i];
                System.arraycopy(data, 0, copy, 0, i);
                toSendList.get(address).add(copy);
            }
            sender.sendRead(toSendList.get(address).getFirst(), 0, address, port);
        }
    }
    @Override
    public void writeRequest(String filename, String mode, InetAddress address, int port) throws IOException {
        if(!client.containsKey(address)) {
            directory = new File(path + File.separator + filename);
            directory.createNewFile();
            if (directory.isFile()) {
                client.put(address, filename);
                sender.sendAck(0, address, port);
            }
        }
    }

    @Override
    public void data(int block, byte[] bytes, InetAddress address, int port) throws IOException {
        File tmpFile = new File("tmpFile-" + client.get(address) + "-" + block);
        tmpFile.createNewFile();
        try (FileOutputStream outputStream = new FileOutputStream(tmpFile)) {
            outputStream.write(bytes);
        }
        sender.sendAck(block, address, port);
        if(bytes.length < 512){
            compactFile(block+1, address, port);
        }
    }

    public void compactFile(int block, InetAddress address, int port) throws IOException { //fine ricezione dati, ultimo pacchetto ricevuto
        File finalFile = new File(path + File.separator + client.get(address));
        FileOutputStream outputStream = new FileOutputStream(finalFile);
        byte[] tmp = new byte[512];
        for(int i = 1; i < block; i++){
            File tmpFile = new File("tmpFile-" + client.get(address) + "-" + i);
            FileInputStream fileInputStream = new FileInputStream(tmpFile);
            int n = fileInputStream.read(tmp);
            outputStream.write(tmp,0,n);
            fileInputStream.close();
            tmpFile.delete();
        }
        outputStream.close();
        toSendList.remove(address);
        client.remove(address);
        this.sendList(address, port);
    }

    @Override
    public void ackReceive(int block, InetAddress address, int port) { //mando dopo read e ack
        if(toSendList.get(address).size() != block + 1){
            sender.sendRead(toSendList.get(address).get(block + 1), block + 1, address, port);
        }else {
            //fine ricezione ack, ultimo pacchetto ricevuto
            toSendList.remove(address);
            client.remove(address);
        }
    }

    @Override
    public void error() {

    }

    @Override
    public void sendList(InetAddress address, int port) {
        File[] files = new File(path).listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String fileName = new String(file.getName().getBytes(), StandardCharsets.UTF_8);
                fileList.add(fileName);
            }
        }
        sender.sendList(fileList, address, port);
    }

}
