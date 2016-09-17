package info.sarihh.unimodeling.utility;

import info.sarihh.unimodeling.gui.RFIDServerFrame;
import info.sarihh.unimodeling.streamapi.OnlineCondenserTask;
import java.net.Socket;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Author: Sari Haj Hussein
 */
public class ClientHandler extends Thread {

    public ClientHandler(RFIDServerFrame rfidServerFrame, Socket client) {
        this.rfidServerFrame = rfidServerFrame;
        this.client = client;
    }

    @Override
    public void run() {
        ConcurrentHashMap<String, HashMap<String, String>> memoryMap = new ConcurrentHashMap<>();
        Timer timer = new Timer();
        OnlineCondenserTask translatorTask = new OnlineCondenserTask(rfidServerFrame, memoryMap);
        timer.schedule(translatorTask, rfidServerFrame.getCondensingStarts(), rfidServerFrame.getCondensingRepeats());
        try {
            ObjectInputStream clientInputStream = new ObjectInputStream(client.getInputStream());
            while (true) {
                HashMap<String, String> reading = (HashMap<String, String>) clientInputStream.readObject();
                if (reading.containsKey("QUIT")) { // client disconnected
                    rfidServerFrame.removeConnection(client);
                    translatorTask.run();
                    translatorTask.cancel();
                    timer.cancel();
                    break;
                } else if (reading.containsKey("END")) { // client simulation ended
                    translatorTask.run();
                } else {
                    rfidServerFrame.appendLogText("[" + client.getInetAddress().getHostAddress() + "] -> " + reading);
                    String objID = reading.get(rfidServerFrame.getStreamSource().getObjIDAttributeName());
                    String readerID = reading.get(rfidServerFrame.getStreamSource().getReaderIDAttributeName());
                    String searchKey = objID + "," + readerID;
                    if (memoryMap.containsKey(searchKey + ",START")) { // if the appearance record has started
                        memoryMap.put(objID + "," + readerID + ",END", reading); // end it
                    } else if (memoryMap.containsKey(searchKey + ",END")) { // if it has endedn
                        memoryMap.put(objID + "," + readerID + ",END", reading); // end it again
                    } else { // if it neither started not ended, then start it
                        memoryMap.put(objID + "," + readerID + ",START", reading);
                    }
                    translatorTask.setSearchKey(searchKey);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rfidServerFrame.appendLogText(e.toString().contains("SocketException") ? "" : e.toString());
            translatorTask.run();
            translatorTask.cancel();
            timer.cancel();
            Thread.currentThread().interrupt();
        }
    }
    private RFIDServerFrame rfidServerFrame;
    private Socket client;
}
