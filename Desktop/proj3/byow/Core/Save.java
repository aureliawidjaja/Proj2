package byow.Core;

import java.io.*;

public class Save {

    private static void saveGameState(Engine en) {
        File save = new File("savefile.txt");
        try {
            if (save.exists()) {
                save.delete();
            }
            save.createNewFile();
            FileOutputStream fOS = new FileOutputStream(save);
            ObjectOutputStream oOS = new ObjectOutputStream(fOS);
            oOS.writeObject(en);
            oOS.close();
        } catch (FileNotFoundException e) {
            System.out.println("No saved data available");
            System.exit(0);
        } catch (IOException e) {
            System.exit(0);
        }
    }

    public static Engine loadGamestate() {
        File file = new File("savefile.txt");
        if (file.exists()) {
            try {
                FileInputStream fIS = new FileInputStream(file);
                ObjectInputStream oIS = new ObjectInputStream(fIS);
                Engine en = (Engine) oIS.readObject();
                return en;
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}

