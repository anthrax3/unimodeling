package info.sarihh.unimodeling.streamapi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.StringTokenizer;

/*
 * Author: Sari Haj Hussein
 */
public class StreamSource {

    public StreamSource() {
    }

    public void register(String streamEncoding) {
        tokenizeStreamEncoding(streamEncoding);
    }

    public void start(Connection conn) {
        this.conn = conn;
        StringBuilder attributes = new StringBuilder();
        for (String attributeName : streamEncodingMap.keySet()) {
            attributes.append(attributeName).append(",");
        }
        attributes.deleteCharAt(attributes.length() - 1);
        String query = "SELECT " + attributes.toString() + " FROM " + streamName + " ORDER BY ?";
        try {
            PreparedStatement statement = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            statement.setString(1, timeAttributeName);
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            for (Throwable t : e) {
                t.printStackTrace();
            }
        }
    }

    public boolean getNext(HashMap<String, String> reading) {
        try {
            if (resultSet.next()) {
                for (String attributeName : streamEncodingMap.keySet()) {
                    reading.put(attributeName, resultSet.getObject(attributeName).toString());
                }
                return true;
            }
        } catch (SQLException e) {
            for (Throwable t : e) {
                t.printStackTrace();
            }
        }
        return false;
    }

    public void end() {
        try {
            resultSet.close();
        } catch (SQLException e) {
            for (Throwable t : e) {
                t.printStackTrace();
            }
        }
    }

    private void tokenizeStreamEncoding(String streamEncoding) {
        StringTokenizer st = new StringTokenizer(streamEncoding);
        st.nextToken();
        st.nextToken();
        streamName = st.nextToken().trim().toUpperCase(); // store stream name
        st = new StringTokenizer(streamEncoding.substring(streamEncoding.indexOf("(") + 1, streamEncoding.length() - 1), "#");
        objIDAttributeName = getAttributeName(st.nextToken().trim()); // store object ID
        readerIDAttributeName = getAttributeName(st.nextToken().trim()); // store reader ID
        timeAttributeName = getAttributeName(st.nextToken().trim()); // store time
        st = new StringTokenizer(streamEncoding.substring(streamEncoding.indexOf("(") + 1, streamEncoding.length() - 1), "#");
        while (st.hasMoreTokens()) { // now store additional attributes
            String token = st.nextToken().trim();
            streamEncodingMap.put(getAttributeName(token), getAttributeType(token));
        }
    }

    private String getAttributeName(String s) {
        return s.substring(0, s.indexOf(' ')).toUpperCase();
    }

    private String getAttributeType(String s) {
        return s.substring(s.indexOf(' ') + 1).toUpperCase();
    }

    public String getObjIDAttributeName() {
        return objIDAttributeName;
    }

    public String getReaderIDAttributeName() {
        return readerIDAttributeName;
    }

    public String getTimeAttributeName() {
        return timeAttributeName;
    }

    public HashMap<String, String> getStreamEncodingMap() {
        return streamEncodingMap;
    }
    private String streamName = null;
    private String objIDAttributeName = null;
    private String readerIDAttributeName = null;
    private String timeAttributeName = null;
    private HashMap<String, String> streamEncodingMap = new HashMap<>();
    private Connection conn = null;
    private ResultSet resultSet = null;
}