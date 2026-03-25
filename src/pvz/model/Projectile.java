package pvz.model;

import java.awt.Color;
import java.io.Serializable;

public class Projectile implements Serializable {
    private static final long serialVersionUID = 1L;

    private float x;
    private final int row;
    private final float speed;
    private final int damage;
    private final boolean snow;   // SnowPea bullet?
    private boolean active;

    public Projectile(float x, int row, float speed, int damage, boolean snow) {
        this.x      = x;
        this.row    = row;
        this.speed  = speed;
        this.damage = damage;
        this.snow   = snow;
        this.active = true;
    }

    public void move() { x += speed; }

    public float getX()      { return x; }
    public int   getRow()    { return row; }
    public int   getDamage() { return damage; }
    public boolean isSnow()  { return snow; }
    public boolean isActive(){ return active; }
    public void deactivate() { active = false; }

    public Color getColor() {
        return snow ? new Color(100, 200, 255) : new Color(50, 200, 50);
    }
}
