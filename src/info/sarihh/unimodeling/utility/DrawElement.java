package info.sarihh.unimodeling.utility;

import info.sarihh.unimodeling.gui.DrawLayer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Drawable elements. This class represents all the drawable elements that are
 * used in the editor/designer. It has abstract methods defining collision
 * checking, getting size and other information. For collision detection I used
 * the double dispatch technique.
 * @see http://en.wikipedia.org/wiki/Double_dispatch
 * Author: Sari Haj Hussein
 */
public abstract class DrawElement implements Comparable<DrawElement> {

    /**
     * Top side of the base rectangle.
     */
    protected static final int TOP = 0;
    /**
     * Bottom side of the base rectangle.
     */
    protected static final int BOTTOM = 1;
    /**
     * Left side of the base rectangle.
     */
    protected static final int LEFT = 2;
    /**
     * Right side of the base rectangle.
     */
    protected static final int RIGHT = 3;
    protected static final int NONE = -1;
    protected static final int NEAST = 0;
    protected static final int NORTH = 1;
    protected static final int NWEST = 2;
    protected static final int WEST = 3;
    protected static final int SWEST = 4;
    protected static final int SOUTH = 5;
    protected static final int SEAST = 6;
    protected static final int EAST = 7;
    /**
     * Start corner of the base rectangle (usually top-left but not necessarily)
     */
    protected Point p1;
    /**
     * End corner of the base rectangle (usually bottom-right but not necessarily)
     */
    protected Point p2;
    /**
     * Selection indicator
     */
    protected boolean selected;
    /**
     * Indicates error with the start point
     */
    protected boolean startError;
    /**
     * Indicates error with the end point
     */
    protected boolean endError;
    /**
     * Visibility
     */
    protected boolean visible;
    /**
     * Object ID @todo: if not used delete it
     */
    protected int ID;
    /**
     * Object name
     */
    protected String name;
    /**
     * Displayable properties
     */
    protected String properties = "";
    /**
     * Object type
     * @see DrawLayer
     */
    protected int type;
    /**
     * Starting point before moving
     */
    protected Point startMovePoint;
    /**
     * Blueprints element ID
     */
    protected Object bpID = null;
    /**
     * Connecting elements list
     */
    protected List<DrawElement> connections;
    /**
     * Connection matrix representing directions;
     */
    protected Boolean[][] connectionMatrix;
    /**
     * OrientGraph ids representing connections;
     */
    protected Object[][] connectedIDs;
    /**
     * MGraph ids
     */
    protected int[][] connectedmIDs;
    /**
     * What part is resized.
     */
    protected int resizePart;
    /**
     * midGraph id
     */
    protected int mID;
    /**
     * If element is dimmed (0.25 alpha)
     */
    protected boolean dimmed;

    /**
     * Default constructor
     */
    public DrawElement() {
        selected = false;
        startError = false;
        endError = false;
        name = "";
        type = DrawLayer.NONE;
        connections = new ArrayList<>();
        connectionMatrix = null;
        connectedIDs = null;
        resizePart = NONE;
        visible = true;
    }

    /**
     * Constructs an element with coordinates specified as string.
     * @param s coordinates
     */
    public DrawElement(String s) {
        p1 = new Point();
        p2 = new Point();
        setCoordinates(s);
        startMovePoint = p1;
        selected = false;
        startError = false;
        endError = false;
        name = "";
        type = DrawLayer.NONE;
        connections = new ArrayList<>();
        connectionMatrix = null;
        connectedIDs = null;
        resizePart = NONE;
        visible = true;
    }

    /**
     * Constuctor. Creates the base rectangle with start and endpoint.
     * @param p1 Start point
     * @param p2 End point
     */
    public DrawElement(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
        startMovePoint = p1;
        selected = false;
        startError = false;
        endError = false;
        name = "";
        type = DrawLayer.NONE;
        connections = new ArrayList<>();
        connectionMatrix = null;
        connectedIDs = null;
        resizePart = NONE;
        visible = true;
    }

    /**
     * Abstract method for point detection. Detects if the specified point is
     * in the object.
     * @param pointer The point to be detected.
     * @return Whether the point is in or not.
     */
    public abstract boolean hasPoint(Point pointer);

