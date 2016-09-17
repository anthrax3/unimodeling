package info.sarihh.unimodeling.midgraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is an edge representation of the midway graph. It stores the
 * connecting vertices, name, id, and connection list. The connection list is
 * needed because the user can define connections between multiple locations,
 * and can also define multiple connections between two locations. With the 
 * current arrow drawing the latter is easier.
 * Author: Sari Haj Hussein
 */
public class MEdge implements Serializable {

    private Map<Integer, MVertex> vertices;
    private String name;
    private int id;
    private static int counter = 0;
    private List<MConnection> connections;
    private Object ogID;

    /**
     * Creates a nameless edge
     */
    public MEdge() {
        name = "";
        vertices = new HashMap<>();
        id = counter++;
        connections = new ArrayList<>();
    }

    /**
     * Creates an edge with name
     * @param name name of the edge
     */
    public MEdge(String name) {
        vertices = new HashMap<>();
        this.name = name;
        id = counter++;
        connections = new ArrayList<>();
    }

    /**
     * Create new edge by giving its vertices
     * @param vIn vertex IN
     * @param vOut vertex OUT
     */
    public MEdge(MVertex vIn, MVertex vOut) {
        name = "";
        vertices = new HashMap<>();
        id = counter++;
        connections = new ArrayList<>();
        vertices.put(MGraph.IN, vIn);
        vertices.put(MGraph.OUT, vOut);
    }

    /**
     * Create new edge by giving its vertices
     * @param vIn vertex IN
     * @param vOut vertex OUT
     * @param name name
     */
    public MEdge(MVertex vIn, MVertex vOut, String name) {
        this.name = name;
        vertices = new HashMap<>();
        id = counter++;
        connections = new ArrayList<>();
        vertices.put(MGraph.IN, vIn);
        vertices.put(MGraph.OUT, vOut);
    }

    /**
     * Adds a vertex to one end of the edge
     * @param vertex vertex to add
     * @param direction end of the edge to add
     */
    public void addVertex(MVertex vertex, int direction) {
        vertices.put(direction, vertex);
    }

    /**
     * Adds a connection the edge. One edge can contain multiple connections.
     * @param conn connection to add
     */
    public void addConnection(MConnection conn) {
        connections.add(conn);
    }

    /**
     * Removes connection by name and multiplicity. Connections between same
     * locations have the same name but different number.
     * @param name name of the edge to remove
     * @param num the number of the edge to remove
     */
    public void removeConnection(String name, int num) {
        MConnection toRemove = null;
        for (MConnection mc : connections) {
            if (mc.getName().equals(name.replaceAll("\\{|\\}", ""))
                    && mc.getNum() == num) {
                toRemove = mc;
                for (String rfid : mc.getRfid().keySet()) {
                    vertices.get(MGraph.IN).removeConnRfid(rfid);
                    vertices.get(MGraph.OUT).removeConnRfid(rfid);
                }
            }
        }
        if (toRemove != null) {
            connections.remove(toRemove);
        }
    }

    /**
     * Removes and RFID from one of the connections of this edge.
     * @param connName connection name
     * @param num connection number
     * @param rfidName RFID name
     */
    public void removeRfid(String connName, int num, String rfidName) {
        MConnection mc = getConnection(connName, num);
        mc.removeRfid(rfidName);
        vertices.get(MGraph.IN).removeConnRfid(rfidName);
        vertices.get(MGraph.OUT).removeConnRfid(rfidName);

    }

    /**
     * Gets the number of connections
     * @return number of connections
     */
    public int connectionsSize() {
        return connections.size();
    }

    /**
     * Gets a connection by its name and multiplicity. Connections between same
     * locations have the same name but different number.
     * @param name name of the connection
     * @param num number of the connection
     * @return needed connection
     */
    public MConnection getConnection(String name, int num) {
        MConnection conn = null;
        for (MConnection mc : connections) {
            if (mc.getName().equals(name.replaceAll("\\{|\\}", ""))
                    && mc.getNum() == num) {
                conn = mc;
            }
        }
        return conn;
    }

    /**
     * Gets a connection by its ID
     * @param id ID
     * @return needed connection
     */
    public MConnection getConnection(int id) {
        MConnection conn = null;
        for (MConnection mc : connections) {
            if (mc.getId() == id) {
                conn = mc;
            }
        }
        return conn;
    }

    /**
     * Gets the list of connections
     * @return list of connections
     */
    public List<MConnection> getConnections() {
        return connections;
    }

    /**
     * Sets the list of connections
     * @param connections list of connections
     */
    public void setConnections(List<MConnection> connections) {
        this.connections = connections;
    }

    /**
     * Gets a connected vertex
     * @param direction end of the edge
     * @return needed vertex
     */
    public MVertex getVertex(int direction) {
        return vertices.get(direction);
    }

    /**
     * Gets the name of this edge
     * @return name of the edge
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this edge
     * @param name name of the edge
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the ID of this edge
     * @return ID of this edge
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of this edge
     * @param id ID of this edge
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the combined name for this edges by grouping together all the
     * connections associated with this edge. This will appear on the graph.
     * @return combined name
     */
    public String getCombinedName() {
        if (!name.equals("")) {
            return name;
        }
        String cName = "";
        for (MConnection mc : connections) {
            if (mc.isDisplayNum()) {
                cName += "(" + mc.getName() + ")" + mc.getNum() + ", ";
            } else {
                cName += mc.getName() + ", ";
            }
        }
        return "{" + cName.substring(0, cName.length() - 2) + "}";
    }

    /**
     * Gets the combined name for this edges by grouping together all the
     * connections associated with this edge. This will appear on the RFID graph
     * @return combined name
     */
    public String getCombinedRfidName() {
        String rName = "";
        for (MConnection mc : connections) {
            if (mc.getRfid().keySet().size() > 1) {
                rName += "(";
            }
            String rfids[] = mc.getSortedRfids(vertices.get(MGraph.OUT));
            for (String r : rfids) {
                rName += r + ", ";
            }
            if (mc.getRfid().keySet().size() > 1) {
                rName = rName.substring(0, rName.length() - 2) + "), ";
            }
        }
        if (rName.equals("")) {
            return "Ø";
        }
        return "{" + rName.substring(0, rName.length() - 2) + "}";
    }

    /**
     * Display the multiplicity of a connection is necessary.
     * @param name name of the connection
     * @param display whether to display its number or not
     */
    public void setConnectionDisplayNum(String name, boolean display) {
        for (MConnection mc : connections) {
            if (mc.getName().equals(name.replaceAll("\\{|\\}", ""))) {
                mc.setDisplayNum(display);
            }
        }
    }

    /**
     * Gets the associated OrientEdge ID
     * @return OrientEdge ID
     */
    public Object getOgID() {
        return ogID;
    }

    /**
     * Sets the associated OrientEdge ID
     * @param ogID OrientEdge ID
     */
    public void setOgID(Object ogID) {
        this.ogID = ogID;
    }

    /**
     * Gets the map of vertices. The direction is the key and the vertex is the
     * value
     * @return map of vertices
     */
    public Map<Integer, MVertex> getVertices() {
        return vertices;
    }

    /**
     * Sets the map of vertices. The direction is the key and the vertex is the
     * value
     * @param vertices map of vertices
     */
    public void setVertices(Map<Integer, MVertex> vertices) {
        this.vertices = vertices;
    }
}
