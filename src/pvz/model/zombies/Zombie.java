package pvz.model.zombies;

import pvz.GameConstants;

import java.awt.Color;
import java.io.Serializable;

/**
 * Abstract base class for every zombie type.
 * Zombies move left (x decreases) and attack the first plant they meet.
 */
public abstract class Zombie implements Serializable {
    private static final long serialVersionUID = 1L;

    protected float hp;
    protected final float maxHp;
    protected final float baseSpeed;
    protected float currentSpeed;

    /** Fixed row (0-4) assigned at spawn. */
    protected final int row;

    /** Pixel x-position (centre of the zombie sprite area). */
    protected float x;

    /** Ticks between attacks. */
    protected final int attackInterval;
    protected int   attackTimer;
    protected final int attackDamage;

    /** True when the zombie is currently blocked by a plant and attacking. */
    protected boolean attacking;

    /** Ticks remaining of the slow effect (0 = not slowed). */
    protected int slowTicks;

    protected Zombie(float hp, float speed, int row, float startX,
                     int attackInterval, int attackDamage) {
        this.hp             = hp;
        this.maxHp          = hp;
        this.baseSpeed      = speed;
        this.currentSpeed   = speed;
        this.row            = row;
        this.x              = startX;
        this.attackInterval = attackInterval;
        this.attackTimer    = attackInterval;
        this.attackDamage   = attackDamage;
        this.attacking      = false;
        this.slowTicks      = 0;
    }

    // ---- health --------------------------------------------------------
    public boolean isAlive()              { return hp > 0; }
    public void    takeDamage(float dmg)  { hp = Math.max(0, hp - dmg); }
    public float   getHp()               { return hp; }
    public float   getMaxHp()            { return maxHp; }
    public float   getHpRatio()          { return hp / maxHp; }

    // ---- position / movement ------------------------------------------
    public int   getRow() { return row; }
    public float getX()   { return x; }
    public void  setX(float x) { this.x = x; }

    /** Move left by currentSpeed pixels (only when not attacking). */
    public void move() {
        if (!attacking) x -= currentSpeed;
    }

    // ---- combat -------------------------------------------------------
    public boolean isAttacking() { return attacking; }
    public void setAttacking(boolean b) {
        this.attacking = b;
        if (!b) attackTimer = attackInterval; // reset timer when freed
    }

    /**
     * Ticks the attack timer.
     * @return true when an attack should land this tick.
     */
    public boolean tickAttack() {
        if (!attacking) return false;
        attackTimer--;
        if (attackTimer <= 0) {
            attackTimer = attackInterval;
            return true;
        }
        return false;
    }

    public int getAttackDamage() { return attackDamage; }

    // ---- slow effect --------------------------------------------------
    public void applySlow() {
        slowTicks     = GameConstants.SLOW_DURATION;
        currentSpeed  = baseSpeed * 0.5f;
    }

    public void tickSlow() {
        if (slowTicks > 0) {
            slowTicks--;
            if (slowTicks == 0) currentSpeed = baseSpeed;
        }
    }

    public boolean isSlowed() { return slowTicks > 0; }

    // ---- abstract -----------------------------------------------------
    public abstract Color  getColor();
    public abstract String getName();
}