    /**
     * Abstract method for drawing object.
     * @param g Graphics2D object used for drawing
     */
    public abstract void draw(Graphics2D g);

    /**
     * Base collision detection. Redirects to child specific collision model.
     * @param elem The colliding DrawElement object.
     * @return Whether given objects collide.
     */
    public boolean isColliding(DrawElement elem) {
        return elem.isCollidingWith(this);
    }

    /**
     * Helper method for collision detection. This method never gets called.
     * @param elem The colliding DrawElement object.
     * @return Always false.
     */
    public boolean isCollidingWith(DrawElement elem) {
        return false;
    }

    /**
     * Abstract DrawDelimiter collision detection
     * @param elem The colliding DrawDelimiter object.
     * @return Whether given objects collide.
     */
    public abstract boolean isCollidingWith(DrawDelimiter elem);

    /**
     * Abstract DrawEdge collision detection
     * @param elem The colliding DrawEdge object.
     * @return Whether given objects collide.
     */
    public abstract boolean isCollidingWith(DrawEdge edge);

    /**
     * Abstract DrawRfid collision detection
     * @param elem The colliding DrawRfid object.
     * @return Whether given objects collide.
     */
    public abstract boolean isCollidingWith(DrawRfid rfid);

    /**
     * Abstract DrawVertex collision detection
     * @param elem The colliding DrawVertex object.
     * @return Whether given objects collide.
     */
    public abstract boolean isCollidingWith(DrawVertex vertex);

    /**
     * Abstract DrawConnection collision detection
     * @param conn The colliding DrawConnection object.
     * @return Whether given objects collide.
     */
    public abstract boolean isCollidingWith(DrawConnection conn);

    /**
     * Abstract DrawDirection collision detection
     * @param conn The colliding DrawConnection object.
     * @return Whether given objects collide.
     */
    public abstract boolean isCollidingWith(DrawDirection dir);

    /**
     * Get the first point of this element.
     * @return the first point
     */
    public Point getPoint1() {
        return p1;
    }

    /**
     * Get the second point of this element.
     * @return the second point
     */
    public Point getPoint2() {
        return p2;
    }

    /**
     * Set the first point of this element.
     * @param p First point
     */
    public void setPoint1(Point p) {
        p1 = p;
    }

    /**
     * Set the second point of this element.
     * @param p Second point
     */
    public void setPoint2(Point p) {
        p2 = p;
    }

    /**
     * Resize this element by the given ratios.
     * @param ratioX horizontal ratio
     * @param ratioY vertical ratio
     */
    public void resize(float ratioX, float ratioY) {
        p1.x = Math.round(p1.x * ratioX);
        p1.y = Math.round(p1.y * ratioY);
        p2.x = Math.round(p2.x * ratioX);
        p2.y = Math.round(p2.y * ratioY);
    }

    /**
     * Returns whether this element is selected.
     * @return Selection state
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Sets selection state on this element.
     * @param s New selection state
     */
    public void setSelected(boolean s) {
        selected = s;
    }

    /**
     * Gets start point error state
     * @return start point error state
     */
    public boolean getStartError() {
        return startError;
    }

    /**
     * Sets start point error state
     * @param e start point error state
     */
    public void setStartError(boolean e) {
        startError = e;
    }

    /**
     * Gets end point error state
     * @return end point error state
     */
    public boolean getEndError() {
        return endError;
    }

    /**
     * Sets end point error state
     * @param e end point error state
     */
    public void setEndError(boolean e) {
        endError = e;
    }

    /**
     * Gets the four coordinates of the base rectangle. Use the defined sides
     * for getting the coordinates out of the returned array.
     * @return array of coordinates in top-bottom-left-right order
     */
    public int[] getSides() {
        int sides[] = new int[4];
        sides[TOP] = (p1.y < p2.y) ? p1.y : p2.y; //top
        sides[BOTTOM] = (p1.y > p2.y) ? p1.y : p2.y; //bottom
        sides[LEFT] = (p1.x < p2.x) ? p1.x : p2.x; //left
        sides[RIGHT] = (p1.x > p2.x) ? p1.x : p2.x; //rigth
        return sides;
    }

