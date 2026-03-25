package pvz.model.plants;

import java.awt.Color;

/** High-HP barrier; stops zombies but does not attack. */
public class WallNut extends Plant {
    private static final long serialVersionUID = 1L;

    public WallNut(int row, int col) {
        // HP=4000, cost=50, no action (very long interval)
        super(4000, 50, row, col, Integer.MAX_VALUE);
    }

    @Override public Color  getColor()  { return new Color(160, 100, 40); }
    @Override public String getSymbol() { return "WN"; }
    @Override public String getName()   { return "WallNut"; }
}
