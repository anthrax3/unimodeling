package info.sarihh.unimodeling.midgraph;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import info.sarihh.unimodeling.utility.DrawDelimiter;
import info.sarihh.unimodeling.utility.DrawEdge;
import info.sarihh.unimodeling.utility.DrawElement;
import info.sarihh.unimodeling.utility.DrawVertex;
import info.sarihh.unimodeling.utility.GraphModeling;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represent a graph. It is midway between the diagrams and the
 * graph database. It was created because the Orient Graph implementation
 * offered little flexibility using additional information other than name and
 * endpoints for elements. This midway graph contains coordinates, RFIDs,
 * special values and two-way connection representation. The edges and vertices
 * are stored in lists.
 * There are some unused getter and setter methods because XMLEncoder requires
 * them to build the XML files that we use to save the graph.
 * Author: Sari Haj Hussein
 */
public class MGraph implements Serializable {

    private List<MEdge> edges;
    private List<MVertex> vertices;
    public static final int IN = 1;
    public static final int OUT = 2;

    /**
     * Creates empty lists for edges and vertices
     */
    public MGraph() {
        edges = new ArrayList<>();
        vertices = new ArrayList<>();
    }

    /**
     * Gets the list of edges
     * @return list of edges
     */
    public List<MEdge> getEdges() {
        return edges;
    }

    /**
     * Sets the list of edges
     * @param edges list of edges
     */
    public void setEdges(List<MEdge> edges) {
        this.edges = edges;
    }

    /**
     * Gets the list of vertices
     * @return list of vertices
     */
    public List<MVertex> getVertices() {
        return vertices;
    }

    /**
     * Sets the list of vertices
     * @param vertices 
     */
    public void setVertices(List<MVertex> vertices) {
        this.vertices = vertices;
    }

    /**
     * Adds an edge to the graph. Also adds the edge to the connecting vertices
     * @param edge edge to add
     */
    public void addEdge(MEdge edge) {
        edges.add(edge);
        edge.getVertex(IN).addEdge(edge, IN);
        edge.getVertex(OUT).addEdge(edge, OUT);
    }

    /**
     * Adds a vertex to the graph
     * @param vertex vertex to add
     */
    public void addVertex(MVertex vertex) {
        vertices.add(vertex);
    }

    /**
     * Removes an edge from the graph.
     * @param edge edge to remove
     */
    public void removeEdge(MEdge edge) {
        edges.remove(edge);
    }

    /**
     * Removes a vertex from the graph, and removes its connecting edges.
     * @param vertex vertex to remove
     */
    public void removeVertex(MVertex vertex) {
        for (MEdge edge : vertex.getEdges().keySet()) {
            edges.remove(edge);
        }
        vertices.remove(vertex);
    }

    /**
     * Gets an edge from the graph by ID
     * @param id ID
     * @return the needed edge
     */
    public MEdge getEdge(int id) {
        MEdge edge = null;
        for (MEdge e : edges) {
            if (e.getId() == id) {
                edge = e;
                break;
            }
        }
        return edge;
    }

    /**
     * Gets an edge from the graph by its endpoint vertices
     * @param vIn vertex to
     * @param vOut vertex from
     * @return needed edge
     */
    public MEdge getEdge(MVertex vIn, MVertex vOut) {
        MEdge edge = null;
        for (MEdge e : edges) {
            if (vIn == e.getVertex(IN) && vOut == e.getVertex(OUT)) {
                edge = e;
                break;
            }
        }
        return edge;
    }

    /**
     * Gets a vertex from the graph by ID
     * @param id ID
     * @return needed vertex
     */
    public MVertex getVertex(int id) {
        MVertex vertex = null;
        for (MVertex v : vertices) {
            if (v.getId() == id) {
                vertex = v;
                break;
            }
        }
        return vertex;
    }

    /**
     * Clears the graph. Deletes all edges and vertices.
     */
    public void clearGraph() {
        edges.clear();
        vertices.clear();
    }

    /**
     * Converts this graph to Orient Graph Database and also saves it as
     * graphml file. For precautions the graph DB is opened-cleared-closed
     * before the real writing occurs, because OrientDB tends to work slow and
     * can quickly become inconsistent or file error can occur.
     * @param orientGraphPath path to the Orient Graph DB
     * @param graphmlPath path to the graphml file
     */
    public void convertToGraph(String orientGraphPath, String graphmlPath) {
        OrientGraph orientGraph = new OrientGraph(orientGraphPath);
        GraphModeling.clearGraph(orientGraph);
        orientGraph.shutdown();
        orientGraph = new OrientGraph(orientGraphPath);
        for (MVertex mv : vertices) {
            Vertex v = orientGraph.addVertex(null);
            v.setProperty("name", mv.getName());
            mv.setOgID(v.getId());
        }
        for (MEdge me : edges) {
            Vertex v1 = null;
            Vertex v2 = null;
            for (Vertex v : orientGraph.getVertices("name", me.getVertex(IN).getName())) {
                v1 = v;
            }
            for (Vertex v : orientGraph.getVertices("name", me.getVertex(OUT).getName())) {
                v2 = v;
            }
            Edge e = orientGraph.addEdge(null, v1, v2, "");
            e.setProperty("c", me.getCombinedName());
            me.setOgID(e.getId());
        }
        GraphModeling.saveGraphMLFile(orientGraph, graphmlPath);
        orientGraph.shutdown();
    }

