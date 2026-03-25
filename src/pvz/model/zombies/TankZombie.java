package pvz.model.zombies;

import java.awt.Color;

/** High HP, medium-high speed — biraz yavaşlatıldı (2.0 → 1.2). */
public class TankZombie extends Zombie {
    private static final long serialVersionUID = 1L;

    public TankZombie(int row, float startX) {
        this(row, startX, 1.0f);
    }

    public TankZombie(int row, float startX, float hpMult) {
        // HP=600, speed=1.2, saldırı her 50 tick (1 s), hasar=150
        super(600 * hpMult, 1.2f, row, startX, 50, 150);
    }

    @Override public Color  getColor() { return new Color(70, 100, 70); }
    @Override public String getName()  { return "Tank"; }
}
