package info.sarihh.unimodeling.utility;

import info.sarihh.unimodeling.gui.DrawLayer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Double;

/*
 * Author: Sari Haj Hussein
 */
public class DrawVertex extends DrawElement {

    private Ellipse2D.Double circle;
    private String rfid = "";

    public DrawVertex() {
        super();
        type = DrawLayer.VERTEX;
        circle = new Ellipse2D.Double(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
    }

    public DrawVertex(Point c) {
        super(new Point(c.x - 15, c.y - 15), new Point(c.x + 15, c.y + 15));
        type = DrawLayer.VERTEX;
        circle = new Ellipse2D.Double(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
    }

    public DrawVertex(Point p1, Point p2) {
        super(p1, p2);
        type = DrawLayer.VERTEX;
        circle = new Ellipse2D.Double(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
    }

    @Override
    public boolean hasPoint(Point pointer) {
        return circle.contains(pointer);
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
        return false;
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
        g.draw(new Ellipse2D.Double(p1.x + 1, p1.y + 1, p2.x - p1.x, p2.y - p1.y));
        g.setColor(new Color(0, 127, 0, (dimmed) ? 63 : 255));
        circle.setFrame(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y);
        g.draw(circle);
        g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, 14));
        g.drawString(name + rfid, p1.x + 10, p1.y + 20);
        g.setColor(new Color(160, 0, 0, (dimmed) ? 63 : 255));
        //hack
        if (properties != null) {
            int i = 1;
            for (String s : properties.split("_")) {
                g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, 14));
                g.drawString(s, p1.x, p2.y + 2 + 13 * (i++));
            }
        }
    }

    @Override
    public boolean isCollidingWith(DrawConnection conn) {
        return false;
    }

    @Override
    public void move(Point p) {
        Point central = getCentralPoint();
        p1.x += p.x - startMovePoint.x;
        p1.y += p.y - startMovePoint.y;
        p2.x += p.x - startMovePoint.x;
        p2.y += p.y - startMovePoint.y;
        for (DrawElement e : connections) {
            if (e.getPoint1().equals(central)) {
                e.setPoint1(getCentralPoint());
            } else if (e.getPoint2().equals(central)) {
                e.setPoint2(getCentralPoint());
            }
        }
        startMovePoint = p;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    @Override
    public boolean isCollidingWith(DrawDirection dir) {
        return false;
    }

    public Double getCircle() {
        return circle;
    }

    public void setCircle(Double circle) {
        this.circle = circle;
    }
}
