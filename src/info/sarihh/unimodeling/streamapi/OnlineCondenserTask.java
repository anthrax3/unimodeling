package info.sarihh.unimodeling.streamapi;

import info.sarihh.unimodeling.gui.RFIDServerFrame;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Author: Sari Haj Hussein
 */
public class OnlineCondenserTask extends TimerTask {

    public OnlineCondenserTask(RFIDServerFrame rfidServerFrame, ConcurrentHashMap<String, HashMap<String, String>> memoryMap) {
        this.rfidServerFrame = rfidServerFrame;
        this.memoryMap = memoryMap;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public void run() {
        //System.out.println("memoryMap size is: " + memoryMap.size());
        for (String objID : memoryMap.keySet()) {
            if (!objID.contains(searchKey)) {
                objID = objID.substring(0, objID.lastIndexOf(",")); // strip off START and END
                rfidServerFrame.appendLogText("Populating ["
                        + rfidServerFrame.getStreamSource().getObjIDAttributeName() + ","
                        + rfidServerFrame.getStreamSource().getReaderIDAttributeName() + "]="
                        + objID);
                rfidServerFrame.getStreamSource().getObjIDAttributeName();
                rfidServerFrame.getStreamSource().getReaderIDAttributeName();
                insert(objID); // insert the record into app_table
                memoryMap.remove(objID + ",START"); // and remove its first appearance from memoryMap
                memoryMap.remove(objID + ",END"); // and remove its last apperance from memoryMap
            }
        }
        if (memoryMap.size() <= 2) { // the last one or two rows
            for (String objID : memoryMap.keySet()) {
                objID = objID.substring(0, objID.lastIndexOf(",")); // strip off START and END
                rfidServerFrame.appendLogText("Populating ["
                        + rfidServerFrame.getStreamSource().getObjIDAttributeName() + ","
                        + rfidServerFrame.getStreamSource().getReaderIDAttributeName() + "]="
                        + objID);
                insert(objID); // insert the record into app_table
                memoryMap.remove(objID + ",START"); // and remove its first appearance from memoryMap
                memoryMap.remove(objID + ",END"); // and remove its last apperance from memoryMap
            }
        }
        System.gc(); // save memory
    }

    public void insert(String searchKey) {
        try {
            HashMap<String, String> s_record = memoryMap.get(searchKey + ",START");
            HashMap<String, String> e_record = memoryMap.get(searchKey + ",END");
            if (e_record == null) { // object appears once in only one location
                e_record = s_record;
            }
            if (s_record != null && e_record != null) {
                String query = "INSERT INTO APPEAR_TABLE VALUES (?, ?, ?, ?)";
                statement = rfidServerFrame.getConnection().prepareStatement(query);
                statement.setString(1, s_record.get("LICENSE_PLATE"));
                statement.setInt(2, Integer.parseInt(s_record.get("LOCATION_ID")));
                statement.setTimestamp(3, Timestamp.valueOf(s_record.get("READING_TS")));
                statement.setTimestamp(4, Timestamp.valueOf(e_record.get("READING_TS")));
                statement.executeUpdate();
            }
            statement.close();
        } catch (SQLException e) {
            for (Throwable t : e) {
                t.printStackTrace();
            }
        }
    }
    private RFIDServerFrame rfidServerFrame = null;
    private ConcurrentHashMap<String, HashMap<String, String>> memoryMap = null;
    private String searchKey = null;
    private static PreparedStatement statement = null;
}