package info.sarihh.unimodeling.utility;

import info.sarihh.unimodeling.gui.DrawLayer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

/*
 * Author: Sari Haj Hussein
 */
public class DrawDelimiter extends DrawElement {

    public static int counter = 1;
    private boolean faint = false;

    public DrawDelimiter() {
        super();
        type = DrawLayer.DELIMITER;
    }

    public DrawDelimiter(String s) {
        super(s);
        type = DrawLayer.DELIMITER;
    }

    public DrawDelimiter(Point p1, Point p2) {
        super(p1, p2);
        type = DrawLayer.DELIMITER;
    }

    @Override
    public boolean hasPoint(Point pointer) {
        if (faint) {
            return false;
        }
        int sides[] = this.getSides();
        return (pointer.x >= sides[LEFT] && pointer.x <= sides[RIGHT]
                && pointer.y >= sides[TOP] && pointer.y <= sides[BOTTOM]);
    }

    @Override
    public boolean isColliding(DrawElement elem) {
        return elem.isCollidingWith(this);
    }

    @Override
    public boolean isCollidingWith(DrawDelimiter delimiter) {
        return false;
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
    public void draw(Graphics2D g) {
        if (!visible) {
            return;
        }
        defaultColor(g);
        int sides[] = this.getSides();
        if (faint) {
            g.setPaint(new Color(0, 127, 0, 63));
            g.fill(new Rectangle(sides[LEFT], sides[TOP],
                    sides[RIGHT] - sides[LEFT], sides[BOTTOM] - sides[TOP]));
        } else {
            g.draw(new Rectangle(sides[LEFT] + 1, sides[TOP] + 1,
                    sides[RIGHT] - sides[LEFT], sides[BOTTOM] - sides[TOP]));
            g.setColor(new Color(0, 127, 0, (dimmed) ? 63 : 255));
            g.draw(new Rectangle(sides[LEFT], sides[TOP],
                    sides[RIGHT] - sides[LEFT], sides[BOTTOM] - sides[TOP]));
        }
        g.setFont(new Font(g.getFont().getFontName(), Font.BOLD, 14));
        g.drawString(name, p1.x + 10, p1.y + 15);
    }

    @Override
    public boolean isCollidingWith(DrawConnection conn) {
        return boxCollide(conn);
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

    @Override
    public boolean isCollidingWith(DrawDirection dir) {
        return (this.hasPoint(dir.getPoint1()) || this.hasPoint(dir.getPoint2()));
    }

    public boolean isFaint() {
        return faint;
    }

    public void setFaint(boolean faint) {
        this.faint = faint;
    }
}