    /**
     * Sets the base rectangle's coordinates from array. Use the defined sides
     * for setting the coordinates of the array.
     * @param sides array of coordinates in top-bottom-left-right order
     */
    public void setSides(int sides[]) {
        p1.x = (p1.x < p2.x) ? sides[LEFT] : sides[RIGHT];
        p1.y = (p1.y < p2.y) ? sides[TOP] : sides[BOTTOM];
        p2.x = (p2.x < p1.x) ? sides[LEFT] : sides[RIGHT];
        p2.y = (p2.y < p1.y) ? sides[TOP] : sides[BOTTOM];
    }

    /**
     * Returns the central point of the base rectangle.
     * @return central point
     */
    public Point getCentralPoint() {
        int sides[] = getSides();
        return new Point((sides[3] + sides[2]) / 2, (sides[1] + sides[0]) / 2);
    }

    /**
     * Returns the central point of a rectangle whose coordinates are given as 
     * string. Static method
     * @param coords coordinates as string: TOP_BOTTOM_LEFT_RIGHT
     * @return central point
     */
    public static Point getCentralPoint(String coords) {
        String c[] = coords.split("_");
        int x1 = Integer.parseInt(c[0]);
        int y1 = Integer.parseInt(c[1]);
        int x2 = Integer.parseInt(c[2]);
        int y2 = Integer.parseInt(c[3]);
        return new Point((x1 + x2) / 2, (y1 + y2) / 2);
    }

    /**
     * Checks whether the element size is zero
     * @return zero size
     */
    public boolean isNullSize() {
        return (p1.x == p2.x && p1.y == p2.y);
    }

    /**
     * Sets the element's name
     * @param n new name
     */
    public void setName(String n) {
        if (n != null) {
            this.name = n;
        }
    }

    /**
     * Gets the element's name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the element's subtype
     * @return subtype
     */
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * Sets the start point from where the movement of the element should start.
     * @param p movement start point
     */
    public void setStartMovePoint(Point p) {
        startMovePoint = p;
    }

    /**
     * Move the object from the movement start point then sets the start point 
     * to the new point
     * @param p 'move to' point
     */
    public void move(Point p) {
        p1.x += p.x - startMovePoint.x;
        p1.y += p.y - startMovePoint.y;
        p2.x += p.x - startMovePoint.x;
        p2.y += p.y - startMovePoint.y;
        startMovePoint = p;
    }

    /**
     * Gets the base rectangle's coordinates as a String
     * @return TOP_BOTTOM_LEFT_RIGHT
     */
    public String getCoordinates() {
        return p1.x + "_" + p1.y + "_" + p2.x + "_" + p2.y;
    }

    /**
     * Sets the base rectangle's coordinates from a String
     * @param coords TOP_BOTTOM_LEFT_RIGHT
     */
    public void setCoordinates(String coords) {
        String c[] = coords.split("_");
        p1.x = Integer.parseInt(c[0]);
        p1.y = Integer.parseInt(c[1]);
        p2.x = Integer.parseInt(c[2]);
        p2.y = Integer.parseInt(c[3]);
    }

    /**
     * Gets the element's OrientGraph ID
     * @return OrientGraph ID
     */
    public Object getBpID() {
        return bpID;
    }

    /**
     * Sets the element's OrientGraph ID
     * @param id OrientGraph ID
     */
    public void setBpID(Object id) {
        bpID = id;
    }

    /**
     * Adds an element to the element's connection list
     * @param element new connecting element
     */
    public void addConnection(DrawElement element) {
        if (element != null) {
            connections.add(element);
        }
    }

    /**
     * Adds an element to the element's connection list in the specified
     * location.
     * @param index index
     * @param element new connecting element
     */
    public void addConnection(int index, DrawElement element) {
        if (element != null) {
            connections.add(index, element);
        }
    }

    /**
     * Adds a list of connecting elements to this element's connection list
     * @param elements new connecting elements
     */
    public void addConnections(List<DrawElement> elements) {
        if (!elements.isEmpty()) {
            connections.addAll(elements);
        }
    }

    /**
     * Gets the connection list
     * @return connecting elements' list
     */
    public List<DrawElement> getConnections() {
        return connections;
    }

    public void setConnections(List<DrawElement> connections) {
        this.connections = connections;
    }

