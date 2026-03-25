package pvz.model.plants;

import java.awt.Color;

/**
 * Produces a sun token every ~13 s (biraz yavaşlatıldı).
 * First sun arrives in ~6.5 s.
 * HP artırıldı: 300 (zombilere dayanıklı).
 */
public class SunFlower extends Plant {
    private static final long serialVersionUID = 1L;

    public SunFlower(int row, int col) {
        // HP=300, cost=50, üretim her 550 tick (11 s)
        super(300, 50, row, col, 550); 
        // İlk güneş ~6.5 sn sonra
        this.actionTimer = actionInterval / 2;
    }

    @Override public Color  getColor()  { return new Color(255, 200, 0); }
    @Override public String getSymbol() { return "SF"; }
    @Override public String getName()   { return "SunFlower"; }
}
