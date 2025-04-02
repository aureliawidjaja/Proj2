package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.io.Serializable;

public class Player implements Serializable {

    private Point pt;
    private TETile t;

    private String name;
    private boolean inEncounter = false;

    public Player(Point pt) {
        this.pt = pt;
        inEncounter = false;
        t = Tileset.AVATAR;
        name = "Josh Hug";
    }

    public Point getPoint() {
        return pt;
    }

    public TETile getTile() { return t; }

    public int getX() { return pt.getX(); }

    public int getY() { return pt.getY(); }

    public void setPoint(int x, int y) {
        pt.setX(x);
        pt.setY(y);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void updateEncounterStatus(boolean status) {
        inEncounter = status;
    }
    public boolean inEncounter() {
        return inEncounter;
    }
    public void updatePlayerPositionEncounter(int x, int y) {
        Point newPoint = new Point(x,y);
        pt = newPoint;
    }


}
