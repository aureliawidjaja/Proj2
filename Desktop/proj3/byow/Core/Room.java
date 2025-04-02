package byow.Core;

import java.awt.Rectangle;
import java.util.HashMap;

public class Room extends Rectangle {
    public static final int NORTH = WorldBuilder.NORTH;
    public static final int SOUTH = WorldBuilder.SOUTH;
    public static final int EAST = WorldBuilder.EAST;
    public static final int WEST = WorldBuilder.WEST;

    private int x_start;
    private int y_start;
    private int x_end;
    private int y_end;

    private int x_roomConnection;
    private int y_roomConnection;

    private Point startPoint;
    private Point endPoint;

    private int direction;

    private static HashMap<Integer, Room> neighbors;

    public Room(int direction, int x, int y, int width, int height) {
        super(x, y, width, height);

        this.direction = direction;

        // Start coordinate is located on NORTH EAST (or TOP LEFT) corner:
        x_start = x;
        y_start = y;

        x_end = x + width;
        y_end = y - height;

        startPoint = new Point(x_start, y_start);
        endPoint   = new Point(this.x_end, this.y_end);

        neighbors = new HashMap<>();
    }

    public int getDirection() {
        return direction;
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public void addNeighbor(int direction, Room r) {
        neighbors.put(Integer.valueOf(direction), r);
    }

    public void setRoomConnection(int x, int y) {
        x_roomConnection = x;
        y_roomConnection = y;
    }

    public Point getRoomConnection() {
        return new Point(x_roomConnection, y_roomConnection);
    }

    public Room getNeighbor(int direction) {
        return neighbors.get(Integer.valueOf(direction));
    }

    public boolean isNeighbor(Room r) {
        return neighbors.containsValue(r);
    }

}
