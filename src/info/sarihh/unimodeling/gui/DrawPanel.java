package info.sarihh.unimodeling.gui;

import com.orientechnologies.orient.core.db.record.OTrackedList;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.type.tree.OMVRBTreeRIDSet;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import info.sarihh.unimodeling.midgraph.MConnection;
import info.sarihh.unimodeling.midgraph.MEdge;
import info.sarihh.unimodeling.midgraph.MGraph;
import info.sarihh.unimodeling.midgraph.MVertex;
import info.sarihh.unimodeling.utility.DrawConnection;
import info.sarihh.unimodeling.utility.DrawElement;
import info.sarihh.unimodeling.utility.DrawRfid;
import info.sarihh.unimodeling.utility.GraphicsUtilities;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JLayeredPane;
import javax.swing.SpinnerNumberModel;

/**
 * This is a JPanel containing modeling tools, the image and modeling layers.
 * Author: Sari Haj Hussein
 */
public class DrawPanel extends javax.swing.JPanel {

    protected static final int ORIGINAL = 0;
    protected static final int GRAPH = 1;
    protected static final int RFID = 2;

    /**
     * Creates a new DrawPanel. 
     * @param uniModelingGUI parent Window
     * @param planFile Space plan file (image)
     * @param oDbPath absolute path for OrientGraph databases (graph & rfid)
     */
    public DrawPanel(UniModelingGUI uniModelingGUI, File planFile, String oDbPath) {
        this.uniModelingGUI = uniModelingGUI;
        graphDbPath = "local:" + oDbPath + File.separator + "graph";
        rfidDbPath = "local:" + oDbPath + File.separator + "rfid";
        graphmlGraphPath = oDbPath + File.separator + "graph.graphml";
        graphmlRfidPath = oDbPath + File.separator + "rfid.graphml";

        initComponents();
        displayPlan(planFile);
        midGraph = new MGraph();
    }

    /** This method displays the specified OI-space plan in a JLabel on this
     * internal frame.
     * @param planFile OI-space plan
     */
    private void displayPlan(File planFile) {
        try {
            planImage = ImageIO.read(planFile);
        } catch (IOException ioe) {
            Logger.getLogger(OISpacePlanFrame.class.getName()).log(Level.SEVERE, null, ioe);
        }
        layeredPane.setPreferredSize(new Dimension(planImage.getWidth(),
                planImage.getHeight()));
        originalLayer = new OriginalLayer(this, planImage);
        drawLayer = new DrawLayer(new Dimension(planImage.getWidth(),
                planImage.getHeight()), this);
        graphLayer = new DrawLayer(new Dimension(planImage.getWidth(),
                planImage.getHeight()), this);
        rfidLayer = new DrawLayer(new Dimension(planImage.getWidth(),
                planImage.getHeight()), this);
        layeredPane.add(originalLayer, JLayeredPane.DEFAULT_LAYER, new Integer(1));
        originalToggleButton.setSelected(true);
        delimiterButton.setSelected(true);
    }

    /**
     * Adds a vertex to the midway graph.
     * @param e element on the modeler
     */
    protected void addMVertex(DrawElement e) {
        MVertex vertex = new MVertex(e.getName());
        vertex.setDelimCoordinates(e.getCoordinates());
        vertex.setGraphCoordinates(e.getCoordinates());
        vertex.setRfidCoordinates(e.getCoordinates());
        midGraph.addVertex(vertex);
        e.setMID(vertex.getId());
    }

    /**
     * Deletes a vertex from the midway graph
     * @param e element on the modeler
     */
    protected void deleteMVertex(DrawElement e) {
        midGraph.removeVertex(midGraph.getVertex(e.getMID()));
    }

