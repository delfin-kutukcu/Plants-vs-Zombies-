package pvz;

public class GameConstants {
    public static final int ROWS = 5;
    public static final int COLS = 9;
    public static final int CELL_WIDTH  = 90;
    public static final int CELL_HEIGHT = 90;
    public static final int PANEL_HEIGHT = 85;   // top selection panel

    public static final int GRID_WIDTH  = COLS * CELL_WIDTH;   // 810
    public static final int GRID_HEIGHT = ROWS * CELL_HEIGHT;  // 450
    public static final int WINDOW_WIDTH  = GRID_WIDTH;
    public static final int WINDOW_HEIGHT = GRID_HEIGHT + PANEL_HEIGHT;

    public static final int GAME_TICK_MS = 20;   // 50 fps

    // Starting sun
    public static final int INITIAL_SUN = 150;

    // Bullet speed (pixels/tick)
    public static final float BULLET_SPEED = 5.0f;

    // Bullet damage
    public static final int BULLET_DAMAGE = 40;

    // Normal zombie spawn interval (ticks)
    public static final int NORMAL_SPAWN_INTERVAL   = 500; // 10 s  — 1. dalga öncesi
    public static final int NORMAL_SPAWN_INTERVAL_2 = 450; // 9 s  — 2. dalga öncesi

    // Zombies before a wave triggers
    public static final int ZOMBIES_BEFORE_WAVE = 8;

    // Total waves
    public static final int TOTAL_WAVES = 2;

    // Snow-pea slow duration (ticks)
    public static final int SLOW_DURATION = 150; // 3 s
}
