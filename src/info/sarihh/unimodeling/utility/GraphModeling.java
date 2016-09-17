package info.sarihh.unimodeling.utility;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * This class deals with the Orient graph database. It creates the OI-space
 * pseudograph, and transforms it into the RFID readers deployment pseudograph.
 * In addition, it enables adding customized properties to the vertices and edges
 * of the latter.
 * Author: Sari Haj Hussein
 */
public class GraphModeling {

    /** Not used.
     * This method creates or otherwise opens a local Orient graph database at
     * the specified path in the OrientDB native raw implementation. */
    public static OGraphDatabase createLocalOrientDatabase(String dbPath) {
        OGraphDatabase db = new OGraphDatabase("local:" + dbPath);
        if (!db.exists()) {
            db.create();
        } else {
            db.open("admin", "admin");
        }
        return db;
    }

    /** Not used.
     * This method closes the specified Orient graph database. */
    public static void closeDatabase(OGraphDatabase db) {
        db.close();
    }

    /** Not used.
     * This method creates an empty Orient graph object corresponding to the
     * specified Orient graph database that implements the Blueprint API. */
    public static OrientGraph createOrientGraph(OGraphDatabase db) {
        return new OrientGraph(db);
    }

    /** This method creates or otherwise opens an Orient graph object at the
     * specified path. This objects implements the Blueprint API. */
    public static OrientGraph createOrientGraph(String path) {
        return new OrientGraph("local:" + path);
    }

    /** This method returns a simplified printout of the specified Orient graph
     * in the form vertex1 -> edge_label -> vertex2. */
    public static StringBuilder printGraph(OrientGraph orientGraph) {
        StringBuilder sb = new StringBuilder();
        for (Edge e : orientGraph.getEdges()) {
            sb.append(e.getVertex(Direction.OUT).getProperty("name")).append(" -> ").
                    append(e.getLabel()).append(" -> ").
                    append(e.getVertex(Direction.IN).getProperty("name")).append("\n");
        }
        return sb;
    }

    /** This method clears the specified Orient graph from all its vertices and
     * edges. */
    public static void clearGraph(OrientGraph orientGraph) {
        for (Vertex v : orientGraph.getVertices()) {
            orientGraph.removeVertex(v);
        }
        for (Edge e : orientGraph.getEdges()) {
            orientGraph.removeEdge(e);
        }
    }

    /** This method shuts down the specified Orient graph closing all open
     * transactions. */
    public static void shutdownGraph(OrientGraph orientGraph) {
        orientGraph.shutdown();
    }

