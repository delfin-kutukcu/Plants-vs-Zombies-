package pvz.model.zombies;

import java.awt.Color;

/** Medium HP, low speed. */
public class BasicZombie extends Zombie {
    private static final long serialVersionUID = 1L;

    public BasicZombie(int row, float startX) {
        this(row, startX, 1.0f);
    }

    public BasicZombie(int row, float startX, float hpMult) {
        // speed=0.5, attack every 60 ticks (1.2 s), damage=100
        super(200 * hpMult, 0.5f, row, startX, 60, 100);
    }

    @Override public Color  getColor() { return new Color(100, 160, 100); }
    @Override public String getName()  { return "Basic"; }
}
