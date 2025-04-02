package byow.Core;

import java.io.Serializable;
import java.util.Random;

public class Point implements Serializable {

    private int x;

    private int y;

    private static final int SEED = 34;

    private static final Random RANDOM = new Random(SEED);

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }
    public int getX() {
        return x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY(){
        return y;
    }
}
