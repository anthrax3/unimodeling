package info.sarihh.unimodeling.utility;

import info.sarihh.unimodeling.gui.DrawLayer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;

/*
 * Author: Sari Haj Hussein
 */
public class DrawRfid extends DrawElement {

    public static int counter = 1;
    private double ratio = 0;

    public DrawRfid() {
        super();
        type = DrawLayer.RFID;
    }

    public DrawRfid(Point p1, Point p2) {
        super(p1, p2);
        type = DrawLayer.RFID;
    }

    @Override
    public boolean hasPoint(Point pointer) {
        return this.getBlock().contains(pointer);
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
        return edge.edge.intersects(getBlock());
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
        g.setColor(Color.white);
        g.setStroke(new BasicStroke(2));
        g.fill(new Ellipse2D.Float(p1.x - 2, p1.y - 2, p2.x - p1.x + 4, p2.y - p1.y + 4));
        defaultColor(g);
        g.draw(new Ellipse2D.Float(p1.x + 1, p1.y + 1, p2.x - p1.x, p2.y - p1.y));
        g.setColor(new Color(160, 0, 0, (dimmed) ? 63 : 255));
        g.draw(new Ellipse2D.Float(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y));
        g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, 14));
        g.drawString(name, p1.x + 10, p1.y + 20);
    }

    @Override
    public boolean isCollidingWith(DrawConnection conn) {
        return boxCollide(conn);
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    @Override
    public boolean isCollidingWith(DrawDirection dir) {
        return false;
    }
}
