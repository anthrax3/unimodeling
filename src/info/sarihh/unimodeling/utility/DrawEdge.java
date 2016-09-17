package info.sarihh.unimodeling.utility;

import info.sarihh.unimodeling.gui.DrawLayer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;

/*
 * Author: Sari Haj Hussein
 */
public class DrawEdge extends DrawElement {

    private DrawElement startPoint;
    private DrawElement endPoint;
    private DrawConnection connection;
    protected Shape edge;
    private int multiplicity;
    private int num = 0;

    public DrawEdge() {
        super();
        type = DrawLayer.EDGE;
        edge = null;
        connection = null;
    }

    public DrawEdge(Point p1, Point p2) {
        super(p1, p2);
        type = DrawLayer.EDGE;
        edge = null;
        connection = null;
    }

    @Override
    public boolean hasPoint(Point pointer) {
        int sides[] = this.getSides();
        boolean ret = (pointer.x >= sides[LEFT] && pointer.x <= sides[RIGHT]
                && pointer.y >= sides[TOP] && pointer.y <= sides[BOTTOM]);
        float m = (float) (p2.y - p1.y) / (p2.x - p1.x);
        float b = p1.y - m * p1.x;
        float p = pointer.y - m * pointer.x;
        ret &= (b <= p + 5 && b >= p - 5);
        return ret;
    }

    @Override
    public boolean isColliding(DrawElement elem) {
        return elem.isCollidingWith(this);
    }

    @Override
    public boolean isCollidingWith(DrawDelimiter elem) {
        return false;
    }

    @Override
    public boolean isCollidingWith(DrawEdge edge) {
        return false;
    }

    @Override
    public boolean isCollidingWith(DrawRfid rfid) {
        return edge.intersects(rfid.getBlock());
    }

    @Override
    public boolean isCollidingWith(DrawVertex vertex) {
        return false;
    }

    @Override
    public void draw(Graphics2D g) {
        if (!visible) {
            return;
        }
        defaultColor(g);
        g.setColor(new Color(0, 0, 160, (dimmed) ? 63 : 255));
        if (p1.equals(p2)) {
            Point central = getCentralPoint();
            edge = new Arc2D.Double(central.x, central.y - 30, 30, 30, 270, 270, Arc2D.OPEN);
            g.draw(edge);
            g.draw(new Line2D.Double(central.x, central.y - 15, central.x - 5, central.y - 25));
            g.draw(new Line2D.Double(central.x, central.y - 15, central.x + 10, central.y - 20));
            g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, 14));
            if (name.contains("|")) {
                String vertexName = this.getConnections().get(0).getName();
                g.drawString("{" + vertexName + "|" + vertexName + "}", central.x + 30, central.y - 30);
            } else {
                g.drawString(name, central.x + 30, central.y - 30);
            }
        } else {
            double a = p2.x - p1.x;
            double b = p2.y - p1.y;
            double alfa = Math.atan2(b, a);
            double sx = Math.cos(alfa);
            double sy = Math.sin(alfa);
            Point2D.Double P1 = new Point2D.Double(p1.x + 15 * sx, p1.y + 15 * sy);
            Point2D.Double P2 = new Point2D.Double(p2.x - 15 * sx, p2.y - 15 * sy);
            Point2D.Double PC = new Point2D.Double((P1.getX() + P2.getX()) / 2,
                    (P1.getY() + P2.getY()) / 2);
            Point2D.Double PX1 = new Point2D.Double(PC.x - sy * (20 + num * 20), PC.y + sx * (20 + num * 20));
            Point2D.Double PX2 = new Point2D.Double(PC.x + sy * 20, PC.y - sx * 20);
            double beta = Math.atan2(P2.y - PX1.y, P2.x - PX1.x);
            double x1 = Math.cos(Math.PI / 12 + beta);
            double x2 = Math.cos(-Math.PI / 12 + beta);
            double y1 = Math.sin(Math.PI / 12 + beta);
            double y2 = Math.sin(-Math.PI / 12 + beta);
            edge = new QuadCurve2D.Double(P1.getX(), P1.getY(), PX1.getX(), PX1.getY(), P2.getX(), P2.getY());
            g.draw(edge);
            g.draw(new Line2D.Double(P2.getX(), P2.getY(), P2.getX() - 20 * x1, P2.getY() - 20 * y1));
            g.draw(new Line2D.Double(P2.getX(), P2.getY(), P2.getX() - 20 * x2, P2.getY() - 20 * y2));
            g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, 14));
            g.drawString(name, (int) PX1.x, (int) PX1.y);
            if (properties != null) {
                int i = 1;
                String lines[] = properties.split("_");
                for (String s : lines) {
                    g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, 14));
                    g.drawString(s, (int) PX1.x, (int) PX1.y + 13 * (i++));
                }
            }
        }
    }

    public void setStartPoint(DrawElement de) {
        startPoint = de;
        autoNaming();
    }

    public void setEndPoint(DrawElement de) {
        endPoint = de;
        autoNaming();
    }

    private void autoNaming() {
        if (startPoint != null) {
            if (endPoint != null) {
                name = "{" + startPoint.getName() + "|" + endPoint.getName() + "}";
            } else {
                name = "{" + startPoint.getName() + "|}";
            }
        } else if (endPoint != null) {
            name = "{|" + endPoint.getName() + "}";
        }
    }

    @Override
    public boolean isCollidingWith(DrawConnection conn) {
        return false;
    }

    public DrawConnection getConnection() {
        return connection;
    }

    public void setConnection(DrawConnection connection) {
        this.connection = connection;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public boolean isCollidingWith(DrawDirection dir) {
        return false;
    }

    public Shape getEdge() {
        return edge;
    }

    public void setEdge(Shape edge) {
        this.edge = edge;
    }

    public int getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(int multiplicity) {
        this.multiplicity = multiplicity;
    }

    public DrawElement getStartPoint() {
        return startPoint;
    }

    public DrawElement getEndPoint() {
        return endPoint;
    }
}