    /**
     * Sets the element's connection matrix.
     * @param matrix connection matrix
     */
    public void setConnectionMatrix(Boolean[][] matrix) {
        connectionMatrix = matrix;
        connectedIDs = new Object[matrix.length][matrix.length];
        connectedmIDs = new int[matrix.length][matrix.length];
    }

    /**
     * Gets the element's connection matrix
     * @return connection matrix
     */
    public Boolean[][] getConnectionMatrix() {
        return connectionMatrix;
    }

    /**
     * Sets the ID of a specified connection. Uses the OrientGraph ID of the
     * corresponding Edge
     * @param i line in connection matrix
     * @param j column in connection matrix
     * @param id new OrientGraph ID
     */
    public void setConnectionBpID(int i, int j, Object id) {
        connectedIDs[i][j] = id;
    }

    /**
     * Gets the ID of a specified connection. Uses the OrientGraph ID of the
     * corresponding Edge
     * @param i line in connection matrix
     * @param j column in connection matrix
     * @return OrientGraph ID
     */
    public Object getConnectionBpID(int i, int j) {
        return connectedIDs[i][j];
    }

    /**
     * Returns an array of Shapes to decide which part of the object is resized.
     * @return array of resize areas
     */
    protected Shape[] getResizeAreas() {
        Shape[] result = new Shape[8];
        int[] sides = this.getSides();
        result[NEAST] = new Rectangle(sides[LEFT], sides[TOP], 5, 5);
        result[NORTH] = new Rectangle(sides[LEFT] + 6, sides[TOP],
                sides[RIGHT] - sides[LEFT] - 12, 5);
        result[NWEST] = new Rectangle(sides[RIGHT] - 5, sides[TOP], 5, 5);
        result[WEST] = new Rectangle(sides[RIGHT] - 5, sides[TOP] + 6, 5,
                sides[BOTTOM] - sides[TOP] - 12);
        result[SWEST] = new Rectangle(sides[RIGHT] - 5, sides[BOTTOM] - 5, 5, 5);
        result[SOUTH] = new Rectangle(sides[LEFT] + 6, sides[BOTTOM] - 5,
                sides[RIGHT] - sides[LEFT] - 12, 5);
        result[SEAST] = new Rectangle(sides[LEFT], sides[BOTTOM] - 5, 5, 5);
        result[EAST] = new Rectangle(sides[LEFT], sides[TOP] + 6, 5,
                sides[BOTTOM] - sides[TOP] - 12);
        return result;
    }

    /**
     * Gets the corresponding resize cursor depending on the mouse location. Be
     * aware that the corner cursors' names represent orientation not location, 
     * but we actually name them by location. This means that for example the
     * cursor in the NW corner is called NE_RESIZE_CURSOR.
     * @param p mouse pointer location
     * @return resize cursor
     */
    public Cursor getCursor(Point p) {
        int resizeCursor[] = {Cursor.NW_RESIZE_CURSOR, Cursor.N_RESIZE_CURSOR,
            Cursor.NE_RESIZE_CURSOR, Cursor.W_RESIZE_CURSOR,
            Cursor.SE_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR,
            Cursor.SW_RESIZE_CURSOR, Cursor.E_RESIZE_CURSOR};
        Shape rs[] = getResizeAreas();
        for (int i = 0; i < 8; ++i) {
            if (rs[i].contains(p)) {
                return new Cursor(resizeCursor[i]);
            }
        }
        return new Cursor(Cursor.DEFAULT_CURSOR);
    }

    /**
     * Specifies which part of the element should be resized based on mouse 
     * location
     * @param p mouse pointer location
     */
    public void resizePart(Point p) {
        Shape rs[] = getResizeAreas();
        for (int i = 0; i < 8; ++i) {
            if (rs[i].contains(p)) {
                resizePart = i;
                break;
            }
        }
    }

    /**
     * Resize the specified part.
     * @param p mouse pointer location
     */
    public void resize(Point p) {
        int sides[] = getSides();
        switch (resizePart) {
            case NEAST:
                sides[LEFT] = p.x;
                sides[TOP] = p.y;
                break;
            case NORTH:
                sides[TOP] = p.y;
                break;
            case NWEST:
                sides[RIGHT] = p.x;
                sides[TOP] = p.y;
                break;
            case WEST:
                sides[RIGHT] = p.x;
                break;
            case SWEST:
                sides[RIGHT] = p.x;
                sides[BOTTOM] = p.y;
                break;
            case SOUTH:
                sides[BOTTOM] = p.y;
                break;
            case SEAST:
                sides[LEFT] = p.x;
                sides[BOTTOM] = p.y;
                break;
            case EAST:
                sides[LEFT] = p.x;
                break;
        }
        setSides(sides);
    }