    /**
     * Converts this graph to RFID Orient Graph Database and also saves it as
     * graphml file. For precautions the graph DB is opened-cleared-closed
     * before the real writing occurs, because OrientDB tends to work slow and
     * can quickly become inconsistent or file error can occur.
     * @param orientGraphPath path to the Orient Graph DB
     * @param graphmlPath path to the graphml file
     */
    public void convertToRfidGraph(String orientGraphPath, String graphmlPath) {
        OrientGraph orientGraph = new OrientGraph(orientGraphPath);
        GraphModeling.clearGraph(orientGraph);
        orientGraph.shutdown();
        orientGraph = new OrientGraph(orientGraphPath);
        for (MVertex mv : vertices) {
            Vertex v = orientGraph.addVertex(null);
            v.setProperty("name", mv.getName());
            v.setProperty("c_l", mv.getLabels());
            v.setProperty("c_r", mv.getCoverageWeightSet());
            mv.setOgID(v.getId());
        }
        for (MEdge me : edges) {
            Vertex v1 = null;
            Vertex v2 = null;
            for (Vertex v : orientGraph.getVertices("name", me.getVertex(IN).getName())) {
                v1 = v;
            }
            for (Vertex v : orientGraph.getVertices("name", me.getVertex(OUT).getName())) {
                v2 = v;
            }
            Edge e = orientGraph.addEdge(null, v1, v2, "");
            e.setProperty("c", me.getCombinedRfidName());
            me.setOgID(e.getId());
        }
        GraphModeling.saveGraphMLFile(orientGraph, graphmlPath);
        orientGraph.shutdown();
    }

    /**
     * Creates the vertices and edges on the Graph from midGraph. It creates
     * circles and arrows on the graph layer from mvertices and medges.
     * @return elements on the graph layer
     */
    public List<DrawElement> createDrawGraph() {
        List<DrawElement> graph = new ArrayList<>();
        for (MVertex mv : vertices) {
            DrawVertex vertex = new DrawVertex(DrawElement.getCentralPoint(mv.getGraphCoordinates()));
            vertex.setName(mv.getName());
            vertex.setMID(mv.getId());
            graph.add(vertex);
            DrawDelimiter delim = new DrawDelimiter(mv.getDelimCoordinates());
            delim.setFaint(true);
            graph.add(delim);
        }
        for (MEdge me : edges) {
            DrawEdge edge = new DrawEdge(
                    DrawElement.getCentralPoint(me.getVertex(IN).getGraphCoordinates()),
                    DrawElement.getCentralPoint(me.getVertex(OUT).getGraphCoordinates()));
            for (DrawElement de : graph) {
                if (de.getMID() == me.getVertex(IN).getId()) {
                    edge.addConnection(de);
                    de.addConnection(edge);
                }
                if (de.getMID() == me.getVertex(OUT).getId()) {
                    edge.addConnection(de);
                    de.addConnection(edge);
                }
            }
            edge.setName(me.getCombinedName());
            edge.setMID(me.getId());
            graph.add(edge);
        }
        return graph;
    }

    /**
     * Creates the vertices and edges on the RFID graph from midGraph. It creates
     * circles and arrows on the rfid layer from mvertices and medges.
     * @return elements on the graph layer
     */
    public List<DrawElement> createDrawRfidGraph() {
        List<DrawElement> graph = new ArrayList<>();
        for (MVertex mv : vertices) {
            DrawVertex vertex = new DrawVertex(DrawElement.getCentralPoint(mv.getRfidCoordinates()));
            vertex.setName(mv.getRfidName());
            vertex.setMID(mv.getId());
            vertex.addProperties(mv.getRfidStats());
            graph.add(vertex);
            DrawDelimiter delim = new DrawDelimiter(mv.getDelimCoordinates());
            delim.setFaint(true);
            graph.add(delim);
        }
        for (MEdge me : edges) {
            DrawEdge edge = new DrawEdge(
                    DrawElement.getCentralPoint(me.getVertex(IN).getRfidCoordinates()),
                    DrawElement.getCentralPoint(me.getVertex(OUT).getRfidCoordinates()));
            for (DrawElement de : graph) {
                if (de.getMID() == me.getVertex(IN).getId()) {
                    edge.addConnection(de);
                    de.addConnection(edge);
                }
                if (de.getMID() == me.getVertex(OUT).getId()) {
                    edge.addConnection(de);
                    de.addConnection(edge);
                }
            }
            edge.setName(me.getCombinedRfidName());
            edge.setMID(me.getId());
            graph.add(edge);
        }
        return graph;
    }
}