    /** This method saves the specified Orient graph under GraphML format in the
     * specified file path. */
    public static void saveGraphMLFile(OrientGraph orientGraph, String graphmlFilePath) {
        try {
            FileOutputStream out = new FileOutputStream(graphmlFilePath);
            GraphMLWriter.outputGraph(orientGraph, out);
        } catch (IOException ioe) {
            Logger.getLogger(GraphModeling.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }

    /** This method creates the OI-space pseudograph in the specified Orient
     * graph object. This is done using the specified sets of semantic locations
     * W_l, binary sub-routes W_m, and edge labels c(W_m). */
    public static void createOISpaceGraph(OrientGraph oispaceGraph,
            String[] W_l_array, String[] W_m_array, String[] c_array) {
        // edge labeling using c: W_m -> P(W_c)
        HashMap<String, String> c = new HashMap<>();
        for (int i = 0; i < W_m_array.length; i++) {
            c.put(W_m_array[i], c_array[i]);
        }
        // create vertices
        for (String l : W_l_array) {
            Vertex v = oispaceGraph.addVertex(null);
            v.setProperty("name", l);
        }
        // cretae edges
        for (String m : W_m_array) {
            String outL = m.substring(1, m.indexOf(','));
            String inL = m.substring(m.indexOf(',') + 1, m.indexOf(')'));
            Vertex outV = oispaceGraph.getVertices("name", outL).iterator().next();
            Vertex inV = oispaceGraph.getVertices("name", inL).iterator().next();
            Edge e = oispaceGraph.addEdge(null, outV, inV, m);
            e.setProperty("c", c.get(m));
        }
    }

    /** This method transforms the OI-space pseudograph specified by W_l, W_m,
     * and c(W_m) into an RFID readers deployment pseudograph. This is done
     * using the specified sets of connection points W_c, and readers W_r. */
    public static void transformOIspace2RFID(OrientGraph rfidGraph, String[] W_c_array,
            String[] W_r_array, String[] W_l_array, String[] W_m_array, String[] c_array) {
        ConcurrentSkipListSet<String> W_r = new ConcurrentSkipListSet<>();
        Collections.addAll(W_r, W_r_array);

        // Stage 1. Copying stage.
        JOptionPane.showMessageDialog(null,
                "is executing",
                "Stage 1. Copying stage",
                JOptionPane.INFORMATION_MESSAGE);
        createOISpaceGraph(rfidGraph, W_l_array, W_m_array, c_array);
        for (Edge e : rfidGraph.getEdges()) { // remove the redundant c(W_m)
            e.removeProperty("c");
        }

        // Stage 2. Initialization stage.
        JOptionPane.showMessageDialog(null,
                "is executing",
                "Stage 2. Initialization stage",
                JOptionPane.INFORMATION_MESSAGE);
        for (Vertex v : rfidGraph.getVertices()) {
            v.setProperty("c_l", new HashSet<String>());
            v.setProperty("c_r", new HashSet<String>());
        }
        for (Edge e : rfidGraph.getEdges()) {
            e.setProperty("c_m", new HashSet<String>());
        }

        // Stage 3. Vertex labeling stage.
        JOptionPane.showMessageDialog(null,
                "is executing",
                "Stage 3. Vertex labeling stage",
                JOptionPane.INFORMATION_MESSAGE);
        for (Vertex v : rfidGraph.getVertices()) {
            String answer = (String) JOptionPane.showInputDialog( // G1
                    null,
                    "What are the readers that are positioned in " + v.getProperty("name")
                    + " away from any connection point?\n" + W_r.toString() + " (enter no if none): ",
                    "Stage 3. Vertex labeling stage",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    v.getProperty("name").equals("MC") ? "r1" : "no");
            if (answer == null) { // if the user cancels the input, then set it to no
                answer = "no";
            }
            if (!answer.equalsIgnoreCase("no")) {
                StringTokenizer st = new StringTokenizer(answer, ",");
                while (st.hasMoreTokens()) {
                    String r = st.nextToken();
                    HashSet<String> c_l = (HashSet<String>) v.getProperty("c_l");
                    c_l.add(r);
                    v.setProperty("c_l", c_l);
                    W_r.remove(r);
                }
            }
        }

        // Stage 4. Edge labeling stage.
        for (String cp : W_c_array) {
            String val = null;
            switch (cp) {
                case "(CH|CGS)1":
                    val = "r4";
                    break;
                case "(CH|CGS)2":
                    val = "r5";
                    break;
                case "(OC|CGS)1":
                    val = "r4";
                    break;
                case "(OC|CGS)2":
                    val = "r5";
                    break;
                case "GS1|BL1":
                    val = "r6";
                    break;
                case "GS2|BL2":
                    val = "r7";
                    break;
                case "GS3|BL3":
                    val = "r8";
                    break;
                default:
                    val = "no";
                    break;
            }
            String answer = (String) JOptionPane.showInputDialog( // G2
                    null,
                    "What are the readers that are positioned at " + cp
                    + "?\n" + W_r.toString() + " (enter no if none): ",
                    "Stage 4. Edge labeling stage",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    val);
            if (answer == null) { // if the user cancels the input, then set it to no
                answer = "no";
            }
            if (!answer.equalsIgnoreCase("no")) {
                StringTokenizer st = new StringTokenizer(answer, ",");
                while (st.hasMoreTokens()) {
                    String r = st.nextToken();
                    for (Edge e : rfidGraph.getEdges()) {
                        Vertex outV = e.getVertex(Direction.OUT);
                        Vertex inV = e.getVertex(Direction.IN);
                        String outL = outV.getProperty("name").toString();
                        String inL = inV.getProperty("name").toString();
                        String lookUp = outL + "|" + inL;
                        if (cp.contains(lookUp)) {
                            HashSet<String> c_m = (HashSet<String>) e.getProperty("c_m");
                            c_m.add(r);
                            e.setProperty("c_m", c_m);
                        }
                    }
                }
            }

            val = null;
            switch (cp) {
                case "SMC|TTS":
                    val = "r2,r3";
                    break;
                case "TTS|TTS":
                    val = "r2,r3";
                    break;
                default:
                    val = "no";
                    break;
            }
            answer = (String) JOptionPane.showInputDialog( // G2
                    null,
                    "What are the readers that are adjacently positioned at " + cp
                    + "?\n" + W_r.toString() + " (enter no if none): ",
                    "Stage 4. Edge labeling stage",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    val);
            if (answer == null) { // if the user cancels the input, then set it to no
                answer = "no";
            }
            if (!answer.equalsIgnoreCase("no")) {
                StringTokenizer st = new StringTokenizer(answer, ",");
                if (st.hasMoreTokens()) {
                    String r = st.nextToken();
                    String rPrime = st.nextToken();
                    for (Edge e : rfidGraph.getEdges()) {
                        Vertex outV = e.getVertex(Direction.OUT);
                        Vertex inV = e.getVertex(Direction.IN);
                        String outL = outV.getProperty("name").toString();
                        String inL = inV.getProperty("name").toString();
                        String lookUp = outL + "|" + inL;
                        if (cp.contains(lookUp)) {
                            answer = (String) JOptionPane.showInputDialog(
                                    null,
                                    "Does " + r + " read before " + rPrime
                                    + " when moving from\n" + outL + " to " + inL
                                    + " across " + cp + "? (yes/no): ",
                                    "Stage 4. Edge labeling stage",
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    null,
                                    "yes");
                            HashSet<String> c_m = (HashSet<String>) e.getProperty("c_m");
                            if (answer.equalsIgnoreCase("yes")) {
                                c_m.add("(" + r + "," + rPrime + ")");
                            } else {
                                c_m.add("(" + rPrime + "," + r + ")");
                            }
                            e.setProperty("c_m", c_m);
                        }
                    }
                }
            }
        }

        // Restore W_r to original.
        // Although this isn't mentioned in the algorithm, it's necessary since
        // we are using W_r.toString()
        W_r.clear();
        Collections.addAll(W_r, W_r_array);
        // Stage 5. Vertex weighing stage.
        for (Vertex v : rfidGraph.getVertices()) {
            String val = null;
            switch (v.getProperty("name").toString()) {
                case "MC":
                    val = "r1->1";
                    break;
                case "SMC":
                    val = "r2->0.8,r3->0.2";
                    break;
                case "TTS":
                    val = "r2->0.2,r3->0.8";
                    break;
                case "CH":
                    val = "r4->0.95,r5->0.95";
                    break;
                case "OC":
                    val = "r4->0.95,r5->0.95";
                    break;
                case "CGS":
                    val = "r4->0.05,r5->0.05";
                    break;
                case "GS1":
                    val = "r6->0.1";
                    break;
                case "GS2":
                    val = "r7->0.1";
                    break;
                case "GS3":
                    val = "r8->0.1";
                    break;
                case "BL1":
                    val = "r6->0.9";
                    break;
                case "BL2":
                    val = "r7->0.9";
                    break;
                case "BL3":
                    val = "r8->0.9";
                    break;
                default:
                    val = "no";
                    break;
            }
            String answer = (String) JOptionPane.showInputDialog(
                    null,
                    "What are the coverage weights of readers\n"
                    + W_r.toString() + " in " + v.getProperty("name") + "? (enter no if none): ",
                    "Stage 5. Vertex weighing stage",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    null,
                    val);
            if (answer == null) { // if the user cancels the input, then set it to no
                answer = "no";
            }
            if (!answer.equalsIgnoreCase("no")) {
                StringTokenizer st = new StringTokenizer(answer, ",");
                while (st.hasMoreTokens()) {
                    String w = st.nextToken();
                    HashSet<String> c_r = (HashSet<String>) v.getProperty("c_r");
                    c_r.add(w);
                    v.setProperty("c_r", c_r);
                }
            }
        }
    }

    /** This method adds a customized property to the vertices or edges of the
     * specified RFID readers deployment pseudograph. */
    public static void addCustomizedProperty(OrientGraph rfidGraph, String propertyName,
            String propertyOf, String[] propertyValues) {
        if (propertyOf.equals("Vertices (Semantic Locations)")) {
            for (Vertex v : rfidGraph.getVertices()) {
                String answer = (String) JOptionPane.showInputDialog(
                        null,
                        "What is the value of the property " + propertyName + " in " + v.getProperty("name") + "?\n"
                        + Arrays.toString(propertyValues) + " (enter no if none): ",
                        "Adding a Customized Property",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        "no");
                if (answer == null) { // if the user cancels the input, then set it to no
                    answer = "no";
                }
                v.setProperty(propertyName, answer);
            }
        } else {
            for (Edge e : rfidGraph.getEdges()) {
                String answer = (String) JOptionPane.showInputDialog(
                        null,
                        "What is the value of the property " + propertyName + " in " + e.getLabel() + "?\n"
                        + Arrays.toString(propertyValues) + " (enter no if none): ",
                        "Adding a Customized Property",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        "no");
                if (answer == null) { // if the user cancels the input, then set it to no
                    answer = "no";
                }
                e.setProperty(propertyName, answer);
            }
        }
    }
}