    /**
     * Changes a component of the midway graph. The component can be: vertex,
     * edge, connection
     * @param e element on the modeler
     * @param oldName old name of the element
     */
    protected void changeMComponent(DrawElement e, String oldName) {
        switch (e.getType()) {
            case DrawLayer.DELIMITER: {
                MVertex vertex = midGraph.getVertex(e.getMID());
                vertex.setName(e.getName());
                vertex.setDelimCoordinates(e.getCoordinates());
            }
            break;
            case DrawLayer.VERTEX: {
                MVertex vertex = midGraph.getVertex(e.getMID());
                if (!e.getName().equals(vertex.getRfidName())) {
                    vertex.setName(e.getName());
                }
                if (selectedDesigner == GRAPH) {
                    vertex.setGraphCoordinates(e.getCoordinates());
                } else if (selectedDesigner == RFID) {
                    vertex.setRfidCoordinates(e.getCoordinates());
                }
            }
            break;
            case DrawLayer.CONNECTION:
                for (int i = 0; i < e.getConnections().size(); ++i) {
                    for (int j = 0; j < e.getConnections().size(); ++j) {
                        if (e.getConnectionMatrix()[i][j]) {
                            MEdge edge = midGraph.getEdge(e.getConnectionmID(i, j));
                            MConnection conn = edge.getConnection(oldName, ((DrawConnection) e).getNum());
                            conn.setName(e.getName());
                        }
                    }
                }
                break;
            case DrawLayer.EDGE: {
                MEdge edge = midGraph.getEdge(e.getMID());
                edge.setName(e.getName());
            }
            break;
            case DrawLayer.RFID: {
                DrawElement conn = e.getConnections().get(0);
                switch (conn.getType()) {
                    case DrawLayer.DELIMITER:
                        midGraph.getVertex(conn.getMID()).replaceRfid(oldName, e.getName());
                        break;
                    case DrawLayer.CONNECTION:
                        for (int i = 0; i < conn.getConnections().size(); ++i) {
                            for (int j = 0; j < conn.getConnections().size(); ++j) {
                                if (conn.getConnectionMatrix()[i][j]) {
                                    MEdge edge = midGraph.getEdge(e.getConnectionmID(i, j));
                                    MConnection mc = edge.getConnection(conn.getName(),
                                            ((DrawConnection) conn).getNum());
                                    mc.replaceRfid(oldName, e.getName(),
                                            ((DrawRfid) e).getRatio());
                                }
                            }
                        }
                        break;
                }
            }
            break;
        }
    }

    /**
     * Adds a connection to the midGraph. Meaning adds the necessary edges to
     * the graph that are coming from the connection matrix. It also calculates
     * the direction so that RFIDs will be indicated in order on the RFID graph.
     * @param e Connection object
     * @return additional elements on the space plan (if additional directions 
     * are specified TODO)
     */
    protected List<DrawElement> addMEdge(DrawElement e) {
        List<DrawElement> newArrows = new ArrayList<>();
        for (int i = 0; i < e.getConnections().size(); ++i) {
            for (int j = 0; j < e.getConnections().size(); ++j) {
                if (e.getConnectionMatrix()[i][j]) {
                    MVertex v1 = midGraph.getVertex(e.getConnections().get(i).getMID());
                    MVertex v2 = midGraph.getVertex(e.getConnections().get(j).getMID());
                    int o = ((DrawConnection) e).getOrientation();
                    double a;
                    double b;
                    if (o == DrawConnection.VERTICAL) {
                        a = e.getConnections().get(i).getCentralPoint().getY();
                        b = e.getConnections().get(j).getCentralPoint().getY();
                    } else {
                        a = e.getConnections().get(i).getCentralPoint().getX();
                        b = e.getConnections().get(j).getCentralPoint().getX();
                    }
                    MEdge edge = midGraph.getEdge(v1, v2);
                    if (edge != null) {
                        MConnection mc = new MConnection(e.getName(), ((DrawConnection) e).getNum());
                        mc.setPlusSide((a < b) ? v1 : v2);
                        edge.addConnection(mc);
                    } else {
                        edge = new MEdge(v1, v2, "");
                        MConnection mc = new MConnection(e.getName(), ((DrawConnection) e).getNum());
                        mc.setPlusSide((a < b) ? v1 : v2);
                        edge.addConnection(mc);
                        e.setMID(mc.getId());
                        midGraph.addEdge(edge);
                    }
                    if (((DrawConnection) e).getNum() > 1) {
                        edge.setConnectionDisplayNum(e.getName(), true);
                    }
                    e.setConnectionmID(i, j, edge.getId());
                }
            }
        }
        return newArrows;
    }

    /**
     * Deletes a connection and its edges from midGraph.
     * @param e connection
     * @return elements to be deleted from the space plan
     */
    protected List<DrawElement> deleteMEdge(DrawElement e) {
        List<DrawElement> removeEdges = new ArrayList<>();
        for (int i = 0; i < e.getConnections().size(); ++i) {
            for (int j = 0; j < e.getConnections().size(); ++j) {
                if (e.getConnectionMatrix()[i][j]) {
                    MEdge edge = midGraph.getEdge(e.getConnectionmID(i, j));
                    edge.removeConnection(e.getName(), ((DrawConnection) e).getNum());
                    for (DrawElement element : drawLayer.getElements()) { //directions
                        if (element.getType() == DrawLayer.DIRECTION) {
                            for (DrawElement elem : element.getConnections()) { //connections
                                if (elem.getType() == DrawLayer.CONNECTION
                                        && elem == e) {
                                    removeEdges.add(element);
                                }
                            }
                        }
                    }
                    if (edge.connectionsSize() == 0) {
                        midGraph.removeEdge(edge);
                    }
                }
            }
        }
        return removeEdges;
    }

