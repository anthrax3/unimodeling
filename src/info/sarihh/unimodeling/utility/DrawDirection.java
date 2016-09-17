package info.sarihh.unimodeling.utility;

import info.sarihh.unimodeling.gui.DrawLayer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/*
 * Author: Sari Haj Hussein
 */
public class DrawDirection extends DrawElement {

    private DrawConnection connection;

    public DrawDirection() {
        super();
        type = DrawLayer.DIRECTION;
    }

    public DrawDirection(Point p1, Point p2) {
        super(p1, p2);
        type = DrawLayer.DIRECTION;
    }

    @Override
    public boolean hasPoint(Point pointer) {
        return false;
    }

    @Override
    public void draw(Graphics2D g) {
        if (!visible) {
            return;
        }
        defaultColor(g);
        g.setColor(new Color(0, 0, 160, (dimmed) ? 63 : 255));
        double a = p2.x - p1.x;
        double b = p2.y - p1.y;
        double alfa = Math.atan2(b, a);
        double sx = Math.cos(alfa);
        double sy = Math.sin(alfa);
        Point2D.Double P1 = new Point2D.Double(p1.x + 15 * sx, p1.y + 15 * sy);
        Point2D.Double P2 = new Point2D.Double(p2.x - 15 * sx, p2.y - 15 * sy);
        double x1 = Math.cos(Math.PI / 12 + alfa);
        double x2 = Math.cos(-Math.PI / 12 + alfa);
        double y1 = Math.sin(Math.PI / 12 + alfa);
        double y2 = Math.sin(-Math.PI / 12 + alfa);
        g.draw(new Line2D.Float(P1, P2));
        g.draw(new Line2D.Double(P2.getX(), P2.getY(), P2.getX() - 20 * x1, P2.getY() - 20 * y1));
        g.draw(new Line2D.Double(P2.getX(), P2.getY(), P2.getX() - 20 * x2, P2.getY() - 20 * y2));
    }

    @Override
    public boolean isCollidingWith(DrawDelimiter elem) {
        return boxCollide(elem);
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
    public boolean isCollidingWith(DrawConnection conn) {
        return false;
    }

    public DrawConnection getConnection() {
        return connection;
    }

    public void setConnection(DrawConnection connection) {
        this.connection = connection;
    }

    @Override
    public boolean isCollidingWith(DrawDirection dir) {
        return false;
    }
}
