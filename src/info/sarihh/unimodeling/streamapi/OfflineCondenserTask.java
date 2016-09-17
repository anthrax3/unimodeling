package info.sarihh.unimodeling.streamapi;

import info.sarihh.unimodeling.gui.OfflineCondenserFrame;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Author: Sari Haj Hussein
 */
public class OfflineCondenserTask {

    public OfflineCondenserTask(OfflineCondenserFrame offlineTranslatorFrame,
            ConcurrentHashMap<String, HashMap<String, String>> memoryMap,
            int bulkSize) {
        this.offlineTranslatorFrame = offlineTranslatorFrame;
        this.memoryMap = memoryMap;
        this.bulkSize = bulkSize;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public void condense() {
        if (memoryMap.size() >= bulkSize) {
            offlineTranslatorFrame.appendLogText("Condensing the in-memory hash structure with " + memoryMap.size() + " mappings.");
            for (String objID : memoryMap.keySet()) {
                if (!objID.contains(searchKey)) {
                    objID = objID.substring(0, objID.lastIndexOf(",")); // strip off START and END
                    offlineTranslatorFrame.getStreamSource().getObjIDAttributeName();
                    offlineTranslatorFrame.getStreamSource().getReaderIDAttributeName();
                    insert(objID); // insert the record into app_table
                    memoryMap.remove(objID + ",START"); // and remove its first appearance from memoryMap
                    memoryMap.remove(objID + ",END"); // and remove its last apperance from memoryMap
                }
            }
            if (memoryMap.size() <= 2) { // the last one or two rows
                for (String objID : memoryMap.keySet()) {
                    objID = objID.substring(0, objID.lastIndexOf(",")); // strip off START and END
                    insert(objID); // insert the record into app_table
                    memoryMap.remove(objID + ",START"); // and remove its first appearance from memoryMap
                    memoryMap.remove(objID + ",END"); // and remove its last apperance from memoryMap
                }
            }
            System.gc(); // save memory
        }
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
                statement = offlineTranslatorFrame.getConnection().prepareStatement(query);
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
    private OfflineCondenserFrame offlineTranslatorFrame = null;
    private ConcurrentHashMap<String, HashMap<String, String>> memoryMap = null;
    private int bulkSize = 0;
    private String searchKey = null;
    private PreparedStatement statement = null;
}