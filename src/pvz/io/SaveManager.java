package pvz.io;

import pvz.model.GameState;

import java.io.*;

/**
 * Handles serialisation/deserialisation of {@link GameState} to disk.
 */
public class SaveManager {

    private static final String SAVE_FILE = "pvz_save.dat";

    /** Saves the given game state to disk. */
    public static void save(GameState state) throws IOException {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(state);
        }
    }

    /**
     * Loads a previously saved game state.
     * @throws IOException            if the file cannot be read.
     * @throws ClassNotFoundException if the class is not on the classpath.
     */
    public static GameState load() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            return (GameState) ois.readObject();
        }
    }

    /** Returns true when a save file exists. */
    public static boolean hasSave() {
        return new File(SAVE_FILE).exists();
    }

    /** Deletes the save file (e.g. after a victory or new game). */
    public static void deleteSave() {
        File f = new File(SAVE_FILE);
        if (f.exists()) f.delete();
    }
}
