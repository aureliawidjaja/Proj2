package byow.Core;

import java.io.Serializable;
import java.util.*;

/* The treasure object will be a non-floor object that the player must walk
through the world to find. */
public class Treasure implements Serializable {

    private static HashSet<Treasure> trinkets;
    private Random r;

    private Point pt;


    public Treasure() {
        HashSet<Treasure> trinkets = new HashSet<>();
    }

    public static void addTrinket(Treasure t) {
        trinkets.add(t);
    }

    public HashSet<Treasure> getTrinkets() {
        return trinkets;
    }

    public void setPoint(int x, int y) {
        pt = new Point(x,y);
    }

    public int getX() {
        return pt.getX();
    }

    public int getY() {
        return pt.getY();
    }


    public Point getPoint() {
        return pt;
    }
}
