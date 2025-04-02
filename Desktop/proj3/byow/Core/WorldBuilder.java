package byow.Core;

import byow.TileEngine.*;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class WorldBuilder implements Serializable {
    Random rand;
    public static int FRAME_WIDTH  = 90;
    public static int FRAME_HEIGHT = 50;

    public static int MIN_ROOM_WIDTH = 4;
    public static int MAX_ROOM_WIDTH = 8;

    public static int MIN_ROOM_HEIGHT = 4;
    public static int MAX_ROOM_HEIGHT = 8;
    /**
     * Abstraction for moving directions:
     */
    public static final int NORTH = 1;
    public static final int SOUTH = 2;
    public static final int EAST = 3;
    public static final int WEST = 4;

    public static final int HALLWAY = 1;
    public static final int ROOM = 2;

    TETile[][] world;

    private TETile wallTile = Tileset.WALL;
    private TETile floorTile = Tileset.FLOOR;

    /**
     * x and y are used to track current coordinate
     *
     */
    int x;
    int y;

    int prev_x = 0;
    int prev_y = 0;

    int frameWidth;
    int frameHeight;

    int width = 0;
    int height = 0;

    int direction = 0;

    LinkedList<Room> roomList;

    // The (only) door into/from world:
    int x_door = 0;
    int y_door = 0;

    public WorldBuilder(Random rand, int frameWidth, int frameHeight, int x_start, int y_start) {

        this.rand = rand;

        world = new TETile[frameWidth][frameHeight];

        roomList = new LinkedList<>();

        for (int i = 0; i < frameWidth; i++) {
            for (int j = 0; j < frameHeight; j++) {
                world[i][j] = Tileset.NOTHING;
            }
        }

        this.frameWidth  = frameWidth;
        this.frameHeight = frameHeight;

        x = x_start;
        y = y_start;
    }

    public Point getDoorCoordinate() {
        return new Point(x_door, y_door);
    }

    public int getCurrentX() {
        return x;
    }

    public int getCurrentY() {
        return y;
    }


    public void buildWall(int x, int y) {
        if (world[x][y] != floorTile) {
            // We don't want to wall up an existing floor
            world[x][y] = wallTile;
        }
    }

    public void buildFloor(int x, int y) {
        world[x][y] = floorTile;
    }

    protected Room modelShape(int direction, int x_start, int y_start, int width, int height) {
        if (x_start + width >= frameWidth || y_start - height < 0 ||
                width <= 0 || height <= 0 || x_start <= 0 || y_start >= frameHeight) {
            // It would shoot through the borders so just return null since
            // we can't use this room model to build a valid room.
            return null;
        }

        Room r = new Room(direction, x_start, y_start, width, height);
        Room neighbor = roomList.peekLast();

        Iterator<Room> neighborIterator = roomList.iterator();

        while (neighborIterator.hasNext()) {
            if (neighborIterator.next().intersects(r)) {
                // It would collide with its neighbors so just return null since
                // we can't use this room model to build a valid room.
                return null;
            }
        }

        this.direction = direction;
        return r;
    }
    public Room modelRoom(int direction, int width, int height) {
        int x_start = 0, y_start = 0;

        switch (direction) {
            case NORTH:
                x_start = x;
                y_start = y + height;
                break;

            case SOUTH:
                x_start = x;
                y_start = y - this.height;
                break;

            case EAST:
                x_start = x + this.width;
                y_start = y;
                break;

            case WEST:
                x_start = x - width;
                y_start = y;
                break;

            default:
                x_start = x;
                y_start = y;
                break;
        }

        return modelShape(direction, x_start, y_start, width, height);
    }

    public Room modelHallway(int direction, int length) {
        int x_start = 0, y_start = 0;
        int new_width  = 0;
        int new_height = 0;

        switch (direction) {
            case NORTH:
                new_width  = 3;
                new_height = length;

                x_start  = x;
                y_start  = y + new_height;
                break;

            case SOUTH:
                new_width  = 3;
                new_height = length;

                x_start  = x;
                y_start  = y - this.height;
                break;

            case EAST:
                new_width  = length;
                new_height = 3;

                x_start = x + this.width;
                y_start = y;
                break;

            case WEST:
                new_width  = length;
                new_height = 3;

                x_start = x - new_width;
                y_start = y;
                break;

            default:
                new_width  = length;
                new_height = 3;
                x_start = x;
                y_start = y;
                break;
        }

        return modelShape(direction, x_start, y_start, new_width, new_height);
    }

    public Room model(int shape, int direction, int width, int height) {
        switch (shape) {
            case HALLWAY:
                int length;

                if (height > width) {
                    length = height;
                } else {
                    length = width;
                }
                return modelHallway(direction, length);

            case ROOM:
                return  modelRoom(direction, width, height);

            default:
                return null;
        }
    }

    public boolean build(Room r) {
        int x_start, y_start, x_end, y_end;

        x_start  = r.getStartPoint().getX();
        x_end    = r.getEndPoint().getX();

        y_start  = r.getStartPoint().getY();
        y_end    = r.getEndPoint().getY();

        this.width  = (int) r.getWidth();
        this.height = (int) r.getHeight();

        for (int i = x_start; i < x_end; i++) {
            // for (int j = y_start; j < y_start + height; j++) {
            for (int j = y_end; j < y_start; j++) {
                if (i == x_start || j == y_end || i == x_end - 1 || j == y_start - 1 ) {
                    buildWall(i, j);
                } else {
                    buildFloor(i, j);
                }
            }
        }

        // Place x and y at the NORTH EAST (i.e. TOP LEFT) corner:
        x = x_start;
        y = y_start;

        Room neighbor = roomList.peekLast();
        if (neighbor != null) {
            // If there is a neighboring room or hallway previously, then we want to add this (current) room
            // as a neighbor of "neighbor":
            neighbor.addNeighbor(direction, r);

            switch (direction) {
                case NORTH:
                    r.addNeighbor(SOUTH, neighbor);
                    break;

                case SOUTH:
                    r.addNeighbor(NORTH, neighbor);
                    break;

                case EAST:
                    r.addNeighbor(WEST, neighbor);
                    break;

                case WEST:
                    r.addNeighbor(EAST, neighbor);
                    break;
            }
        }
        roomList.addLast(r);
        return true;
    }

    public void installDoor() {
        // We don't want to install a door at the first or the last room:
        int roomCount = roomList.size();
        int roomIndex;

        if (roomCount > 1) {
            roomIndex = RandomUtils.uniform(rand, 0, roomCount / 2);
        } else {
            roomIndex = 0;
        }

        Room r = roomList.get(roomIndex);

        int x_start = r.getStartPoint().getX();
        int y_start = r.getStartPoint().getY();

        int x_end = r.getEndPoint().getX();
        int y_end = r.getEndPoint().getY();

        int x_roomConnection = r.getRoomConnection().getX();
        int y_roomConnection = r.getRoomConnection().getY();

        boolean doorPositionOK = false;
        int retryCount = 0;


        do {
            x_door = RandomUtils.uniform(rand, x_start + 1, x_end);
            y_door = RandomUtils.uniform(rand, y_end + 1, y_start);

            if (world[x_door][y_door] == wallTile &&
                    world[x_door - 1][y_door] == floorTile &&
                    world[x_door + 1][y_door] == Tileset.NOTHING) {
                doorPositionOK = true;
            } else if (world[x_door][y_door] == wallTile &&
                    world[x_door - 1][y_door] == Tileset.NOTHING &&
                    world[x_door + 1][y_door] == floorTile) {
                doorPositionOK = true;
            } else if (world[x_door][y_door] == wallTile &&
                    world[x_door][y_door - 1] == Tileset.NOTHING &&
                    world[x_door][y_door + 1] == floorTile) {
                doorPositionOK = true;
            } else if (world[x_door][y_door] == wallTile &&
                    world[x_door][y_door + 1] == Tileset.NOTHING &&
                    world[x_door][y_door - 1] == floorTile) {
                doorPositionOK = true;
            }

            retryCount++;
            if (retryCount > 1) {
                if (roomCount > 1) {
                    roomIndex = RandomUtils.uniform(rand, 0, roomCount / 2);
                } else {
                    roomIndex = 0;
                }
            }
        } while (!doorPositionOK && retryCount < 10);

        world[x_door][y_door] = Tileset.LOCKED_DOOR;
    }

    public TETile[][] getWorld() {
        return world;
    }

    public void connectRooms() {
        int roomCount = roomList.size();

        Room cr = roomList.get(roomCount - 1); // Current room
        Room pr = roomList.get(roomCount - 2); // Previous room

        int Xcr_start, Xcr_end, Ycr_start, Ycr_end;
        int Xpr_start, Xpr_end, Ypr_start, Ypr_end;

        int x_hole_lower, x_hole_upper, x_hole_east, x_hole_west, x_hole;
        int y_hole_lower, y_hole_upper, y_hole;

        switch (cr.getDirection()) {
            case NORTH:
                // Current room is NORTH of previous room which means that the BOTTOM of this room
                // (i.e. [Xcr_start, Ycr_end] and [Xcr_end, Ycr_end] touches the TOP of the room
                // below this room [Xpr_start, Ypr_start] and [Xpr_end, Ypr_start]
                Xcr_start = cr.getStartPoint().getX();
                Xcr_end   = cr.getEndPoint().getX();
                Ycr_end   = cr.getEndPoint().getY();

                Xpr_start = pr.getStartPoint().getX();
                Xpr_end   = pr.getEndPoint().getX();
                Ypr_start = pr.getStartPoint().getY();

                if (Xcr_start >= Xpr_start) {
                    x_hole_lower = Xcr_start;
                } else {
                    x_hole_lower = Xpr_start;
                }

                if (Xcr_end >= Xpr_end) {
                    x_hole_upper = Xpr_end;
                } else {
                    x_hole_upper = Xcr_end;
                }

                // Create a random hole between the given bounds:
                x_hole = RandomUtils.uniform(rand, x_hole_lower + 1, x_hole_upper - 1);
                y_hole_upper = Ycr_end;
                y_hole_lower = Ypr_start - 1;

                buildFloor(x_hole, y_hole_upper);
                buildFloor(x_hole, y_hole_lower);

                // Store these coordinates for future usage.
                cr.setRoomConnection(x_hole, y_hole_upper);
                pr.setRoomConnection(x_hole, y_hole_lower);

                break;

            case SOUTH:
                // Current room is SOUTH of previous room which means that the TOP of this room
                // (i.e. [Xcr_start, Ycr_start] and [Xcr_end, Ycr_start] touches the BOTTOM of the room
                // below this room [Xpr_start, Ypr_end] and [Xpr_end, Ypr_end]
                Xcr_start = cr.getStartPoint().getX();
                Xcr_end   = cr.getEndPoint().getX();
                Ycr_start = cr.getStartPoint().getY();

                Xpr_start = pr.getStartPoint().getX();
                Xpr_end   = pr.getEndPoint().getX();
                Ypr_end   = pr.getEndPoint().getY();

                if (Xcr_start >= Xpr_start) {
                    x_hole_lower = Xcr_start;
                } else {
                    x_hole_lower = Xpr_start;
                }

                if (Xcr_end >= Xpr_end) {
                    x_hole_upper = Xpr_end;
                } else {
                    x_hole_upper = Xcr_end;
                }

                // Create a random hole between the given bounds:
                x_hole = RandomUtils.uniform(rand, x_hole_lower + 1, x_hole_upper - 1);
                y_hole_upper = Ypr_end;
                y_hole_lower = Ycr_start - 1;

                buildFloor(x_hole, y_hole_upper);
                buildFloor(x_hole, y_hole_lower);

                // Store these coordinates for future usage.
                cr.setRoomConnection(x_hole, y_hole_lower);
                pr.setRoomConnection(x_hole, y_hole_upper);
                break;

            case EAST:
                // Current room is EAST of previous room which means that the WEST wall of this room
                // (i.e. [Xcr_start, Ycr_start] and [Xcr_start, Ycr_end] touches the EAST wall of the room
                // WEST of current room [Xpr_end, Ypr_start] and [Xpr_end, Ypr_end]

                Xcr_start = cr.getStartPoint().getX();
                Ycr_start = cr.getStartPoint().getY();
                Ycr_end   = cr.getEndPoint().getY();

                Xpr_end   = pr.getEndPoint().getX();
                Ypr_start = pr.getStartPoint().getY();
                Ypr_end   = pr.getEndPoint().getY();

                if (Ycr_end >= Ypr_end) {
                    y_hole_lower = Ycr_end;
                } else {
                    y_hole_lower = Ypr_end;
                }

                if (Ycr_start >= Ypr_start) {
                    y_hole_upper = Ypr_start;
                } else {
                    y_hole_upper = Ycr_start;
                }

                // Create a random hole between the given bounds:
                y_hole = RandomUtils.uniform(rand, y_hole_lower + 1, y_hole_upper - 1);
                x_hole_east = Xcr_start;
                x_hole_west = x_hole_east - 1;

                buildFloor(x_hole_east, y_hole);
                buildFloor(x_hole_west, y_hole);

                // Store these coordinates for future usage.
                cr.setRoomConnection(x_hole_east, y_hole);
                pr.setRoomConnection(x_hole_west, y_hole);
                break;

            case WEST:
                // Current room is WEST of previous room which means that the EAST wall of this room
                // (i.e. [Xcr_end, Ycr_start] and [Xcr_end, Ycr_end] touches the WEST wall of the room
                // EAST of this room [Xpr_start, Ypr_start] and [Xpr_start, Ypr_end]

                Xcr_end   = cr.getEndPoint().getX();
                Ycr_start = cr.getStartPoint().getY();
                Ycr_end   = cr.getEndPoint().getY();

                Xpr_start = pr.getStartPoint().getX();
                Ypr_start = pr.getStartPoint().getY();
                Ypr_end   = pr.getEndPoint().getY();

                if (Ycr_end >= Ypr_end) {
                    y_hole_lower = Ycr_end;
                } else {
                    y_hole_lower = Ypr_end;
                }

                if (Ycr_start >= Ypr_start) {
                    y_hole_upper = Ypr_start;
                } else {
                    y_hole_upper = Ycr_start;
                }

                // Create a random hole between the given bounds:
                y_hole = RandomUtils.uniform(rand, y_hole_lower + 1, y_hole_upper - 1);
                x_hole_east = Xpr_start;
                x_hole_west = x_hole_east - 1;

                buildFloor(x_hole_east, y_hole);
                buildFloor(x_hole_west, y_hole);

                // Store these coordinates for future usage.
                cr.setRoomConnection(x_hole_west, y_hole);
                pr.setRoomConnection(x_hole_east, y_hole);
                break;

            default:
                break;
        }
    }

    /**
     * A static method that generates a 2D world pseudo-randomly
     * @param seed: Used for generating pseudo-random numbers
     * @param maxShapeCount: Maximum number of shapes/rooms/hallways
     * @return a random 2D world whose rooms and hallways are interconnected.
     */
    public static TETile[][] generateWorld(long seed, int maxShapeCount, TETile wall, TETile floor) {
        Random rand = new Random(seed);

        int x_start = RandomUtils.uniform(rand, 8, 30);
        int y_start = RandomUtils.uniform(rand, 6, 20);
        int direction = 0;

        WorldBuilder wb = new WorldBuilder(rand, FRAME_WIDTH, FRAME_HEIGHT, x_start, y_start);

        wb.wallTile = wall;
        wb.floorTile = floor;

        int width;
        int height;
        int length;

        // Shape can only be either a hallway or room:
        int shape = RandomUtils.uniform(rand, HALLWAY, ROOM + 1);

        int retryCount;

        Room r;

        for (int i = 0; i < maxShapeCount; i++) {
            // Step 1: Determine width and height pseudo-randomly
            width   = RandomUtils.uniform(rand, MIN_ROOM_WIDTH, MAX_ROOM_WIDTH + 1);
            height  = RandomUtils.uniform(rand, MIN_ROOM_HEIGHT, MAX_ROOM_HEIGHT + 1);

            if (shape == HALLWAY) {
                shape = ROOM;
            } else {
                shape = HALLWAY;
            }

            retryCount = 0;
            do {
                // Step 2: Determine direction pseudo-randomly. Current direction indicates in which
                // direction current rectangle will be placed relative to previous rectangle.
                if (i > 0) {
                    direction = RandomUtils.uniform(rand, NORTH, WEST + 1);
                } else {
                    direction = 0; // Initial direction (neither NORTH, SOUTH, EAST nor WEST)
                }

                // Step 3: Before we actually build a room/hallway/rectangle we need to model it first to make
                // sure that it won't overlap/intersect with other existing rectangles or frame borders. If it
                // does intersect, the model method returns null.
                r = wb.model(shape, direction, width, height);

                retryCount++;

                if (retryCount > 1) {
                    // Step 4: If it's been retried more than once, it means there were intersections/overlaps
                    // with the model. In order to reduce the odds of encountering intersections/overlaps, we
                    // reshuffle the direction, width and height and hope that the change will open up an unblocked
                    // or unintersected space.
                    direction = RandomUtils.uniform(rand, NORTH, WEST + 1);
                    width     = RandomUtils.uniform(rand, MIN_ROOM_WIDTH - 1, MAX_ROOM_WIDTH - 1);
                    height    = RandomUtils.uniform(rand, MIN_ROOM_HEIGHT - 1, MAX_ROOM_HEIGHT - 1);
                    // System.out.println("Iteration #: "+i+" | Shape: "+shape+" | Direction: "+direction+" | Retry count: "+retryCount);
                }

            } while (r == null && retryCount < 20);

            if (r != null) {
                // Step 5: Once we get a valid model (as an instance of Room/Rectangle) we want to build the room or
                // hallway according to the given model.
                wb.build(r);
            }

            if (i > 0) {
                // Step 6: Connect current and previous rooms with each other:
                wb.connectRooms();
            }
        }

        // Step 7: Once the world has been built, we want to install a locked door randomly
        wb.installDoor();

        // Step 8: Return the generated world to the called.
        return wb.getWorld();
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(FRAME_WIDTH, FRAME_HEIGHT);

//        TETile[][] world = WorldBuilder.generateWorld(1234567801, 5);
//        TETile[][] world = WorldBuilder.generateWorld(1234567801, 10);
//        TETile[][] world = WorldBuilder.generateWorld(1234567801, 20);
//        TETile[][] world = WorldBuilder.generateWorld(1234567801, 30);
//        TETile[][] world = WorldBuilder.generateWorld(1234567801, 40);
        //long seed = Long.valueOf( "5197880843569031643");
        long seed = Long.valueOf( "9223372036854775807");
        //TETile[][] world = WorldBuilder.generateWorld(seed, 50);

        //ter.renderFrame(world);
    }
}