    /**
     * Simple colliding boxes. Should have used built-in contains or intersects
     * methods...
     * @param elem colliding element
     * @return whether the elements are colliding
     */
    protected boolean boxCollide(DrawElement elem) {
        int sides1[] = this.getSides();
        int sides2[] = elem.getSides();
        return !((sides1[BOTTOM] < sides2[TOP]) || (sides1[TOP] > sides2[BOTTOM])
                || (sides1[LEFT] > sides2[RIGHT]) || (sides1[RIGHT] < sides2[LEFT]));
    }

    /**
     * Sets the default coloring for this element. If it is selected it is 
     * green, if error occured it is red, otherwise it is black.
     * @param g Graphics2D object
     */
    protected void defaultColor(Graphics2D g) {
        int alpha = (dimmed) ? 63 : 255;
        if (selected) {
            g.setStroke(new BasicStroke(4));
            g.setColor(new Color(0, 255, 0, alpha));
        } else if (startError || endError) {
            g.setStroke(new BasicStroke(3));
            g.setColor(new Color(255, 0, 0, alpha));
        } else {
            g.setStroke(new BasicStroke(2));
            g.setColor(new Color(0, 0, 0, alpha));
            g.setPaint(new Color(0, 0, 0, alpha));
        }
    }

    /**
     * Sets visibility parameter
     * @param v visibility
     */
    public void setVisible(boolean v) {
        visible = v;
    }

    /**
     * Gets visibility parameter
     * @return visibility
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Gets the base rectangle
     * @return base rectangle
     */
    protected Rectangle2D getBlock() {
        int sides[] = getSides();
        return new Rectangle2D.Double(sides[LEFT], sides[TOP],
                sides[RIGHT] - sides[LEFT], sides[BOTTOM] - sides[TOP]);
    }

    /**
     * Compare this element to another by its type. The order of the types are
     * necessary when deciding which element is above the other an the order is:
     * DELIMITER -> VERTEX -> EDGE -> CONNECTION -> RFID
     * @param e the other element
     * @return comparison
     */
    @Override
    public int compareTo(DrawElement e) {
        return this.type - e.type;
    }

    /**
     * Returns the string representation of this element. It is only for debug
     * purposes. It writes the type, the name and the points of this element.
     * @return element string
     */
    @Override
    public String toString() {
        String types[] = {"DELIMITER", "VERTEX", "EDGE", "CONNECTION", "RFID",
            "MOVE", "RESIZE", "DIRECTION"};
        return types[getType()] + ": " + getName() + " id:" + bpID
                + " [" + p1.x + "," + p1.y + "] [" + p2.x + "," + p2.y + "]";
    }

    /**
     * Gets this elements ID
     * @return ID
     */
    public int getID() {
        return ID;
    }

    /**
     * Sets this element's ID
     * @param ID the ID
     */
    public void setID(int ID) {
        this.ID = ID;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public void addProperties(String properties) {
        this.properties += properties;
    }

    public int getMID() {
        return mID;
    }

    public void setMID(int mID) {
        this.mID = mID;
    }

    public int getConnectionmID(int i, int j) {
        return connectedmIDs[i][j];
    }

    public void setConnectionmID(int i, int j, int id) {
        connectedmIDs[i][j] = id;
    }

    public boolean isDimmed() {
        return dimmed;
    }

    public void setDimmed(boolean dimmed) {
        this.dimmed = dimmed;
    }

    public Object[][] getConnectedIDs() {
        return connectedIDs;
    }

    public void setConnectedIDs(Object[][] connectedIDs) {
        this.connectedIDs = connectedIDs;
    }

    public int[][] getConnectedMIDs() {
        return connectedmIDs;
    }

    public void setConnectedMIDs(int[][] connectedmIDs) {
        this.connectedmIDs = connectedmIDs;
    }
}
