package pvz.model.zombies;

import java.awt.Color;

/** Medium HP, medium speed. */
public class FastZombie extends Zombie {
    private static final long serialVersionUID = 1L;

    public FastZombie(int row, float startX) {
        this(row, startX, 1.0f);
    }

    public FastZombie(int row, float startX, float hpMult) {
        // speed=1.5
        super(200 * hpMult, 1.5f, row, startX, 60, 100);
    }

    @Override public Color  getColor() { return new Color(150, 210, 150); }
    @Override public String getName()  { return "Fast"; }
}
