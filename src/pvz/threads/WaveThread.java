package pvz.threads;

import pvz.GameConstants;
import pvz.model.GameState;
import pvz.model.zombies.*;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Spawns a set of zombies for a single wave, then signals completion.
 *
 * Zorluk artışı (isterler):
 *   - Her dalga ile zombi HP'si artar  (hpMult)
 *   - Her dalga ile doğma sıklığı artar (spawnIntervalMs azalır)
 *
 * Sadece üretim yapar – hareket (movement) yapmaz.
 * Dalga bitince sonlanır; yeni dalga için yeniden başlatılır.
 */
public class WaveThread extends Thread {

    // ---- Dalga parametreleri -------------------------------------------
    // Dalga 1 : 7 zombi | HP x1.0 | 3 sn arayla | Basic + Fast
    // Dalga 2 : 12 zombi | HP x1.6 | 1.8 sn arayla | Basic + Fast + Run + Tank
    private static final int   BASE_ZOMBIES       = 8;
    private static final int   EXTRA_PER_WAVE     = 5;   // dalga başına ek zombi
    private static final float HP_MULT_PER_WAVE   = 0.6f; // dalga başına HP artışı
    private static final int   BASE_INTERVAL_MS   = 3000; // Dalga 1 spawn aralığı
    private static final int   INTERVAL_REDUCE_MS = 1200; // Her dalga hızlanma
    private static final int   MIN_INTERVAL_MS    = 800;  // Alt sınır

    // --------------------------------------------------------------------
    private final GameState state;
    private final ConcurrentLinkedQueue<Zombie> zombieQueue;
    private final int   waveNumber;
    private final float hpMult;
    private final int   spawnIntervalMs;
    private final int   totalZombies;
    private volatile boolean running;

    public WaveThread(GameState state,
                      ConcurrentLinkedQueue<Zombie> zombieQueue,
                      int waveNumber) {
        this.state       = state;
        this.zombieQueue = zombieQueue;
        this.waveNumber  = waveNumber;

        // Zorluk hesabı
        this.hpMult          = 1.0f + (waveNumber - 1) * HP_MULT_PER_WAVE;
        this.spawnIntervalMs = Math.max(MIN_INTERVAL_MS,
                BASE_INTERVAL_MS - (waveNumber - 1) * INTERVAL_REDUCE_MS);
        this.totalZombies    = BASE_ZOMBIES + (waveNumber - 1) * EXTRA_PER_WAVE;

        this.running = true;
        setDaemon(true);
        setName("WaveThread-" + waveNumber);
    }

    @Override
    public void run() {
        Random rng    = new Random();
        float  startX = GameConstants.COLS * GameConstants.CELL_WIDTH + 40f;

        for (int i = 0; i < totalZombies && running; i++) {
            int row = rng.nextInt(GameConstants.ROWS);
            zombieQueue.offer(createZombie(row, startX, rng));
            try {
                Thread.sleep(spawnIntervalMs);
            } catch (InterruptedException e) {
                break;
            }
        }
        // Tüm zombiler üretildi → dalga bitti
        state.waveActive = false;
    }

    /**
     * Dalga 1 → yalnızca Basic + Fast (isterler: kolay başlangıç)
     * Dalga 2 → tüm türler (Basic / Fast / Run / Tank)
     */
    private Zombie createZombie(int row, float startX, Random rng) {
        if (waveNumber == 1) {
            // Dalga 1: sadece temel zombiler
            return rng.nextBoolean()
                    ? new BasicZombie(row, startX, hpMult)
                    : new FastZombie (row, startX, hpMult);
        }
        // Dalga 2+: tüm türler
        switch (rng.nextInt(4)) {
            case 0:  return new BasicZombie(row, startX, hpMult);
            case 1:  return new FastZombie (row, startX, hpMult);
            case 2:  return new RunZombie  (row, startX, hpMult);
            default: return new TankZombie (row, startX, hpMult);
        }
    }

    public void stopWave() {
        running = false;
        interrupt();
    }
}
