package pvz.model.plants;

import java.awt.Color;
import java.io.Serializable;

/**
 * Abstract base class for every plant type.
 */
public abstract class Plant implements Serializable {
    private static final long serialVersionUID = 1L;

    protected int hp;
    protected final int maxHp;
    protected final int cost;
    protected final int row;
    protected final int col;

    /** Counts down; fires an action when it hits 0, then resets. */
    protected int actionTimer;
    protected final int actionInterval;

    protected Plant(int hp, int cost, int row, int col, int actionInterval) {
        this.hp             = hp;
        this.maxHp          = hp;
        this.cost           = cost;
        this.row            = row;
        this.col            = col;
        this.actionInterval = actionInterval;
        this.actionTimer    = actionInterval;
    }

    // ---- health --------------------------------------------------------
    public boolean isAlive()              { return hp > 0; }
    public void    takeDamage(int damage) { hp = Math.max(0, hp - damage); }
    public int     getHp()               { return hp; }
    public int     getMaxHp()            { return maxHp; }
    public float   getHpRatio()          { return (float) hp / maxHp; }

    // ---- info ----------------------------------------------------------
    public int     getCost() { return cost; }
    public int     getRow()  { return row; }
    public int     getCol()  { return col; }

    /**
     * Advances the action timer by one tick.
     * @return true when the timer fires (action should occur).
     */
    public boolean tickAction() {
        actionTimer--;
        if (actionTimer <= 0) {
            actionTimer = actionInterval;
            return true;
        }
        return false;
    }

    // ---- abstract ------------------------------------------------------
    public abstract Color  getColor();
    public abstract String getSymbol();
    public abstract String getName();

    /** Override to true for plants that fire projectiles. */
    public boolean canShoot() { return false; }
}