    /**
     * Adds an RFID to the specified element. This method decides to which
     * element the RFID added to by checking its connections matrix.
     * @param e added RFID element
     */
    protected void addMRfid(DrawElement e) {
        DrawElement conn = e.getConnections().get(0);
        switch (conn.getType()) {
            case DrawLayer.DELIMITER:
                MVertex vertex = midGraph.getVertex(conn.getMID());
                vertex.addRfid(e.getName());
                break;
            case DrawLayer.CONNECTION:
                for (int i = 0; i < conn.getConnections().size(); ++i) {
                    for (int j = 0; j < conn.getConnections().size(); ++j) {
                        if (conn.getConnectionMatrix()[i][j]) {
                            MEdge edge = midGraph.getEdge(conn.getConnectionmID(i, j));
                            MConnection mc = edge.getConnection(conn.getName(), ((DrawConnection) conn).getNum());
                            double ratio = ((DrawRfid) e).getRatio();
                            mc.addRfid(e.getName(), ratio);
                            MVertex v1 = edge.getVertex(MGraph.IN);
                            MVertex v2 = edge.getVertex(MGraph.OUT);
                            double rat1 = (ratio > 0) ? ratio : 1 + ratio;
                            double rat2 = (ratio > 0) ? 1 - ratio : -ratio;
                            v1.addConnRfid(e.getName(), 0.5);
                            v2.addConnRfid(e.getName(), 0.5);
                        }
                    }
                }
                break;
        }
    }

    /**
     * Deletes an RFID. It checks to which elements it is attached, and remove
     * itself from the element's connections.
     * @param e RFID to be removed
     */
    protected void deleteMRfid(DrawElement e) {
        DrawElement conn = e.getConnections().get(0);
        switch (conn.getType()) {
            case DrawLayer.DELIMITER:
                midGraph.getVertex(conn.getMID()).removeRfid(e.getName());
                break;
            case DrawLayer.CONNECTION:
                for (int i = 0; i < conn.getConnections().size(); ++i) {
                    for (int j = 0; j < conn.getConnections().size(); ++j) {
                        if (conn.getConnectionMatrix()[i][j]) {
                            MEdge edge = midGraph.getEdge(conn.getConnectionmID(i, j));
                            edge.removeRfid(conn.getName(), ((DrawConnection) conn).getNum(), e.getName());
                        }
                    }
                }
                break;
        }
    }

    /**
     * Gets the RFID coverage weights of a vertex on the modeler
     * @param e element on the modeler
     * @return coverage weights
     */
    public Set<String> getCoverageWeights(DrawElement e) {
        if (e.getType() == DrawLayer.VERTEX) {
            return midGraph.getVertex(e.getMID()).getCoverageWeightSet();
        } else {
            return new HashSet<>();
        }
    }

    /**
     * Sets the RFID coverage weights of an element on the modeler
     * @param e element on the modeler
     * @param cwdata coverage weights
     */
    public void setCoverageWeights(DrawElement e, Set<String> cwdata) {
        if (e.getType() == DrawLayer.VERTEX) {
            MVertex mv = midGraph.getVertex(e.getMID());
            Set<MVertex> changedVertices = new HashSet<>();
            changedVertices.add(mv);
            for (String s : cwdata) {
                String data[] = s.split("->");
                mv.removeConnRfid(data[0]);
                mv.addConnRfid(data[0], Double.parseDouble(data[1]));
                MVertex rfidVertex = null;
                for (MEdge edge : mv.getEdges().keySet()) {
                    if (edge.getCombinedRfidName().contains(data[0])) {
                        if (mv.getEdges().get(edge) == MGraph.IN) {
                            rfidVertex = edge.getVertex(MGraph.OUT);
                        } else {
                            rfidVertex = edge.getVertex(MGraph.IN);
                        }
                    }
                }
                if (rfidVertex != null) {
                    rfidVertex.removeConnRfid(data[0]);
                    rfidVertex.addConnRfid(data[0], 1.0 - Double.parseDouble(data[1]));
                    drawLayer.getElement(rfidVertex.getId()).setProperties(rfidVertex.getRfidStats());
                    changedVertices.add(rfidVertex);
                }
            }
            e.setProperties(mv.getRfidStats());
            OrientGraph rfidGraph = new OrientGraph(rfidDbPath);
            for (MVertex vx : changedVertices) {
                Vertex vertex = rfidGraph.getVertex(vx.getOgID());
                vertex.setProperty("c_l", vx.getLabels());
                vertex.setProperty("c_r", vx.getCoverageWeightSet());
            }
            rfidGraph.shutdown();
        }
    }

