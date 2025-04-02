package byow.Core;

import byow.Input.KeyboardInputSource;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Engine implements Serializable{
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 90;
    public static final int HEIGHT = 50;
    private Random rand;
    private int seed;
    private TETile[][] world = new TETile[WIDTH][HEIGHT];
    private Treasure treasure;
    String gameStatus = "Idle";
    private TETile wallTile = Tileset.WALL;
    private TETile floorTile = Tileset.FLOOR;
    private TETile avatarTile = Tileset.AVATAR;
    private Player p;
    private Player ep = Encounter.retrieveEncounterPlayer();
    private Encounter ec;
    boolean inEncounter;
    boolean isGhost = false;
    private String hudText = " ";
    private String playerName = "";

    public Engine() {
        rand = new Random(seed);
    }

    public static void main(String[] args) {
        Engine e = new Engine();
        e.interactWithKeyboard();
    }
    public int getSeed() {
        return seed;
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, running both of these:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        String inputStr = input.trim().toUpperCase();
        //String inputStr = input.toUpperCase();
        char[] inputStrArray = inputStr.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : inputStrArray) {
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        long seedStr = Long.valueOf(sb.toString());
        return WorldBuilder.generateWorld(seedStr, 50, wallTile, floorTile);
        //generate our world
    }

    public void interactWithKeyboard() {
        //process keys to play the game
        drawMenu();
        Engine e = new Engine();
        KeyboardInputSource inputSource = new KeyboardInputSource();
        while (true) {
            if (inputSource.possibleNextInput()) {
                char key = inputSource.getNextKey();
                e.keyInput(key);
            }

            e.displayHUD();
        }
    }

    String prevState = "";
    public void keyInput(char key) {
        System.out.println(gameStatus);
        System.out.print(inEncounter);
        switch (gameStatus) {
            case "Idle" -> {
                switch (key) {
                    case 'N' -> {
                        drawSeedMenu();
                        changeGameStatus("SeedMenu");
                    }
                    case 'L' ->
                            loadGamestate();
                    case 'C' -> {
                        drawAppearanceMenu();
                        changeGameStatus("CustomizationMenu");
                    }
                    case ':' -> {
                        saveGameState();
                        prevState = gameStatus;
                        changeGameStatus("Quitting");
                    }
                }
            }
            case "Quitting" -> {
                if (key == 'Q') {
                    System.exit(0);
                } else {
                    changeGameStatus(prevState);
                }
            }
            case "SeedMenu" -> {
                if (Character.isDigit(key)) {
                    int charNum = Character.getNumericValue(key);
                    seed *= 10;
                    seed += charNum;
                    drawSeedMenu();
                } if (key == 'S') {
                    startGame();
                    changeGameStatus("Playing");
                } else if (key == ':') {
                    saveGameState();
                    prevState = gameStatus;
                    changeGameStatus("Quitting");
                    break;
                }
                else if (!Character.isDigit(key)) {
                    drawSeedError();
                }
            }
            case "CustomizationMenu" -> {
                switch (key) {
                    case 'N' -> {
                        drawNameMenu();
                        changeGameStatus("NameCustomization");
                    }
                    case 'W' -> {
                        drawWorldAppearanceMenu();
                        changeGameStatus("WorldCustomization");
                    }
                    case 'A' -> {
                        drawAvatarAppearanceMenu();
                        changeGameStatus("AvatarCustomization");
                    }
                    case ':' -> {
                        prevState = gameStatus;
                        saveGameState();
                        changeGameStatus("Quitting");
                    }
                }
            }
            case "NameCustomization" -> {
                if (key == 'N') {
                    drawMenu();
                    changeGameStatus("Idle");
                } else if (key == 'Y') {
                    drawName(playerName);

                    while (playerName.length() < 15) {
                        if (!StdDraw.hasNextKeyTyped()) {
                            continue;
                        }
                        char ch = StdDraw.nextKeyTyped();
                        if (Character.isLetter(ch)) {
                            playerName += String.valueOf(ch);;
                            drawName(playerName);
                        }
                        if (ch == '0') {
                            drawMenu();
                            changeGameStatus("Idle");
                            break;
                        }
                    }
                }
                if (key == ':') {
                    prevState = gameStatus;
                    saveGameState();
                    changeGameStatus("Quitting");
                }
            }

            case "WorldCustomization" -> {
                switch (key) {
                    case '1' -> {
                        //default world
                        floorTile = Tileset.FLOOR;
                        wallTile = Tileset.WALL;
                        drawMenu();
                        changeGameStatus("Idle");
                    }
                    case '2' -> {
                        //icyWorld
                        floorTile = Tileset.iceFloor;
                        wallTile = Tileset.iceWall;
                        drawMenu();
                        changeGameStatus("Idle");
                    }
                    case '3' -> {
                        //strangeWorld
                        floorTile = Tileset.FLOWER;
                        wallTile = Tileset.LAVA;
                        drawMenu();
                        changeGameStatus("Idle");
                    }
                    case ':' -> {
                        prevState = gameStatus;
                        saveGameState();
                        changeGameStatus("Quitting");
                    }
                }
            }
            case "AvatarCustomization" -> {
                switch (key) {
                    case '1' -> {
                        avatarTile = Tileset.AVATAR;
                        drawMenu();
                        changeGameStatus("Idle");
                    }
                    case '2' -> {
                        avatarTile = Tileset.AVATAR1;
                        drawMenu();
                        changeGameStatus("Idle");
                    }
                }
            }
            case "Playing" -> {
                switch (key) {
                    case 'W'->
                            moveAvatar(0, 1);
                    case 'A' ->
                            moveAvatar( -1, 0);
                    case 'S' ->
                            moveAvatar( 0, -1);
                    case 'D' ->
                            moveAvatar( 1, 0);
                    case':' -> {
                        prevState = gameStatus;
                        saveGameState();
                        changeGameStatus("Quitting");
                    }
                }
                break;
            }
            case "Encounter" -> {
                switch (key) {
                    case 'N' -> {
                        initializeEncounter();
                        changeGameStatus("Playing");
                    }
                }
//                Encounter newEncounter = Encounter.establishEncounter(this);
//                //newEncounter.interactWithKeyboard();
//                TETile[][] encounterWorld = newEncounter.generateEncounterWorld(seed);
//                Encounter.startEncounterScreen();
//                Player ePlayer = newEncounter.retrieveEncounterPlayer();
//                int x = ePlayer.getX();
//                int y = ePlayer.getY();
//                encounterWorld[x][y] = avatarTile;
//                this.p = ePlayer;
//                ter.renderFrame(encounterWorld);
//                break;
            }
            case "Return" -> {
                drawWon();
                changeGameStatus("PressX");
            }
            case "PressX" -> {
                if (key == 'X') {
                    System.exit(0);
                }
            }
        }
    }
    public void startGame() {
        String intString = Long.toString(seed);
        TETile[][] initialMap = interactWithInputString(intString);
        if (p == null) {
            TETile[][] worldPlayer = initializePlayer(initialMap);
            world = worldPlayer;
        } else {
            int x = p.getX();
            int y = p.getY();
            initialMap[x][y] = avatarTile;
            world = initialMap;
        }
        initializeEncounterPoint();
        if (treasure == null) {
            setTreasure();
        } else {
            int tX = treasure.getX();
            int tY = treasure.getY();
            world[tX][tY] = Tileset.chest;
        }
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(world);
    }
    public void initializeEncounter() {
        ec = Encounter.establishEncounter(this);
        TETile[][] encounterWorld = ec.generateEncounterWorld(seed);
        Encounter.startEncounterScreen();
        Player ePlayer = ec.retrieveEncounterPlayer();
        int x = ePlayer.getX();
        int y = ePlayer.getY();
        encounterWorld[x][y] = avatarTile;
        ep = ePlayer;
        TERenderer ter = new TERenderer();
        inEncounter = true;
        world = encounterWorld;
        ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(encounterWorld);
    }

    public void restartEncounter() {
        TETile[][] encounterWorld = ec.generateEncounterWorld(seed);
        int x = ep.getX();
        int y = ep.getY();
        encounterWorld[x][y] = avatarTile;
        inEncounter = true;
        world = encounterWorld;
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(encounterWorld);
    }

    public TETile[][] getWorld() {
        return world;
    }
    public Player getPlayer() {
        return p;
    }

    public TETile[][] initializePlayer(TETile[][] world) { //player is always initialized in the bottom left of the game
        int x,y;
        for (x = 0; x < WIDTH ; x++) {
            for (y = 0; y < HEIGHT ; y++) {
                if (world[x][y] == floorTile) {
                    world[x][y] = avatarTile;
                    Point playerPoint = new Point(x,y);
                    p = new Player(playerPoint);
                    return world;
                }
            }
        }
        return world;
    }
    public TETile[][] blankWorld() {
        TETile[][] blank = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                blank[x][y] = Tileset.NOTHING;
            }
        }
        return blank;
    }
    public void initializeEncounterPoint() {
        Random rand = new Random(seed);
        int x = rand.nextInt(WIDTH);
        int y = rand.nextInt(HEIGHT);
        while (!isTileFloor(x, y)) {
            x = rand.nextInt(WIDTH);
            y = rand.nextInt(HEIGHT);
        }
        this.world[x][y] = Tileset.chasmTile;
    }

    public static void drawWorldAppearanceMenu() {
        StdDraw.clear(Color.BLACK);
        Font font = new Font("Monaco", Font.BOLD, 50);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.setPenColor(Color.ORANGE);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        StdDraw.text(WIDTH/3, 40, "Choose your world:");

        StdDraw.text(WIDTH/2, 30, "(1) Default World");
        StdDraw.text(WIDTH/2, 25, "(2) Icy World");
        StdDraw.text(WIDTH/2, 20, "(3) Strange World");

        StdDraw.show();
    }
    public static void drawAvatarAppearanceMenu() {
        StdDraw.clear();
        Font font = new Font("Monaco", Font.BOLD, 50);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.setPenColor(Color.ORANGE);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        StdDraw.text(WIDTH/3, 40, "Choose your Avatar:");

        StdDraw.text(WIDTH/2, 30, "(1) Default Avatar");
        StdDraw.text(WIDTH/2, 25, "(2) Icy Avatar");
        StdDraw.text(WIDTH/2, 20, "(3) Strange Avatar");

        StdDraw.show();
    }


    public void displayHUD() {
        int mouseX = (int)StdDraw.mouseX();
        int mouseY = (int)StdDraw.mouseY();

        if (world == null ||  mouseX == 0 || mouseX == WIDTH || mouseY == 0 ||  mouseY == HEIGHT
                || world[mouseX][mouseY] == null) {
            return;
        }

        String hudDescription = world[mouseX][mouseY].description();

        if (hudText == hudDescription) {
            // If the caption hasn't changed, we don't want to update the UI
            return;
        }

        hudText = hudDescription;

        Font font = new Font("Times New Roman", Font.BOLD, 14);
        StdDraw.setFont(font);



        StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
        StdDraw.filledRectangle(2, HEIGHT - 1, WIDTH, 1);
        StdDraw.setPenColor(StdDraw.WHITE);

        StdDraw.textRight(WIDTH - 10, HEIGHT - 1, "Player name: ");
        StdDraw.textRight(WIDTH - 4, HEIGHT - 1, playerName);

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date date = new Date();
        StdDraw.textLeft( 2, HEIGHT - 1.5, formatter.format(date));

        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.line(0, HEIGHT - 2, WIDTH, HEIGHT - 2);

        StdDraw.textLeft( 2, HEIGHT - 0.5, hudText);
        StdDraw.show();
    }


    public void drawSeedMenu() {
        //This is how we will solicit the seed!
        Font font = new Font("Monaco", Font.BOLD, 50);
        StdDraw.setPenColor(Color.ORANGE);
        StdDraw.setFont(font);
        StdDraw.clear(Color.BLACK);

        StdDraw.text(WIDTH / 2, 40, "PLEASE ENTER A SEED NUMBER");
        StdDraw.text(WIDTH / 2, 30, Long.toString(seed));
        StdDraw.text(WIDTH / 2, 20, "PLEASE PRESS 'S' TO START");
        StdDraw.show();
        StdDraw.pause(1);
    }

    public void drawSeedError() {
        StdDraw.setPenColor(Color.RED);
        Font errorFont = new Font("Montana", Font.ITALIC, 25);
        StdDraw.setFont(errorFont);
        StdDraw.text(WIDTH / 2, 6, "Please enter a number");
        StdDraw.text(WIDTH/2, 10, "Please enter a number");
        StdDraw.show();
    }

    public void drawNameMenu() {
        Font font = new Font("Monaco", Font.BOLD, 50);
        StdDraw.setPenColor(Color.ORANGE);
        StdDraw.setFont(font);
        StdDraw.clear(Color.BLACK);

        StdDraw.text(WIDTH / 2, 40, "Would you like to enter a name?");
        StdDraw.text(WIDTH / 2, 35, "(Y)es");
        StdDraw.text(WIDTH / 2, 30, "(N)o");
        StdDraw.show();
        StdDraw.pause(1);
    }


    public static void drawMenu() {
        StdDraw.clear();
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16); // Each cell is 16x16 pixels
        Font font = new Font("Monaco", Font.BOLD, 50);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.setPenColor(Color.YELLOW);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
        StdDraw.text(WIDTH / 2, 40, "WElCOME TO TREASURE ISLAND");
        StdDraw.text(WIDTH / 2, 30, "(N)EW GAME");
        StdDraw.text(WIDTH / 2, 25, "(L)OAD");
        StdDraw.text(WIDTH / 2, 20, "(C)USTOMIZE");
        StdDraw.text(WIDTH / 2, 15, "(Q)UIT");
        StdDraw.show();
    }
    public void drawAppearanceMenu() {
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
        StdDraw.text(WIDTH/4, 25, "What would you like to customize?");
        StdDraw.text(WIDTH/2, 22, "(N)ame");
        StdDraw.text(WIDTH/2, 18, "(W)orld");
        StdDraw.text(WIDTH/2, 14, "(A)vatar");
        StdDraw.show();
    }
    public void drawLoadError() {
        Font font = new Font("Monaco", Font.ITALIC, 30);
        StdDraw.setFont(font);
        StdDraw.setPenColor(Color.RED);
        StdDraw.text(WIDTH/2, 10, "No save available");
        StdDraw.show();
    }




    public void drawName(String name) {
        StdDraw.clear();
        StdDraw.clear(Color.black);
        Font f = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(f);
        StdDraw.setPenColor(Color.white);

        StdDraw.text(WIDTH / 2, 40, "Maximum 15 characters.");
        StdDraw.text(WIDTH / 2, 35, name);
        StdDraw.text(WIDTH / 2, 30, "Press '0' when done.");
        StdDraw.show();
    }


    public void drawWon() {
        Font font = new Font("Monaco", Font.BOLD, 50);
        StdDraw.setFont(font);
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(StdDraw.YELLOW);

        StdDraw.text(WIDTH / 2, 40, "You found the treasure! Congratulations!");
        StdDraw.text(WIDTH / 2, 30, "Press (X) to exit the game.");
        StdDraw.show();
    }


