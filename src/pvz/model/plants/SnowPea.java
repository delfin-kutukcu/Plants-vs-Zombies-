package pvz.model.plants;

import java.awt.Color;

/** Fires a freezing bullet that slows zombies. */
public class SnowPea extends Plant {
    private static final long serialVersionUID = 1L;

    public SnowPea(int row, int col) {
        // HP=300, cost=175, shoots every 75 ticks
        super(300, 175, row, col, 75);
    }

    @Override public Color  getColor()  { return new Color(100, 200, 240); }
    @Override public String getSymbol() { return "SP"; }
    @Override public String getName()   { return "SnowPea"; }
    @Override public boolean canShoot() { return true; }
}