    /**
     * Gets the associated OrientGraph DB properties for the element on the 
     * modeler
     * @param e element on the modeler
     * @return properties
     */
    public Map<String, Object> getProperties(DrawElement e) {
        OrientGraph rfidGraph = new OrientGraph(rfidDbPath);
        Map<String, Object> map = new HashMap<>();
        switch (e.getType()) {
            case DrawLayer.VERTEX:
                Vertex vertex = rfidGraph.getVertex(midGraph.getVertex(e.getMID()).getOgID());
                for (String key : vertex.getPropertyKeys()) {
                    map.put(key, vertex.getProperty(key));
                }
                break;
            case DrawLayer.EDGE:
                Edge edge = rfidGraph.getEdge(midGraph.getEdge(e.getMID()).getOgID());
                for (String key : edge.getPropertyKeys()) {
                    map.put(key, edge.getProperty(key));
                }
        }
        rfidGraph.shutdown();
        return map;
    }

    /**
     * Sets the associated properties of an element on the modeler
     * @param e element on the modeler
     * @param map properties
     */
    public void setProperties(DrawElement e, Map<String, Object> map) {
        OrientGraph rfidGraph = new OrientGraph(rfidDbPath);
        switch (e.getType()) {
            case DrawLayer.VERTEX:
                Vertex vertex = rfidGraph.getVertex(midGraph.getVertex(e.getMID()).getOgID());
                for (String key : vertex.getPropertyKeys()) {
                    vertex.removeProperty(key);
                }
                for (String key : map.keySet()) {
                    vertex.setProperty(key, map.get(key));
                }
                break;
            case DrawLayer.EDGE:
                Edge edge = rfidGraph.getEdge(midGraph.getEdge(e.getMID()).getOgID());
                for (String key : edge.getPropertyKeys()) {
                    edge.removeProperty(key);
                }
                for (String key : map.keySet()) {
                    edge.setProperty(key, map.get(key));
                }
        }
        rfidGraph.shutdown();
    }

    /**
     * Gets the name of an element on the modeler
     * @param e element on the modeler
     * @return element's name
     */
    public String getComponentName(DrawElement e) {
        switch (e.getType()) {
            case DrawLayer.DELIMITER:
            case DrawLayer.VERTEX:
                return midGraph.getVertex(e.getMID()).getName();
        }
        return "";
    }

    /**
     * This method computes the observability of the route that passes through
     * the specified array of semantic locations in the specified RFID readers
     * deployment pseudograph.
     * @param routeLocations route locations
     * @return observability
     */
    public double getObs(String[] routeLocations) {
        OrientGraph rfidGraph = new OrientGraph(rfidDbPath);
        double obs = 0.0;
        // sum over all route semantic locations
        for (String location : routeLocations) {
            OTrackedList<String> c_r = get_c_r(rfidGraph, location);
            // sum over all the weights in a semantic location
            for (String weightString : c_r) {
                StringTokenizer st = new StringTokenizer(weightString, "->");
                st.nextToken();
                double weight = Double.parseDouble(st.nextToken());
                obs += log2(weight + 1); // -Infinity cannot happen
            }
        }
        rfidGraph.shutdown();
        return obs;
    }

    /**
     * This method returns the coverage weight of RFID readers in the specified
     * semantic location of the specified RFID readers deployment pseudograph.
     * @param rfidGraph OrientGraph
     * @param location location name
     * @return coverage weight
     */
    private OTrackedList<String> get_c_r(OrientGraph rfidGraph, String location) {
        List<ODocument> result = rfidGraph.getRawGraph().query(
                new OSQLSynchQuery("select * from V where name = '" + location + "'"));
        // In some mysterious way, Orient DB converts the HashSet<String>, I used
        // to store c_r in the class GraphModeling, into OTrackedList<String> !!!
        OTrackedList<String> c_r = result.get(0).field("c_r");
        return c_r;
    }

    /** 
     * This method computes the upper bound of the observability of the route
     * that passes through the specified array of semantic locations in the
     * specified RFID readers deployment pseudograph.
     * @param routeLocations route locations
     * @return upper bound
     */
    public double getUpperBound(String[] routeLocations) {
        OrientGraph rfidGraph = new OrientGraph(rfidDbPath);
        double upperBound = 0.0;
        // sum over all route semantic locations
        for (String location : routeLocations) {
            OTrackedList<String> c_r = get_c_r(rfidGraph, location);
            int c_r_cardinality = c_r.size();
            double c_r_bar = 0.0;
            // sum over all the weights in a semantic location
            for (String weightString : c_r) {
                StringTokenizer st = new StringTokenizer(weightString, "->");
                st.nextToken();
                double weight = Double.parseDouble(st.nextToken());
                c_r_bar += weight;
            }
            if (c_r_bar + c_r_cardinality != 0) { // avoid -Infinity
                upperBound += log2(c_r_bar + c_r_cardinality);
            }
        }
        rfidGraph.shutdown();
        return upperBound;
    }

