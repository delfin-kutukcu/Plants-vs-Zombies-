package pvz.model.plants;

import java.awt.Color;

/** Fires a pea bullet every ~1.5 s when zombies are in its lane. */
public class PeaShooter extends Plant {
    private static final long serialVersionUID = 1L;

    public PeaShooter(int row, int col) {
        // HP=300, cost=100, shoots every 75 ticks (75*20ms = 1.5 s)
        super(300, 100, row, col, 75);
    }

    @Override public Color  getColor()  { return new Color(34, 139, 34); }
    @Override public String getSymbol() { return "PS"; }
    @Override public String getName()   { return "PeaShooter"; }
    @Override public boolean canShoot() { return true; }
}
