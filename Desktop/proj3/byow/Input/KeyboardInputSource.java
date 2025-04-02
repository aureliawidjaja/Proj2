package byow.Input;
import edu.princeton.cs.algs4.StdDraw;

public class KeyboardInputSource {

    public char getNextKey() {
        return Character.toUpperCase(StdDraw.nextKeyTyped());
    }

    public boolean possibleNextInput() {
        return StdDraw.hasNextKeyTyped();
    }

}
