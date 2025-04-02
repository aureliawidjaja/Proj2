package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;
import java.io.Serializable;
import java.util.Random;

public class Encounter implements Serializable {

    public static final int WIDTH = 90;
    public static final int HEIGHT = 50;

    public static final int screenXStart = 20;
    public static final int screenXEnd = 70;
    public static final int screenYStart = 15;
    public static final int screenYEnd = 35;

    public static final int screenX = 50;
    public static final int screenY = 30;
    private TETile[][] world = new TETile[WIDTH][HEIGHT];
    private static final int avatarXCoor = 21;
    private static final int avatarYCoor = HEIGHT/2;
    private static final Point initialPlayerPoint = new Point(21, HEIGHT/2);
    private static final Player avatar = new Player(initialPlayerPoint);
    //graphics
    private TETile pathTile;
    private TETile avatarTile;
    private TETile wallTile;
    private static final TETile lavaTile = Tileset.LAVA;
    private Engine engine;
    //storage variables
    private TETile[][] mainWorld;
    private Player mainPlayer;
    private long mainSeed;
    public static Encounter establishEncounter(Engine e) {
        Encounter mainE = new Encounter();
        mainE.engine = e;
        mainE.mainWorld = e.getWorld();
        mainE.mainPlayer = e.getPlayer();
        mainE.mainSeed = e.getSeed();
        mainE.pathTile = e.getFloorTile();
        mainE.wallTile = e.getWallTile();
        mainE.avatarTile = e.getAvatarTile();
        return mainE;
    }
    public static Player retrieveEncounterPlayer() {
        return avatar;
    }
    public void changePlayerPosition(int horz, int vert) {
        int oX, oY;
        oX = avatar.getX();
        oY = avatar.getY();
        int x, y;
        x = oX + horz;
        y = oY + vert;
        Point po = new Point(x,y);
        avatar.updatePlayerPositionEncounter(x,y);
    }


    public TETile[][] generateEncounterWorld(long seed) {
        int x, y;
        // Initialize world[][] with nothing except for edges where are a border
        for (x = 0; x < WIDTH; x++) {
            for (y = 0; y < HEIGHT; y++) {
                if (x == screenXStart && y > screenYStart && y < screenYEnd) {
                    world[x][y] = wallTile;
                } else if (x == screenXEnd && y > screenYStart && y < screenYEnd) {
                    world[x][y] = wallTile;
                } else if (y == screenYStart && x > screenXStart - 1 && x < screenXEnd + 1) {
                    world[x][y] = wallTile;
                } else if (y == screenYEnd && x > screenXStart - 1 && x < screenXEnd + 1) {
                    world[x][y] = wallTile;
                } else if (x == screenXStart + 1 && y < screenYEnd && y > screenYStart) {
                    world[x][y] = pathTile;
                }else if (x > screenXStart && x < screenXEnd && y > screenYStart && y < screenYEnd){
                    world[x][y] = Tileset.chasmTile;
                } else {
                    world[x][y] = Tileset.NOTHING;
                }
            }
        }
        //create the path to the end
        Random rand = new Random(seed);
        int verticalPoint = RandomUtils.uniform(rand, screenYStart+1, screenYEnd-1);
        int leftRightPoint = 23;
        world[leftRightPoint-1][verticalPoint] = pathTile;
        world[leftRightPoint][verticalPoint] = pathTile;

        boolean movingUp = false;
        boolean movingDown = false;

        while (leftRightPoint < screenXEnd-1) {
            int nextMove = rand.nextInt(3);
            if (nextMove == 0) {
                leftRightPoint += 1;
                world[leftRightPoint][verticalPoint] = pathTile;
            } else if (nextMove == 1) {
                if (movingDown) {
                    leftRightPoint += 1;
                    world[leftRightPoint][verticalPoint] = pathTile;
                    movingDown = false;
                    continue;
                }
                movingUp = true;
                if (verticalPoint + 1 >= screenYEnd - 1) {
                    continue;
                }
                verticalPoint += 1;
                world[leftRightPoint][verticalPoint] = pathTile;
            } else if (nextMove == 2) {
                if (movingUp) {
                    leftRightPoint += 1;
                    world[leftRightPoint][verticalPoint] = pathTile;
                    movingUp = false;
                    continue;
                }
                if (verticalPoint - 1 <= screenYStart) {
                    continue;
                }
                movingDown = true;
                verticalPoint -= 1;
                world[leftRightPoint][verticalPoint] = pathTile;
            }
        }
        world[screenXEnd-1][verticalPoint] = pathTile;
        avatar.updateEncounterStatus(true);
        return world;
    }

    public static void startEncounterScreen() {
        //StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16); // Each cell is 16x16 pixels
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        // Draw Title
        StdDraw.text(WIDTH / 2.0, HEIGHT / 1.5, "YOU HAVE STEPPED ON A HOLE");

        // Draw Options
        Font subtitle = new Font("Monaco", Font.ITALIC, 30);
        StdDraw.setFont(subtitle);
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.5, "CROSS THE CHASM TO ESCAPE");

        //Press to start
        Font subtitle2 = new Font("Monaco", Font.ITALIC, 20);
        StdDraw.setFont(subtitle2);
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.3, "Press (N) to start");

        StdDraw.show();
    }


    public TETile[][] endGame() {
        Font font = new Font("Serif", Font.BOLD, 50);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
        StdDraw.setPenColor(Color.RED);

        // Draw Title
        StdDraw.text(WIDTH / 2.0, 40, "YOU HAVE DIED");

        // Draw Options
        Font subtitle = new Font("Serif", Font.ITALIC, 30);
        StdDraw.setFont(subtitle);
        StdDraw.text(WIDTH / 2.0, 30, "WAH WAH WAH");
        StdDraw.text(WIDTH / 2.0, 20, "Enjoy the afterlife");

        StdDraw.show();
        StdDraw.pause(3000);

        StdDraw.clear(Color.BLACK);
        world = mainWorld;
        engine.setPlayer(mainPlayer);
        engine.setWorld(mainWorld);
        engine.setInEncounter(false);
        int px = mainPlayer.getX();
        int py = mainPlayer.getY();
        world[px][py] = Tileset.ghostAvatar;
        engine.changeAvatar(Tileset.ghostAvatar);
        engine.setGhost();
        return world;
    }
    public void setMainWorld(TETile[][] world) {
        mainWorld = world;
    }
    public void setMainPlayer (Player p) {
        mainPlayer = p;
    }
    public TETile[][] winEncounter() {
        //StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16); // Each cell is 16x16 pixels
        StdDraw.clear(Color.BLACK);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.enableDoubleBuffering();

        // Draw Title
        StdDraw.text(WIDTH / 2.0, HEIGHT / 1.5, "YOU HAVE ESCAPED");

        // Draw Options
        Font subtitle = new Font("Monaco", Font.ITALIC, 30);
        StdDraw.setFont(subtitle);
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.5, "WOO WOO WOO");
        StdDraw.show();
        StdDraw.pause(3000);
        world = mainWorld;
        int playerX = mainPlayer.getX();
        int playerY = mainPlayer.getY();
        world[playerX][playerY] = Tileset.AVATAR;
        engine.setWorld(mainWorld);
        engine.setPlayer(mainPlayer);
        engine.setInEncounter(false);
        return world;
    }
    public TETile[][] getMainWorld() {
        return mainWorld;
    }
    public Player getMainPlayer() {
        return mainPlayer;
    }


}