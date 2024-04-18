import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.*;

public class View extends JFrame implements  IApplicationObserver {
    private InetAddress address;
    private int port;
    private String selectedFile;
    private Path path;

    private SenderProtocolManager sender;
    private JFileChooser jFileChooser;
    private JFileChooser choseDownloadDirectory;
    private JButton choseDirectory;
    private JButton download;
    private JButton upload;
    private JButton connect;
    private JButton refresh;
    private DefaultListModel<String> modelFiles;
    private JList<String> files;
    private ArrayList<byte[]> toSend = new ArrayList<>();


    public View(SenderProtocolManager sender){
        this.sender = sender;
        modelFiles = new DefaultListModel<>();
        files = new JList(modelFiles);
        JPanel center =new JPanel();
        center.setBorder(BorderFactory.createTitledBorder("Files"));

        center.add(new JScrollPane(files),BorderLayout.SOUTH);
        add(center);

        download = new JButton("Download");
        download.setEnabled(false);
        choseDirectory = new JButton("Cartella Download");
        choseDirectory.setEnabled(false);
        upload = new JButton("Upload");
        upload.setEnabled(false);
        connect = new JButton("Connetti");
        refresh = new JButton("Aggiorna file");
        refresh.setEnabled(false);

        JPanel north = new JPanel();
        north.add(connect);
        north.add(choseDirectory);
        north.add(download);
        north.add(upload);
        north.add(refresh);
        add(north, BorderLayout.NORTH);

        setSize(530,300);
        setLocationRelativeTo(null);
        setVisible(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

            }
        });
        connect.addActionListener(e -> {
            try {
                address = InetAddress.getByName(JOptionPane.showInputDialog(View.this, "Inserire l'ip"));
            } catch (UnknownHostException ex) {
                throw new RuntimeException(ex);
            }
            port = Integer.parseInt(JOptionPane.showInputDialog(View.this, "Inserire la porta"));
            sender.sendListReq(address, port);
            choseDirectory.setEnabled(true);
            upload.setEnabled(true);
            refresh.setEnabled(true);
        });
        download.addActionListener(e -> {
            this.selectedFile = files.getSelectedValue();
            download.setEnabled(false);
            upload.setEnabled(false);
            choseDirectory.setEnabled(false);
            if (!files.isSelectionEmpty()) {
                sender.sendReadRequest(this.selectedFile, address, port);
            } else {
                JOptionPane.showMessageDialog(View.this, "File non valido");
            }
        });
        choseDirectory.addActionListener(e -> {
            choseDownloadDirectory = new JFileChooser();
            choseDownloadDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = choseDownloadDirectory.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                path = Path.of(choseDownloadDirectory.getSelectedFile().getAbsolutePath());
                download.setEnabled(true);
            }
        });
        upload.addActionListener(e -> {
            jFileChooser = new JFileChooser();
            int option = jFileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                try {
                    divideArray(jFileChooser.getSelectedFile());
                    if(toSend.size() < 65536) {
                        sender.sendWriteRequest(jFileChooser.getSelectedFile().getName(), address, port);
                        download.setEnabled(false);
                        upload.setEnabled(false);
                        choseDirectory.setEnabled(false);
                    }else {
                        JOptionPane.showMessageDialog(View.this, "File troppo grande");
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        refresh.addActionListener(e -> {
            sender.sendListReq(address, port);
        });
    }
    private void divideArray(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        byte[] data = new byte[512];
        int i;
        while ((i = inputStream.read(data, 0, 512)) > 0){
            byte[] copy = new byte[i];
            System.arraycopy(data, 0, copy, 0, i);
            toSend.add(copy);
        }
    }
    @Override
    public void data(int block, byte[] bytes, InetAddress address, int port) throws IOException {
        this.port = port;
        File tmpFile = new File("tmpFile-" + selectedFile + "-" + block);
        tmpFile.createNewFile();
        try (FileOutputStream outputStream = new FileOutputStream(tmpFile)) {
            outputStream.write(bytes);
        }
        if(bytes.length < 512){
            compactFile(block+1);
        }
        sender.sendAck(block, address, port);
    }
    public void compactFile(int block) throws IOException {
        File finalFile = new File(path + File.separator + selectedFile);
        FileOutputStream outputStream = new FileOutputStream(finalFile);
        byte[] tmp = new byte[512];
        for(int i = 0; i < block; i++){
            File tmpFile = new File("tmpFile-" + selectedFile + "-" + i);
            FileInputStream fileInputStream = new FileInputStream(tmpFile);
            int n = fileInputStream.read(tmp);
            outputStream.write(tmp,0,n);
            fileInputStream.close();
            tmpFile.delete();
        }
        outputStream.close();
        download.setEnabled(true);
        upload.setEnabled(true);
        choseDirectory.setEnabled(true);
    }
    @Override
    public void ack(int block, InetAddress address, int port) {
        this.port = port;
        if(toSend.size() != block){
            sender.sendData(toSend.get(block), block + 1, address, port);
        } else {
            download.setEnabled(true);
            upload.setEnabled(true);
            choseDirectory.setEnabled(true);
        }
    }

    @Override
    public void addFileToGUI(String filename) {
        if(!modelFiles.contains(filename)) {
            modelFiles.addElement(filename);
        }
    }
}