package pvz.model.zombies;

import java.awt.Color;

/** Medium HP, high speed — biraz yavaşlatıldı (2.5 → 1.7). */
public class RunZombie extends Zombie {
    private static final long serialVersionUID = 1L;

    public RunZombie(int row, float startX) {
        this(row, startX, 1.0f);
    }

    public RunZombie(int row, float startX, float hpMult) {
        super(200 * hpMult, 1.7f, row, startX, 60, 100);
    }

    @Override public Color  getColor() { return new Color(200, 230, 200); }
    @Override public String getName()  { return "Run"; }
}
