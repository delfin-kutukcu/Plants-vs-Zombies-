package pvz.model;

import java.io.Serializable;

public class Sun implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Click/draw radius.  The drawn image is (RADIUS*2+10)px wide,
     * but we use 25 px so the whole image is clickable.
     */
    public static final int RADIUS    = 25;
    public static final int SUN_VALUE = 25;

    /** Ticks the sun stays on screen (counted only after it has landed). */
    private static final int LIFETIME = 1000; // 20 s at 20 ms/tick

    private float x, y;
    private final float targetY;
    private boolean falling;
    private int     lifetime;
    private boolean collected;

    public Sun(float x, float startY, float targetY) {
        this.x         = x;
        this.y         = startY;
        this.targetY   = targetY;
        this.falling   = (startY < targetY);
        this.lifetime  = LIFETIME;
        this.collected = false;
    }

    /**
     * Advance one game tick.
     * @return true when the sun should be removed from the list.
     */
    public boolean update() {
        if (collected) return true;
        if (falling) {
            y += 2.0f;                      // fall speed
            if (y >= targetY) {
                y      = targetY;
                falling = false;
            }
        } else {
            lifetime--;
            if (lifetime <= 0) return true;
        }
        return false;
    }

    /** True while the sun is still animating downward. */
    public boolean isFalling() { return falling; }

    /** 0.0–1.0 how much of the on-screen lifetime has elapsed. */
    public float lifetimeRatio() { return (float) lifetime / LIFETIME; }

    /** Rectangle-based hit test that matches the drawn image area. */
    public boolean contains(int mx, int my) {
        float dx = mx - x;
        float dy = my - y;
        return dx * dx + dy * dy <= (float) RADIUS * RADIUS;
    }

    public void    collect()       { collected = true; }
    public boolean isCollected()   { return collected; }
    public float   getX()          { return x; }
    public float   getY()          { return y; }
}
