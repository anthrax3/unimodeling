package info.sarihh.unimodeling.utility;

import info.sarihh.unimodeling.gui.DrawLayer;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/*
 * Author: Sari Haj Hussein
 */
public class DrawConnection extends DrawElement {

    private int num = 0;
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static int counter = 1;
    private int orientation = HORIZONTAL;
    private List<DrawRfid> rfids;
    private boolean writeName = false;

    public DrawConnection() {
        super();
        type = DrawLayer.CONNECTION;
        rfids = new ArrayList<>();
    }

    public DrawConnection(Point p1, Point p2) {
        super(p1, p2);
        type = DrawLayer.CONNECTION;
        rfids = new ArrayList<>();
    }

    @Override
    public boolean hasPoint(Point pointer) {
        int sides[] = this.getSides();
        return (pointer.x >= sides[LEFT] && pointer.x <= sides[RIGHT]
                && pointer.y >= sides[TOP] && pointer.y <= sides[BOTTOM]);
    }

    @Override
    public void draw(Graphics2D g) {
        if (!visible) {
            return;
        }
        defaultColor(g);
        int sides[] = this.getSides();
        for (int i = 0; i < getConnections().size(); ++i) {
            for (int j = 0; j < getConnections().size(); ++j) {
            }
        }

        double a = p2.x - p1.x;
        double b = p2.y - p1.y;
        double alfa = Math.atan2(b, a);
        double sx = Math.cos(alfa);
        double sy = Math.sin(alfa);
        Point2D.Double P1 = new Point2D.Double(p1.x/*+15*sx*/, p1.y/*+15*sy*/);
        Point2D.Double P2 = new Point2D.Double(p2.x/*-15*sx*/, p2.y/*-15*sy*/);
        double x1 = Math.cos(Math.PI / 12 + alfa);
        double x2 = Math.cos(-Math.PI / 12 + alfa);
        double y1 = Math.sin(Math.PI / 12 + alfa);
        double y2 = Math.sin(-Math.PI / 12 + alfa);
        g.draw(new Line2D.Float(P1, P2));
        g.draw(new Line2D.Double(P2.getX(), P2.getY(), P2.getX() - 20 * x1, P2.getY() - 20 * y1));
        g.draw(new Line2D.Double(P2.getX(), P2.getY(), P2.getX() - 20 * x2, P2.getY() - 20 * y2));
        String nameToWrite = "";
        if (writeName) {
            nameToWrite += num;
        }
        g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, 14));
        g.drawString(name + nameToWrite, getCentralPoint().x + 5, getCentralPoint().y + 5);
    }

    @Override
    public boolean isColliding(DrawElement elem) {
        return elem.isCollidingWith(this);
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
        return boxCollide(rfid);
    }

    @Override
    public boolean isCollidingWith(DrawVertex vertex) {
        return false;
    }

    @Override
    public boolean isCollidingWith(DrawConnection conn) {
        return boxCollide(conn);
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getOrientation() {
        int sides[] = getSides();
        int a = sides[RIGHT] - sides[LEFT];
        int b = sides[BOTTOM] - sides[TOP];
        if (a <= b) {
            orientation = VERTICAL;
        } else {
            orientation = HORIZONTAL;
        }
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public List<DrawRfid> getRfids() {
        return rfids;
    }

    public void setRfids(List<DrawRfid> rfids) {
        this.rfids = rfids;
    }

    public String sortRfids() {
        for (int i = 0; i < rfids.size() - 1; ++i) {
            for (int j = i; j < rfids.size(); ++j) {
                if (rfids.get(i).getRatio() <= rfids.get(j).getRatio()) {
                    DrawRfid temp = rfids.get(i);
                    rfids.set(i, rfids.get(j));
                    rfids.set(j, temp);
                }
            }
        }
        String sorted = "";
        for (DrawRfid dr : rfids) {
            sorted += dr.getRatio() + "_";
        }
        sorted = sorted.substring(0, sorted.length() - 1);
        return sorted;
    }

    public List<DrawRfid> reverseRfids() {
        int s = rfids.size();
        List<DrawRfid> newList = new ArrayList<>();
        for (DrawRfid dr : rfids) {
            newList.add(0, dr);
        }
        return newList;
    }

    public boolean isWriteName() {
        return writeName;
    }

    public void setWriteName(boolean writeName) {
        this.writeName = writeName;
    }

    @Override
    public boolean isCollidingWith(DrawDirection dir) {
        return false;
    }
}