    /** 
     * This method returns the binary logarithm of the specified number.
     * @param x number
     * @return log2 of number
     */
    private double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    /** 
     * This method computes the static bottleneck point estimate of the specified
     * semantic location in the specified OI-space pseudograph.
     * @param location location name
     * return static bottleneck point estimate
     */
    public double getStaticBPEstimate(String location) {
        OrientGraph oiSpaceGraph = new OrientGraph(graphDbPath);
        double d = getPseudoDegree(oiSpaceGraph, location) / (2.0 * getNumEdges(oiSpaceGraph));
        oiSpaceGraph.shutdown();
        return d;
    }

    /** 
     * This method returns the pseudodegree of the specified semantic location
     * in the specified OI-space pseudograph. 
     * @param oispaceGraph OrientGraph
     * @param location location name
     * @return pseudo degree
     */
    private int getPseudoDegree(OrientGraph oispaceGraph, String location) {
        List<ODocument> result = oispaceGraph.getRawGraph().query(
                new OSQLSynchQuery("select * from V where name = '" + location + "'"));
        OMVRBTreeRIDSet in = result.get(0).field("in");
        OMVRBTreeRIDSet out = result.get(0).field("out");
        int inDegree = 0, outDegree = 0;
        if (in != null) {
            inDegree = in.size();
        }
        if (out != null) {
            outDegree = out.size();
        }
        int degree = inDegree + outDegree;
        return degree;
    }

    /** 
     * This method returns the number of edges in the specified OI-space
     * pseudograph.
     * @param oispaceGraph OrientGraph
     * @return number of edges
     */
    private long getNumEdges(OrientGraph oispaceGraph) {
        List<ODocument> result = oispaceGraph.getRawGraph().query(
                new OSQLSynchQuery("select count(*) from E"));
        long numEdges = result.get(0).field("count");
        return numEdges;
    }

    /**
     * Gets the selected designer. (PLAN, GRAPH, RFID)
     * @return selected designer
     */
    public int getSelectedDesigner() {
        return selectedDesigner;
    }

    /**
     * Dims all the elements on the modeler
     * @param dim true to dim
     */
    public void dimGraphics(boolean dim) {
        for (DrawElement e : selectedLayer.getElements()) {
            e.setDimmed(dim);
        }
        selectedLayer.repaint();
    }

    /**
     * Hides all the elements on the modeler
     * @param hide true to hide
     */
    public void hideGraphics(boolean hide) {
        for (DrawElement e : selectedLayer.getElements()) {
            e.setVisible(!hide);
        }
        selectedLayer.repaint();
    }

    /**
     * Dimes the background
     * @param dim true to dim
     */
    public void dimPlan(boolean dim) {
        originalLayer.setDimmed(dim);
    }

    /**
     * Hides the background
     * @param hide true to hide
     */
    public void hidePlan(boolean hide) {
        originalLayer.setOff(hide);
    }

    /**
     * Gets all the elements from the modeler
     * @return elements
     */
    public List<DrawElement> saveElements() {
        return drawLayer.getElements();
    }

    /**
     * Adds the loaded elements
     * @param elements elements
     */
    public void loadElements(List<DrawElement> elements) {
        drawLayer.addElements(elements);
    }

    /**
     * Gets the midway graph to save
     * @return midway graph
     */
    public MGraph saveMidGraph() {
        return midGraph;
    }

    /**
     * Sets the midway graph
     * @param graph midway graph
     */
    public void loadMidGraph(MGraph graph) {
        midGraph = graph;
    }

