package pvz.model.plants;

import java.awt.Color;

/**
 * Explodes after a short fuse, killing all zombies in its own cell.
 * The game loop must call {@link #tickFuse()} each tick and remove
 * the plant (plus kill zombies) when it returns {@code true}.
 */
public class CherryBomb extends Plant {
    private static final long serialVersionUID = 1L;

    private static final int FUSE_TICKS = 100; // 2 s

    private int fuseTimer;

    public CherryBomb(int row, int col) {
        // HP çok yüksek — zombiler fuse dolmadan öldüremesin
        super(999_999, 150, row, col, Integer.MAX_VALUE);
        this.fuseTimer = FUSE_TICKS;
    }

    /**
     * Decrements fuse.
     * @return true when the bomb should explode.
     */
    public boolean tickFuse() {
        fuseTimer--;
        return fuseTimer <= 0;
    }

    /** 0.0–1.0 remaining fuse ratio (for drawing). */
    public float getFuseRatio() {
        return (float) fuseTimer / FUSE_TICKS;
    }

    @Override public Color  getColor()  { return new Color(220, 30, 30); }
    @Override public String getSymbol() { return "CB"; }
    @Override public String getName()   { return "CherryBomb"; }
}