// idea for persistence: create a class called gamestate that holds all relevant data
    //that needs to be stored

    public void loadGamestate() {
        File file = new File("savefile.txt");
        if (file.exists()) {
            try {
                FileInputStream fIS = new FileInputStream(file);
                ObjectInputStream oIS = new ObjectInputStream(fIS);
                rand = (Random) oIS.readObject();
                p = (Player) oIS.readObject();
                ter = (TERenderer) oIS.readObject();
                treasure = (Treasure) oIS.readObject();
                seed = (int) oIS.readObject();
                ec = (Encounter) oIS.readObject();
                inEncounter = (boolean) oIS.readObject();
                //ep = (Player) oIS.readObject();
                changeGameStatus("Playing");
                if (inEncounter) {
                    String intString = Long.toString(seed);
                    TETile[][] mainWorld = interactWithInputString(intString);
                    //ec.setMainWorld(mainWorld);
                    //ec.setMainPlayer(p);
                    restartEncounter();
                } else {
                    startGame();
                }
            } catch (FileNotFoundException e) {
                System.out.println("No saved data is available");
                System.exit(0);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e);
                System.exit(0);
            }
        } else{
            drawLoadError();
        }
    }
    private void saveGameState() {
        File save = new File("savefile.txt");
        try {
            if (save.exists()) {
                save.delete();
            }
            save.createNewFile();
            FileOutputStream fOS = new FileOutputStream(save);
            ObjectOutputStream oOS = new ObjectOutputStream(fOS);
            oOS.writeObject(rand);
            oOS.writeObject(p);
            oOS.writeObject(ter);
            oOS.writeObject(treasure);
            oOS.writeObject(seed);
            oOS.writeObject(ec);
            oOS.writeObject(inEncounter);
            //oOS.writeObject(ep);
            oOS.close();
        } catch (FileNotFoundException e) {
            System.out.println("No saved data available");
            System.exit(0);
        } catch (IOException e) {
            System.exit(0);
        }
    }


    public boolean isFound() {
        if (treasure.getPoint().equals(p.getPoint())) {
            return true;
        }
        return false;
    }


    //move mechanics
    public boolean isTileFloor(int x, int y) {
        return world[x][y] == floorTile;
    }
    public boolean isTileEncounter(int x, int y) {
        return world[x][y] == Tileset.chasmTile;
    }
    public void moveAvatar(int horz, int vert) {
        int playerX, playerY;
        if (inEncounter) {
            playerX = ep.getX();
            playerY = ep.getY();
            if (world[playerX + horz][playerY + vert] == Tileset.chasmTile) {
                inEncounter = false;
                setGhost();
                avatarTile = Tileset.ghostAvatar;
                world = ec.endGame();
                ter.initialize(WIDTH, HEIGHT);
                ter.renderFrame(world);
            } else if (playerX + horz == 69) {
                inEncounter = false;
                world = ec.winEncounter();
                ter.initialize(WIDTH, HEIGHT);
                ter.renderFrame(world);
            } else if (world[playerX + horz][playerY + vert] == wallTile) {
                return;
            } else {
                world[playerX][playerY] = floorTile;
                playerX += horz;
                playerY += vert;
                ep.setPoint(playerX, playerY);
                world[playerX][playerY] = avatarTile;
                ter.renderFrame(world);
            }
        } else {
            playerX = p.getX();
            playerY = p.getY();
            if (isTileFloor(playerX + horz, playerY + vert)) {
                p.setPoint(playerX + horz, playerY + vert);
                world[playerX][playerY] = floorTile;
                world[playerX + horz][playerY + vert] = avatarTile;
                ter.renderFrame(world);
            }
            if (isTileEncounter(playerX + horz, playerY + vert)) {
                world[playerX + horz][playerY + vert] = floorTile;
                changeGameStatus("Encounter");
                Encounter.startEncounterScreen();
            }
            if (playerX + horz == treasure.getX() && playerY + vert == treasure.getY() && !isGhost) {
                changeGameStatus("Return");
            }
        }
    }
    public TETile getWallTile() {
        return wallTile;
    }
    public TETile getFloorTile() {
        return floorTile;
    }

    public TETile getAvatarTile() {
        return avatarTile;
    }
    public void setInEncounter(boolean status) {
        inEncounter = status;
    }


    public boolean notWall(TETile t) {
        return t.equals(Tileset.FLOOR);

    }

    public void setTreasure() {
        treasure = new Treasure();
        int x = rand.nextInt(WIDTH);
        int y= rand.nextInt(HEIGHT);
        while (world[x][y] != floorTile && world[x][y] != Tileset.chasmTile) {
            x = rand.nextInt(WIDTH);
            y= rand.nextInt(HEIGHT);
        }
        treasure.setPoint(x,y);
        world[x][y] = Tileset.chest;
        //Let's place our treasure somewhere in the world!
    }

    /* public void setPlayer() {
        while (true && !isFound()) {
            int x = rand.nextInt(WIDTH);
            int y = rand.nextInt(HEIGHT);
            if (world[x][y] == Tileset.FLOOR) {
                setTile(x, y, world, Tileset.AVATAR);
                p.setPoint(x,y);
            }
        }
    }
    */
    public void setPlayer(Player p) {
        this.p = p;
    }
    public void setWorld(TETile[][] w) {
        world = w;
    }
    public void changeGameStatus(String newStatus) {
        gameStatus = newStatus;
    }

    public void changeAvatar(TETile a) {
        avatarTile = a;
    }

    public void setGhost() {
        isGhost = true;
    }

}
