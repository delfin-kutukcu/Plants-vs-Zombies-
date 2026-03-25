package pvz;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and caches images from the classpath (resources/ folder).
 * Returns {@code null} gracefully when a file is missing so the
 * game falls back to shape-based graphics automatically.
 */
public class ImageLoader {

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    // ---- Plant image filenames -------------------------------------
    public static final String IMG_PEASHOOTER = "peashooter.png";
    public static final String IMG_SUNFLOWER  = "sunflower.png";
    public static final String IMG_WALLNUT    = "wallnut.png";
    public static final String IMG_SNOWPEA    = "snowpea.png";
    public static final String IMG_CHERRYBOMB = "cherrybomb.png";

    // ---- Zombie image filenames ------------------------------------
    public static final String IMG_BASIC_ZOMBIE = "basiczombie.png";
    public static final String IMG_FAST_ZOMBIE  = "fastzombie.png";
    public static final String IMG_RUN_ZOMBIE   = "runzombie.png";
    public static final String IMG_TANK_ZOMBIE  = "tankzombie.png";

    // ---- Other -----------------------------------------------------
    public static final String IMG_SUN    = "sun.png";
    public static final String IMG_SHOVEL = "shovel.png";

    /**
     * Returns the cached image for {@code filename}, loading on first call.
     * @return the image, or {@code null} when the file cannot be found/read.
     */
    public static BufferedImage get(String filename) {
        if (CACHE.containsKey(filename)) return CACHE.get(filename);
        BufferedImage img = null;
        try (InputStream is = ImageLoader.class.getResourceAsStream("/" + filename)) {
            if (is != null) img = ImageIO.read(is);
        } catch (IOException ignored) { }
        CACHE.put(filename, img);
        return img;
    }

    /** Maps a plant class name → image filename. */
    public static String forPlant(String className) {
        switch (className) {
            case "PeaShooter":  return IMG_PEASHOOTER;
            case "SunFlower":   return IMG_SUNFLOWER;
            case "WallNut":     return IMG_WALLNUT;
            case "SnowPea":     return IMG_SNOWPEA;
            case "CherryBomb":  return IMG_CHERRYBOMB;
            default:            return null;
        }
    }

    /** Maps a zombie name (from {@code Zombie.getName()}) → image filename. */
    public static String forZombie(String name) {
        switch (name) {
            case "Basic": return IMG_BASIC_ZOMBIE;
            case "Fast":  return IMG_FAST_ZOMBIE;
            case "Run":   return IMG_RUN_ZOMBIE;
            case "Tank":  return IMG_TANK_ZOMBIE;
            default:      return null;
        }
    }
}
