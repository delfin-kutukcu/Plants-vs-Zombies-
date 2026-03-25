package pvz.model;

import pvz.GameConstants;
import pvz.model.plants.Plant;
import pvz.model.zombies.Zombie;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the complete, serializable state of an ongoing game.
 * All mutable collections must only be accessed from the EDT
 * (game-loop timer) except zombies, which is guarded separately
 * via ConcurrentLinkedQueue in GamePanel.
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    // ---- grid -------------------------------------------------------
    public Plant[][] grid;          // [row][col]

    // ---- live objects -----------------------------------------------
    public List<Zombie>     zombies;
    public List<Projectile> projectiles;
    public List<Sun>        suns;

    // ---- resources --------------------------------------------------
    public int sunAmount;

    // ---- wave tracking ----------------------------------------------
    /** 0 = no wave started yet; 1 = first wave; etc. */
    public int  currentWave;
    public int  totalWaves;
    /** Zombies spawned via *normal* production (resets each wave cycle). */
    public int  zombiesSpawnedNormally;
    /** True while a WaveThread is still spawning zombies. */
    public boolean waveActive;

    // ---- game-flow flags --------------------------------------------
    public boolean gameOver;
    public boolean victory;

    // ---- timers (in ticks) ------------------------------------------
    public int normalSpawnTimer;   // ticks until next normal zombie

    // ---- score ------------------------------------------------------
    public int zombiesKilled;

    // -----------------------------------------------------------------
    public GameState() {
        grid        = new Plant[GameConstants.ROWS][GameConstants.COLS];
        zombies     = new ArrayList<>();
        projectiles = new ArrayList<>();
        suns        = new ArrayList<>();

        sunAmount              = GameConstants.INITIAL_SUN;
        currentWave            = 0;
        totalWaves             = GameConstants.TOTAL_WAVES;
        zombiesSpawnedNormally = 0;
        waveActive             = false;
        gameOver               = false;
        victory                = false;
        normalSpawnTimer       = 1000;  // first zombie after 20 s
        zombiesKilled          = 0;
    }
}
