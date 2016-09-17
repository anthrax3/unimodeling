package info.sarihh.unimodeling.midgraph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is a vertex representation of the midway graph. It stores the
 * connected edges and added RFIDs. It also contains different coordinates for
 * the different modelers
 * Author: Sari Haj Hussein
 */
public class MVertex implements Serializable {

    private Map<MEdge, Integer> edges;
    private Map<String, Double> rfids;
    private Map<String, Double> coverageWeights;
    private String name;
    private String delimCoordinates;
    private String graphCoordinates;
    private String rfidCoordinates;
    private int id;
    private static int counter = 0;
    private Object ogID;

    /**
     * Creates a new vertex without name
     */
    public MVertex() {
        edges = new HashMap<>();
        coverageWeights = new HashMap<>();
        rfids = new HashMap<>();
        id = counter++;
    }

    /**
     * Creates a new vertex with name
     * @param name name of the vertex
     */
    public MVertex(String name) {
        edges = new HashMap<>();
        coverageWeights = new HashMap<>();
        rfids = new HashMap<>();
        this.name = name;
        id = counter++;
    }

    /**
     * Gets the name of the vertex
     * @return name of the vertex
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the vertex
     * @param name name of the vertex
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the graph coordinates of this vertex. X1_Y1_X2_Y2
     * @return coordinates
     */
    public String getGraphCoordinates() {
        return graphCoordinates;
    }

    /**
     * Sets the graph coordinates of this vertex. X1_Y1_X2_Y2
     * @param rfidCoordinates coordinates
     */
    public void setGraphCoordinates(String graphCoordinates) {
        this.graphCoordinates = graphCoordinates;
    }

    /**
     * Gets the RFID graph coordinates of this vertex. X1_Y1_X2_Y2
     * @return RFID coordinates
     */
    public String getRfidCoordinates() {
        return rfidCoordinates;
    }

    /**
     * Sets the RFID graph coordinates of this vertex. X1_Y1_X2_Y2
     * @param rfidCoordinates RFID coordinates
     */
    public void setRfidCoordinates(String rfidCoordinates) {
        this.rfidCoordinates = rfidCoordinates;
    }

    /**
     * Gets the vertex ID
     * @return ID
     */
    public int getId() {
        return id;
    }

    /**
     * Adds a new RFID to this location.
     * @param name RFID name
     */
    public void addRfid(String name) {
        rfids.put(name, 1.0);
    }

    /**
     * Replaces an RFID by its name
     * @param oldName old RFID name
     * @param newName new RFID name
     */
    public void replaceRfid(String oldName, String newName) {
        rfids.remove(oldName);
        rfids.put(newName, 1.0);
    }

    /**
     * Removes an RFID by name from this location
     * @param name RFID name
     */
    public void removeRfid(String name) {
        rfids.remove(name);
    }

    /**
     * Adds an RFID to one of the connections
     * @param name RFID name
     * @param ratio RFID coverage weight
     */
    public void addConnRfid(String name, double ratio) {
        coverageWeights.put(name, ratio);
    }

    /**
     * Removes an RFID from a connection
     * @param name RFID name
     */
    public void removeConnRfid(String name) {
        coverageWeights.remove(name);
    }

    /**
     * Gets the vertex' name for the RFID graph
     * @return vertex name
     */
    public String getRfidName() {
        String rfid = "";
        for (String s : rfids.keySet()) {
            rfid += s + ", ";
        }
        if (rfid.equals("")) {
            return name + ": Ø";
        }
        return name + ":" + rfid.substring(0, rfid.length() - 2);
    }

    /**
     * Gets the RFID labels as a string set. To store it in OrientGraph DB.
     * @return RFID labels
     */
    public Set<String> getLabels() {
        Set<String> labels = new HashSet<>();
        for (String s : rfids.keySet()) {
            labels.add(s);
        }
        return labels;
    }

    /**
     * Gets the RFID coverage weights associated with this vertex. It combines
     * them in a string. The separator is the '_' character.
     * @return RFID coverage weights
     */
    public String getRfidStats() {
        String rfid = "";
        for (String s : rfids.keySet()) {
            rfid += s + "->" + rfids.get(s) + "_";
        }
        for (String s : coverageWeights.keySet()) {
            rfid += s + "->" + coverageWeights.get(s) + "_";
        }
        return rfid;
    }

    /**
     * Gets coverage weights as set. Reads the RFIDs and combine them in a
     * string set to store in OrientGraph DB.
     * @return coverage weights set
     */
    public Set<String> getCoverageWeightSet() {
        Set<String> weights = new HashSet<>();
        for (String s : rfids.keySet()) {
            weights.add(s + "->" + rfids.get(s));
        }
        for (String s : coverageWeights.keySet()) {
            weights.add(s + "->" + coverageWeights.get(s));
        }
        return weights;
    }

    /**
     * Adds an edge to thhis vertex
     * @param edge new edge
     * @param direction edge direction (IN or OUT)
     */
    public void addEdge(MEdge edge, int direction) {
        edges.put(edge, direction);
    }

    /**
     * Gets the connected edges. Edges are the keys and direction is the value
     * @return connected edges
     */
    public Map<MEdge, Integer> getEdges() {
        return edges;
    }

    /**
     * Sets the connected edges. Edges are the keys and direction is the value
     * @param edges connected edges
     */
    public void setEdges(Map<MEdge, Integer> edges) {
        this.edges = edges;
    }

    /**
     * Sets the ID
     * @param id ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the OrientVertex ID
     * @return OrientVertex ID
     */
    public Object getOgID() {
        return ogID;
    }

    /**
     * Sets the OrientVertex ID
     * @param ogID OrientVertex ID
     */
    public void setOgID(Object ogID) {
        this.ogID = ogID;
    }

    /**
     * Gets the coordinates of the delimiters. Represented as X1_Y1_X2_Y2
     * @return delimiter coordinates
     */
    public String getDelimCoordinates() {
        return delimCoordinates;
    }

    /**
     * Sets the coordinates of the delimiters. Represented as X1_Y1_X2_Y2
     * @param delimCoordinates delimiter coordinates
     */
    public void setDelimCoordinates(String delimCoordinates) {
        this.delimCoordinates = delimCoordinates;
    }

    /**
     * Gets the RFIDs of this location (vertex). The RFIDs are in a map, their
     * name is the key, and their coverage weight is the value.
     * @return RFIDs of this location
     */
    public Map<String, Double> getRfids() {
        return rfids;
    }

    /**
     * Sets the RFIDs of this location (vertex). The RFIDs are in a map, their
     * name is the key, and their coverage weight is the value.
     * @param rfids RFIDs of this location
     */
    public void setRfids(Map<String, Double> rfids) {
        this.rfids = rfids;
    }

    /**
     * Gets the map of coverage weights. RFID names are the key and coverage
     * weights are the values
     * @return map of coverage weights
     */
    public Map<String, Double> getCoverageWeights() {
        return coverageWeights;
    }

    /**
     * Sets the map of coverage weights. RFID names are the key and coverage
     * weights are the values
     * @param coverageWeights map of coverage weights
     */
    public void setCoverageWeights(Map<String, Double> coverageWeights) {
        this.coverageWeights = coverageWeights;
    }
}
