package info.sarihh.unimodeling.midgraph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a connection representation for the midway graph. A connection
 * contains the RFIDs and the multiplicity of the connection. Meaning that
 * between two location there can be multiple connections that will appear as
 * only one edge on the graph but with a combined name.
 * Author: Sari Haj Hussein
 */
public class MConnection implements Serializable {

    private String name;
    private int num;
    private Map<String, Double> coverageWeights;
    private int id;
    private static int counter = 0;
    private MVertex plusSide;
    private boolean displayNum = false;

    /**
     * Creates a nameless connection
     */
    public MConnection() {
        name = "";
        num = 1;
        coverageWeights = new HashMap<>();
        id = counter++;
    }

    /**
     * Creates a connection with name
     * @param name connection name
     */
    public MConnection(String name) {
        this.name = name.replaceAll("\\{|\\}", "");
        num = 1;
        coverageWeights = new HashMap<>();
        id = counter++;
    }

    /**
     * Creates a connection with name and multiplicity
     * @param name connection name
     * @param num multiplicity
     */
    public MConnection(String name, int num) {
        this.name = name.replaceAll("\\{|\\}", "");
        this.num = num;
        coverageWeights = new HashMap<>();
        id = counter++;
    }

    /**
     * Gets the connection's name
     * @return connection name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the connection's name
     * @param name connection name
     */
    public void setName(String name) {
        this.name = name.replaceAll("\\{|\\}", "");
    }

    /**
     * Gets the connection's multiplicity
     * @return multiplicity
     */
    public int getNum() {
        return num;
    }

    /**
     * Gets the connection's multiplicity
     * @param num multiplicity
     */
    public void setNum(int num) {
        this.num = num;
    }

    /**
     * Gets the connection ID
     * @return ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the connection ID
     * @param id ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Adds an RFID to this connection
     * @param name RFID name
     * @param ratio RFID coverage weight
     */
    public void addRfid(String name, double ratio) {
        coverageWeights.put(name, ratio);
    }

    /**
     * Replaces an RFID on this connection
     * @param oldName old name
     * @param newName new name
     * @param ratio coverage weight
     */
    public void replaceRfid(String oldName, String newName, double ratio) {
        coverageWeights.remove(oldName);
        coverageWeights.put(newName, ratio);
    }

    /**
     * Removes an RFID from this connection
     * @param name RFID name
     */
    public void removeRfid(String name) {
        coverageWeights.remove(name);
    }

    /**
     * Gets the number of connected RFIDs
     * @return number of connected RFIDs
     */
    public int rfidSize() {
        return coverageWeights.size();
    }

    /**
     * Gets the RFIDs as a map. Names are keys and coverage weights are values
     * @return RFIDs
     */
    public Map<String, Double> getRfid() {
        return coverageWeights;
    }

    /**
     * Gets the vertex that is on the positive side of this location
     * @return positive side vertex
     */
    public MVertex getPlusSide() {
        return plusSide;
    }

    /**
     * Sets the vertex that is on the positive side of this location
     * @param plusSide positive side vertex
     */
    public void setPlusSide(MVertex plusSide) {
        this.plusSide = plusSide;
    }

    @Override
    public String toString() {
        return "(" + id + ")" + name;
    }

    /**
     * Determines whether the multiplicity should appear in the name or not
     * @return true if it should be displayed
     */
    public boolean isDisplayNum() {
        return displayNum;
    }

    /**
     * Shows or hides the multiplicity of this connection
     * @param displayNum true for show
     */
    public void setDisplayNum(boolean displayNum) {
        this.displayNum = displayNum;
    }

    /**
     * Gets the sorted RFID list for this conenction. Sort is done by reading
     * coordinates of the RFIDs and determining which is the positive side
     * @param outVertex outward vertex
     * @return sorted RFID list
     */
    public String[] getSortedRfids(MVertex outVertex) {
        String[] rfids = new String[coverageWeights.size()];
        rfids = coverageWeights.keySet().toArray(rfids);
        if (plusSide == outVertex) {
            for (int i = 0; i < rfids.length - 1; ++i) {
                for (int j = i; j < rfids.length; ++j) {
                    if (coverageWeights.get(rfids[i]) > coverageWeights.get(rfids[j])) {
                        String temp = rfids[i];
                        rfids[i] = rfids[j];
                        rfids[j] = temp;
                    }
                }
            }
        } else {
            for (int i = 0; i < rfids.length - 1; ++i) {
                for (int j = i; j < rfids.length; ++j) {
                    if (coverageWeights.get(rfids[i]) < coverageWeights.get(rfids[j])) {
                        String temp = rfids[i];
                        rfids[i] = rfids[j];
                        rfids[j] = temp;
                    }
                }
            }
        }
        return rfids;
    }

    /**
     * Gets the coverage weights of the RFIDs
     * @return coverage weights
     */
    public Map<String, Double> getCoverageWeights() {
        return coverageWeights;
    }

    /**
     * Sets the coverage weights of the RFIDs
     * @param coverageWeights coverage weights
     */
    public void setCoverageWeights(Map<String, Double> coverageWeights) {
        this.coverageWeights = coverageWeights;
    }
}