    /**
     * Gets the background image
     * @return background image
     */
    public BufferedImage getPlanImage() {
        return planImage;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonPanel = new javax.swing.JPanel();
        originalToggleButton = new javax.swing.JToggleButton();
        graphToggleButton = new javax.swing.JToggleButton();
        rfidToggleButton = new javax.swing.JToggleButton();
        delimiterButton = new javax.swing.JToggleButton();
        edgeButton = new javax.swing.JToggleButton();
        rfidButton = new javax.swing.JToggleButton();
        moveButton = new javax.swing.JToggleButton();
        resizeButton = new javax.swing.JToggleButton();
        applyToLabel = new javax.swing.JLabel();
        applyToComboBox = new javax.swing.JComboBox();
        percentageLabel = new javax.swing.JLabel();
        percentageSpinner = new javax.swing.JSpinner();
        fitButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        layeredPane = new javax.swing.JLayeredPane();

        setLayout(new java.awt.BorderLayout());

        originalToggleButton.setText("Space plan");
        originalToggleButton.setToolTipText("OI-space plan designer");
        originalToggleButton.setFocusable(false);
        originalToggleButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                originalToggleButtonItemStateChanged(evt);
            }
        });
        buttonPanel.add(originalToggleButton);

        graphToggleButton.setText("Space Graph");
        graphToggleButton.setToolTipText("OI-space graph designer");
        graphToggleButton.setFocusable(false);
        graphToggleButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                graphToggleButtonItemStateChanged(evt);
            }
        });
        buttonPanel.add(graphToggleButton);

        rfidToggleButton.setText("RFID Graph");
        rfidToggleButton.setToolTipText("RFID graph designer");
        rfidToggleButton.setFocusable(false);
        rfidToggleButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rfidToggleButtonItemStateChanged(evt);
            }
        });
        buttonPanel.add(rfidToggleButton);

        delimiterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/sarihh/unimodeling/gui/delim.png"))); // NOI18N
        delimiterButton.setToolTipText("Semantic location");
        delimiterButton.setFocusable(false);
        delimiterButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                delimiterButtonItemStateChanged(evt);
            }
        });
        buttonPanel.add(delimiterButton);

        edgeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/sarihh/unimodeling/gui/conn.png"))); // NOI18N
        edgeButton.setToolTipText("Connection point");
        edgeButton.setFocusable(false);
        edgeButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                edgeButtonItemStateChanged(evt);
            }
        });
        buttonPanel.add(edgeButton);

        rfidButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/sarihh/unimodeling/gui/rfid.png"))); // NOI18N
        rfidButton.setToolTipText("RFID reader");
        rfidButton.setFocusable(false);
        rfidButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rfidButtonItemStateChanged(evt);
            }
        });
        buttonPanel.add(rfidButton);

        moveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/sarihh/unimodeling/gui/move.png"))); // NOI18N
        moveButton.setToolTipText("Move");
        moveButton.setFocusable(false);
        moveButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                moveButtonItemStateChanged(evt);
            }
        });
        buttonPanel.add(moveButton);

        resizeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/info/sarihh/unimodeling/gui/resize.png"))); // NOI18N
        resizeButton.setToolTipText("Resize");
        resizeButton.setFocusable(false);
        resizeButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                resizeButtonItemStateChanged(evt);
            }
        });
        buttonPanel.add(resizeButton);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("info/sarihh/unimodeling/gui/OISpacePlanFrame"); // NOI18N
        applyToLabel.setText(bundle.getString("OISpacePlanFrame.applyToLabel.text")); // NOI18N
        buttonPanel.add(applyToLabel);

        applyToComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Width only", "Height only", "Width and height" }));
        applyToComboBox.setSelectedIndex(2);
        buttonPanel.add(applyToComboBox);

        percentageLabel.setText(bundle.getString("OISpacePlanFrame.percentageLabel.text")); // NOI18N
        buttonPanel.add(percentageLabel);

        percentageSpinner.setModel(new SpinnerNumberModel(100, 1, 5000, 1));
        percentageSpinner.setEditor(new javax.swing.JSpinner.NumberEditor(percentageSpinner, "#"));
        percentageSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                percentageSpinnerStateChanged(evt);
            }
        });
        buttonPanel.add(percentageSpinner);

        fitButton.setText(bundle.getString("OISpacePlanFrame.fitButton.text")); // NOI18N
        fitButton.setToolTipText("Fit size");
        fitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fitButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(fitButton);

        add(buttonPanel, java.awt.BorderLayout.NORTH);

        scrollPane.setPreferredSize(new java.awt.Dimension(500, 500));

        layeredPane.setPreferredSize(new java.awt.Dimension(1000, 1000));
        scrollPane.setViewportView(layeredPane);

        add(scrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void percentageSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_percentageSpinnerStateChanged
        int resizePercentage = (int) percentageSpinner.getValue();
        String applyTo = (String) applyToComboBox.getSelectedItem();
        BufferedImage resizedPlanImage = null;
        float ratioX = 1.0f;
        float ratioY = 1.0f;
        switch (applyTo) {
            case "Width only":
                resizedPlanImage = GraphicsUtilities.getScaledInstance(planImage,
                        planImage.getWidth() * resizePercentage / 100,
                        layeredPane.getPreferredSize().height,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, false);
                ratioX = (float) resizePercentage / 100.0f;
                break;
            case "Height only":
                resizedPlanImage = GraphicsUtilities.getScaledInstance(planImage,
                        layeredPane.getPreferredSize().width,
                        planImage.getHeight() * resizePercentage / 100,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, false);
                ratioY = (float) resizePercentage / 100.0f;
                break;
            case "Width and height":
                resizedPlanImage = GraphicsUtilities.getScaledInstance(planImage,
                        planImage.getWidth() * resizePercentage / 100,
                        planImage.getHeight() * resizePercentage / 100,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, false);
                ratioX = (float) resizePercentage / 100.0f;
                ratioY = (float) resizePercentage / 100.0f;
                break;
            default:
                resizedPlanImage = GraphicsUtilities.getScaledInstance(planImage,
                        planImage.getWidth() * resizePercentage / 100,
                        planImage.getHeight() * resizePercentage / 100,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, false);
                ratioX = (float) resizePercentage / 100.0f;
                ratioY = (float) resizePercentage / 100.0f;
                break;
        }
        originalLayer.setImage(resizedPlanImage);
        layeredPane.setPreferredSize(new Dimension(resizedPlanImage.getWidth(),
                resizedPlanImage.getHeight()));
        drawLayer.resize(ratioX, ratioY);
        revalidate();
    }//GEN-LAST:event_percentageSpinnerStateChanged

    private void fitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fitButtonActionPerformed
        float ratioW = (float) (scrollPane.getWidth() - 10) / planImage.getWidth();
        float ratioH = (float) (scrollPane.getHeight() - 10) / planImage.getHeight();
        float ratio = (ratioW < ratioH) ? ratioW : ratioH;
        BufferedImage resizedPlanImage = GraphicsUtilities.getScaledInstance(planImage,
                Math.round(planImage.getWidth() * ratio),
                Math.round(planImage.getHeight() * ratio),
                RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);
        percentageSpinner.setValue(Math.round(ratio * 100));
        originalLayer.setImage(resizedPlanImage);
        layeredPane.setPreferredSize(new Dimension(resizedPlanImage.getWidth(),
                resizedPlanImage.getHeight()));
        drawLayer.resize(ratio, ratio);
        revalidate();
    }//GEN-LAST:event_fitButtonActionPerformed

    private void originalToggleButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_originalToggleButtonItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            selectedDesigner = ORIGINAL;
            if (selectedLayer != null) {
                layeredPane.remove(selectedLayer);
            }
            layeredPane.add(drawLayer, JLayeredPane.DEFAULT_LAYER, new Integer(0));
            selectedLayer = drawLayer;
            graphToggleButton.setSelected(false);
            rfidToggleButton.setSelected(false);
            delimiterButton.setEnabled(true);
            edgeButton.setEnabled(true);
            rfidButton.setEnabled(true);
            drawLayer.setTool(DrawLayer.DELIMITER);
        } else {
            if (!(graphToggleButton.isSelected() || rfidToggleButton.isSelected())) {
                originalToggleButton.setSelected(true);
            }
        }
    }//GEN-LAST:event_originalToggleButtonItemStateChanged

    private void delimiterButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_delimiterButtonItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            edgeButton.setSelected(false);
            rfidButton.setSelected(false);
            moveButton.setSelected(false);
            resizeButton.setSelected(false);
            switch (selectedDesigner) {
                case ORIGINAL:
                    selectedLayer.setTool(DrawLayer.DELIMITER);
                    break;
                case GRAPH:
                case RFID:
                    selectedLayer.setTool(DrawLayer.VERTEX);
            }
        } else {
            if (!(edgeButton.isSelected() || rfidButton.isSelected()
                    || moveButton.isSelected() || resizeButton.isSelected())) {
                delimiterButton.setSelected(true);
            }
        }
    }//GEN-LAST:event_delimiterButtonItemStateChanged

    private void edgeButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_edgeButtonItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            rfidButton.setSelected(false);
            delimiterButton.setSelected(false);
            moveButton.setSelected(false);
            resizeButton.setSelected(false);
            switch (selectedDesigner) {
                case ORIGINAL:
                    selectedLayer.setTool(DrawLayer.CONNECTION);
                    break;
                case GRAPH:
                case RFID:
                    selectedLayer.setTool(DrawLayer.EDGE);
            }
        } else {
            if (!(delimiterButton.isSelected() || rfidButton.isSelected()
                    || moveButton.isSelected() || resizeButton.isSelected())) {
                edgeButton.setSelected(true);
            }
        }
    }//GEN-LAST:event_edgeButtonItemStateChanged

    private void rfidButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rfidButtonItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            delimiterButton.setSelected(false);
            edgeButton.setSelected(false);
            moveButton.setSelected(false);
            resizeButton.setSelected(false);
            selectedLayer.setTool(DrawLayer.RFID);
        } else {
            if (!(delimiterButton.isSelected() || edgeButton.isSelected()
                    || moveButton.isSelected() || resizeButton.isSelected())) {
                rfidButton.setSelected(true);
            }
        }
    }//GEN-LAST:event_rfidButtonItemStateChanged

    private void graphToggleButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_graphToggleButtonItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            selectedDesigner = GRAPH;
            if (selectedLayer != null) {
                layeredPane.remove(selectedLayer);
                midGraph.convertToGraph(graphDbPath, graphmlGraphPath);
                graphLayer.clearElements();
                graphLayer.addElements(midGraph.createDrawGraph());

            }
            layeredPane.add(graphLayer, JLayeredPane.DEFAULT_LAYER, new Integer(0));
            selectedLayer = graphLayer;
            originalToggleButton.setSelected(false);
            rfidToggleButton.setSelected(false);
            delimiterButton.setEnabled(false);
            edgeButton.setEnabled(false);
            rfidButton.setEnabled(false);
            graphLayer.setTool(DrawLayer.VERTEX);
        } else {
            if (!(originalToggleButton.isSelected() || rfidToggleButton.isSelected())) {
                graphToggleButton.setSelected(true);
            }
        }
    }//GEN-LAST:event_graphToggleButtonItemStateChanged

    private void rfidToggleButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rfidToggleButtonItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            selectedDesigner = RFID;
            if (selectedLayer != null) {
                layeredPane.remove(selectedLayer);
                midGraph.convertToRfidGraph(rfidDbPath, graphmlRfidPath);
                rfidLayer.clearElements();
                rfidLayer.addElements(midGraph.createDrawRfidGraph());
            }
            layeredPane.add(rfidLayer, JLayeredPane.DEFAULT_LAYER, new Integer(0));
            selectedLayer = rfidLayer;
            graphToggleButton.setSelected(false);
            originalToggleButton.setSelected(false);
            delimiterButton.setEnabled(false);
            edgeButton.setEnabled(false);
            rfidButton.setEnabled(false);
            rfidLayer.setTool(DrawLayer.VERTEX);
        } else {
            if (!(graphToggleButton.isSelected() || originalToggleButton.isSelected())) {
                rfidToggleButton.setSelected(true);
            }
        }
    }//GEN-LAST:event_rfidToggleButtonItemStateChanged

    private void moveButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_moveButtonItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            edgeButton.setSelected(false);
            rfidButton.setSelected(false);
            delimiterButton.setSelected(false);
            resizeButton.setSelected(false);
            switch (selectedDesigner) {
                case ORIGINAL:
                case GRAPH:
                case RFID:
                    selectedLayer.setTool(DrawLayer.MOVE);
            }
        } else {
            if (!(edgeButton.isSelected() || rfidButton.isSelected()
                    || delimiterButton.isSelected() || resizeButton.isSelected())) {
                moveButton.setSelected(true);
            }
        }
    }//GEN-LAST:event_moveButtonItemStateChanged

    private void resizeButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_resizeButtonItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            edgeButton.setSelected(false);
            rfidButton.setSelected(false);
            moveButton.setSelected(false);
            delimiterButton.setSelected(false);
            switch (selectedDesigner) {
                case ORIGINAL:
                case GRAPH:
                case RFID:
                    selectedLayer.setTool(DrawLayer.RESIZE);
            }
        } else {
            if (!(edgeButton.isSelected() || rfidButton.isSelected()
                    || moveButton.isSelected() || delimiterButton.isSelected())) {
                resizeButton.setSelected(true);
            }
        }
    }//GEN-LAST:event_resizeButtonItemStateChanged
    /** Parent frame */
    private UniModelingGUI uniModelingGUI = null;
    /** Background image */
    private BufferedImage planImage = null;
    /** Background layer */
    private OriginalLayer originalLayer = null;
    /** Modeler layer */
    private DrawLayer drawLayer = null;
    /** Graph layer */
    private DrawLayer graphLayer = null;
    /** RFID layer */
    private DrawLayer rfidLayer = null;
    /** Selected layer */
    private DrawLayer selectedLayer = null;
    /** Selected designer/modeler */
    private int selectedDesigner;
    /** OrientGraph DB path */
    private String graphDbPath;
    /** RFID OrientGraph DB path */
    private String rfidDbPath;
    /** Graphml file path */
    private String graphmlGraphPath;
    /** RFID graphml file path */
    private String graphmlRfidPath;
    /** Midway graph */
    private MGraph midGraph;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox applyToComboBox;
    private javax.swing.JLabel applyToLabel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JToggleButton delimiterButton;
    private javax.swing.JToggleButton edgeButton;
    private javax.swing.JButton fitButton;
    private javax.swing.JToggleButton graphToggleButton;
    private javax.swing.JLayeredPane layeredPane;
    private javax.swing.JToggleButton moveButton;
    private javax.swing.JToggleButton originalToggleButton;
    private javax.swing.JLabel percentageLabel;
    private javax.swing.JSpinner percentageSpinner;
    private javax.swing.JToggleButton resizeButton;
    private javax.swing.JToggleButton rfidButton;
    private javax.swing.JToggleButton rfidToggleButton;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
}
