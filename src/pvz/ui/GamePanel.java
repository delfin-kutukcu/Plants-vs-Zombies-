package pvz.ui;

import pvz.GameConstants;
import pvz.ImageLoader;
import pvz.io.SaveManager;
import pvz.model.GameState;
import pvz.model.Projectile;
import pvz.model.Sun;
import pvz.model.plants.*;
import pvz.model.zombies.*;
import pvz.threads.WaveThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Main game panel: renders everything and drives the game-loop timer.
 */
public class GamePanel extends JPanel {

    // ---- layout constants ------------------------------------------
    private static final int ROWS   = GameConstants.ROWS;
    private static final int COLS   = GameConstants.COLS;
    private static final int CW     = GameConstants.CELL_WIDTH;
    private static final int CH     = GameConstants.CELL_HEIGHT;
    private static final int PH     = GameConstants.PANEL_HEIGHT; // top bar
    private static final int GX     = 0;   // grid left edge (x)
    private static final int GY     = PH;  // grid top edge (y)

    // ---- plant card layout (inside the top bar) --------------------
    private static final int CARD_W    = 82;
    private static final int CARD_H    = 72;
    private static final int CARD_YOFF = 6;
    private static final String[] PLANT_TYPES = {
            "PeaShooter", "SunFlower", "WallNut", "SnowPea", "CherryBomb"
    };
    private static final int[] PLANT_COSTS = {100, 50, 50, 175, 150};
    private static final Color[] PLANT_COLORS = {
            new Color(34, 139, 34),
            new Color(255, 200, 0),
            new Color(160, 100, 40),
            new Color(100, 200, 240),
            new Color(220, 30, 30)
    };

    // ---- state / game loop -----------------------------------------
    private GameState state;
    private final JFrame owner;
    private javax.swing.Timer gameTimer;
    private WaveThread waveThread;
    private final ConcurrentLinkedQueue<Zombie> zombieQueue = new ConcurrentLinkedQueue<>();
    private final Random rng = new Random();

    private boolean paused = false;
    private String  selectedPlant  = null;  // null = none selected
    private boolean shovelSelected = false; // kürek modu aktif mi?

    // ---- Kürek butonu hit-alanı ------------------------------------
    private Rectangle shovelBtn;

    // ---- Dalga uyarı mesajı ----------------------------------------
    /** Mesaj kaç tick daha ekranda kalacak (0 = gösterme). */
    private int waveWarningTicks = 0;
    private static final int WAVE_WARNING_DURATION = 150; // 3 sn

    // ---- Pause / Buttons -------------------------------------------
    private Rectangle pauseBtn;
    private Rectangle saveBtn;
    private Rectangle menuBtn;

    // ---- Constructor -----------------------------------------------
    public GamePanel(GameState state, JFrame owner) {
        this.state = state;
        this.owner = owner;

        setPreferredSize(new Dimension(GameConstants.WINDOW_WIDTH,
                                       GameConstants.WINDOW_HEIGHT));
        setBackground(new Color(50, 120, 50));
        setFocusable(true);

        // Define button hit-areas
        pauseBtn  = new Rectangle(COLS * CW - 240, 14, 75, 55);
        saveBtn   = new Rectangle(COLS * CW - 160, 14, 75, 55);
        menuBtn   = new Rectangle(COLS * CW -  80, 14, 75, 55);
        // Kürek butonu: güneş sayacının hemen sağında
        int sunAreaX = 2 + PLANT_TYPES.length * (CARD_W + 2) + 8;
        shovelBtn = new Rectangle(sunAreaX + 75, 10, 58, 62);

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { handleClick(e.getX(), e.getY()); }
        });

        // Keyboard: P = pause/resume
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_P) togglePause();
            }
        });
    }

    // ---- Game loop control -----------------------------------------
    public void startGame() {
        gameTimer = new Timer(GameConstants.GAME_TICK_MS, e -> gameTick());
        gameTimer.start();
        requestFocusInWindow();
    }

    public void stopGame() {
        if (gameTimer != null) gameTimer.stop();
        stopWaveThread();
    }

    private void stopWaveThread() {
        if (waveThread != null && waveThread.isAlive()) {
            waveThread.stopWave();
        }
    }

    // ---- Game tick (runs on EDT) -----------------------------------
    private void gameTick() {
        if (paused || state.gameOver || state.victory) {
            repaint();
            return;
        }

        // 1. Drain zombie queue (produced by WaveThread)
        Zombie z;
        while ((z = zombieQueue.poll()) != null) {
            state.zombies.add(z);
        }

        // 2. Normal zombie spawning
        tickNormalSpawn();

        // 3. Update plants
        updatePlants();

        // 4. Update zombies
        updateZombies();

        // 5. Update projectiles
        updateProjectiles();

        // 6. Update suns
        updateSuns();

        // 7. Dalga uyarı sayacını düşür
        if (waveWarningTicks > 0) waveWarningTicks--;

        // 8. Check end conditions
        checkEndConditions();

        repaint();
    }

    // ----------------------------------------------------------------
    private void tickNormalSpawn() {
        if (state.waveActive || state.currentWave >= state.totalWaves) return;

        state.normalSpawnTimer--;
        if (state.normalSpawnTimer <= 0) {
            // 1. dalga öncesi: 15 sn | 2. dalga öncesi: 10 sn
            int interval = (state.currentWave == 0)
                    ? GameConstants.NORMAL_SPAWN_INTERVAL
                    : GameConstants.NORMAL_SPAWN_INTERVAL_2;
            state.normalSpawnTimer = interval;
            spawnNormalZombie();
            state.zombiesSpawnedNormally++;
            if (state.zombiesSpawnedNormally % GameConstants.ZOMBIES_BEFORE_WAVE == 0) {
                startNextWave();
            }
        }
    }

    private void spawnNormalZombie() {
        int row    = rng.nextInt(ROWS);
        float startX = GX + COLS * CW + 40f;
        // Normal production: only Basic and Fast
        Zombie z = (rng.nextBoolean())
                ? new BasicZombie(row, startX)
                : new FastZombie(row, startX);
        zombieQueue.offer(z);
    }

    private void startNextWave() {
        stopWaveThread();
        state.currentWave++;
        state.waveActive = true;
        waveWarningTicks = WAVE_WARNING_DURATION; // uyarı mesajını başlat
        waveThread = new WaveThread(state, zombieQueue, state.currentWave);
        waveThread.start();
    }

    // ----------------------------------------------------------------
    private void updatePlants() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Plant p = state.grid[row][col];
                if (p == null) continue;

                // CherryBomb: fuse kontrolü isAlive'dan ÖNCE yapılmalı.
                // HP=1 olduğundan zombi saldırısı patlama olmadan siler.
                if (p instanceof CherryBomb) {
                    CherryBomb cb = (CherryBomb) p;
                    if (cb.tickFuse()) {
                        explodeCherryBomb(row, col);
                    }
                    continue; // isAlive kontrolü atlanır
                }

                if (!p.isAlive()) { state.grid[row][col] = null; continue; }

                if (p instanceof SunFlower) {
                    if (p.tickAction()) produceSun(row, col);
                } else if (p.canShoot()) {
                    if (zombieInRow(row, col) && p.tickAction()) {
                        spawnBullet(row, col, p instanceof SnowPea);
                    }
                }
            }
        }
    }

    private boolean zombieInRow(int row, int shooterCol) {
        float minX = GX + shooterCol * CW;
        for (Zombie z : state.zombies) {
            if (z.isAlive() && z.getRow() == row && z.getX() > minX) return true;
        }
        return false;
    }

    private void produceSun(int row, int col) {
        // Güneş, SunFlower'ın kendi hücresinde üretilir.
        // Hücrenin üst kenarından hücre ortasına düşer.
        // Aynı hücrede birden fazla güneş görünür olsun diye küçük x ofseti.
        float baseX   = GX + col * CW + CW / 2f;
        float offsetX = (float)(Math.random() * 30 - 15); // -15 ile +15 px arası
        float cx      = baseX + offsetX;
        float cellTop = GY + row * CH + 8f;
        float cellMid = GY + row * CH + CH / 2f;
        state.suns.add(new Sun(cx, cellTop, cellMid));
    }

    private void spawnBullet(int row, int col, boolean snow) {
        float bx = GX + (col + 1) * CW - 5;
        state.projectiles.add(new Projectile(bx, row,
                GameConstants.BULLET_SPEED, GameConstants.BULLET_DAMAGE, snow));
    }

    private void explodeCherryBomb(int row, int col) {
        state.grid[row][col] = null;

        // ---- Patlama alanı: sadece kendi hücresi ---------------
        float leftEdge  = GX + col * CW;
        float rightEdge = GX + (col + 1) * CW;

        List<Zombie> toRemove = new ArrayList<>();
        for (Zombie z : state.zombies) {
            if (z.getRow() == row
                    && z.getX() >= leftEdge && z.getX() <= rightEdge) {
                toRemove.add(z);
            }
        }
        for (Zombie z : toRemove) {
            z.takeDamage(Float.MAX_VALUE); // kim olursa olsun anlık öldürme
            state.zombiesKilled++;
        }
        state.zombies.removeAll(toRemove);
    }

    // ----------------------------------------------------------------
    private void updateZombies() {
        Iterator<Zombie> it = state.zombies.iterator();
        while (it.hasNext()) {
            Zombie z = it.next();

            if (!z.isAlive()) {
                it.remove();
                state.zombiesKilled++;
                continue;
            }

            z.tickSlow();

            float zx = z.getX();
            // Has zombie crossed the left boundary?
            if (zx < GX) {
                state.gameOver = true;
                return;
            }

            int col = (int)(zx / CW);
            if (col < COLS) {
                Plant p = state.grid[z.getRow()][col];
                if (p != null) {
                    z.setAttacking(true);
                    if (z.tickAttack()) {
                        // CherryBomb'a hasar ver ama ölse bile gridden silme
                        // — fuse dolunca kendi kendine patlayacak
                        if (!(p instanceof CherryBomb)) {
                            p.takeDamage(z.getAttackDamage());
                            if (!p.isAlive()) {
                                state.grid[z.getRow()][col] = null;
                                z.setAttacking(false);
                            }
                        }
                    }
                    continue;
                }
            }
            z.setAttacking(false);
            z.move();
        }
    }

    // ----------------------------------------------------------------
    private void updateProjectiles() {
        Iterator<Projectile> it = state.projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.move();

            // Off right edge
            if (p.getX() > GX + COLS * CW) { it.remove(); continue; }

            // Collision check
            boolean hit = false;
            for (Zombie z : state.zombies) {
                if (!z.isAlive()) continue;
                if (z.getRow() == p.getRow() &&
                        Math.abs(z.getX() - p.getX()) < 38) {
                    z.takeDamage(p.getDamage());
                    if (p.isSnow()) z.applySlow();
                    hit = true;
                    break;
                }
            }
            if (hit) it.remove();
        }
    }

    // ----------------------------------------------------------------
    private void updateSuns() {
        state.suns.removeIf(Sun::update);
    }

    // ----------------------------------------------------------------
    private void checkEndConditions() {
        if (state.gameOver) return;

        // Victory: last wave finished spawning AND all zombies dead
        if (state.currentWave >= state.totalWaves
                && !state.waveActive
                && state.zombies.isEmpty()) {
            // Also: normal-spawning is exhausted for last wave cycle
            // (no zombies left at all)
            state.victory = true;
        }
    }

    // ---- Mouse handling --------------------------------------------
    private void handleClick(int mx, int my) {
        if (state.gameOver || state.victory) return;

        // Buttons in top bar
        if (pauseBtn.contains(mx, my)) { togglePause(); return; }
        if (saveBtn.contains(mx, my))  { saveGame();    return; }
        if (menuBtn.contains(mx, my))  { goToMenu();    return; }

        // ---- Kürek butonu --------------------------------------------
        if (shovelBtn.contains(mx, my)) {
            shovelSelected = !shovelSelected;
            if (shovelSelected) selectedPlant = null; // kürek seçilince bitki seçimi iptal
            setCursor(shovelSelected
                    ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
                    : Cursor.getDefaultCursor());
            repaint();
            return;
        }

        // ---- Top panel: bitki kartı seçimi ---------------------------
        if (my < PH) {
            int idx = (mx - 2) / (CARD_W + 2);
            if (idx >= 0 && idx < PLANT_TYPES.length) {
                String type = PLANT_TYPES[idx];
                selectedPlant = selectedPlant != null && selectedPlant.equals(type)
                        ? null : type;
                if (selectedPlant != null) shovelSelected = false; // bitki seçilince kürek iptal
                setCursor(selectedPlant != null
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
            }
            return;
        }

        // ---- Grid tıklaması: önce güneş toplanır --------------------
        boolean collectedAny = false;
        for (int i = state.suns.size() - 1; i >= 0; i--) {
            Sun sun = state.suns.get(i);
            if (!sun.isCollected() && sun.contains(mx, my)) {
                state.suns.remove(i);
                state.sunAmount += Sun.SUN_VALUE;
                collectedAny = true;
            }
        }
        if (collectedAny) {
            repaint();
            return;
        }

        if (paused) return;

        int row = (my - GY) / CH;
        int col = mx / CW;
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return;

        // ---- Kürek modu: tıklanan hücredeki bitkiyi sil -------------
        if (shovelSelected) {
            if (state.grid[row][col] != null) {
                state.grid[row][col] = null;
            }
            // Kürek tek kullanımlık değil: seçili kalmaya devam eder
            // (orijinal oyun davranışı)
            repaint();
            return;
        }

        // ---- Bitki yerleştirme --------------------------------------
        if (selectedPlant == null) return;
        if (state.grid[row][col] != null) return;

        int cost = costOf(selectedPlant);
        if (state.sunAmount < cost) return;

        state.grid[row][col] = createPlant(selectedPlant, row, col);
        state.sunAmount -= cost;
        selectedPlant = null;
        setCursor(Cursor.getDefaultCursor());
    }

    private int costOf(String type) {
        for (int i = 0; i < PLANT_TYPES.length; i++)
            if (PLANT_TYPES[i].equals(type)) return PLANT_COSTS[i];
        return 0;
    }

    private Plant createPlant(String type, int row, int col) {
        switch (type) {
            case "PeaShooter": return new PeaShooter(row, col);
            case "SunFlower":  return new SunFlower(row, col);
            case "WallNut":    return new WallNut(row, col);
            case "SnowPea":    return new SnowPea(row, col);
            case "CherryBomb": return new CherryBomb(row, col);
            default:           return new PeaShooter(row, col);
        }
    }

    // ---- Pause / Save / Menu ---------------------------------------
    private void togglePause() {
        paused = !paused;
    }

    private void saveGame() {
        if (!paused) togglePause();

        // WaveThread'i durdur — yeni zombi eklemesin
        stopWaveThread();
        state.waveActive = false;

        // zombieQueue'daki henüz işlenmemiş zombileri state'e taşı
        // (bu zombiler kayıt sırasında kaybedilmesin)
        Zombie qz;
        while ((qz = zombieQueue.poll()) != null) {
            state.zombies.add(qz);
        }

        try {
            SaveManager.save(state);
            JOptionPane.showMessageDialog(this,
                    "Oyun kaydedildi.", "Kaydet", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Kaydetme hatası: " + ex.getMessage(),
                    "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void goToMenu() {
        int ans = JOptionPane.showConfirmDialog(this,
                "Ana menüye dönmek istiyor musunuz?",
                "Menü", JOptionPane.YES_NO_OPTION);
        if (ans == JOptionPane.YES_OPTION) {
            stopGame();
            // Signal the owner frame to show menu
            if (owner instanceof pvz.GameFrame) {
                ((pvz.GameFrame) owner).showMenu();
            }
        }
    }

    // ================================================================
    // ---- Rendering --------------------------------------------------
    // ================================================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawTopPanel(g2);
        drawGrid(g2);
        drawPlants(g2);    // bitkiler önce
        drawZombies(g2);
        drawProjectiles(g2);
        drawSuns(g2);      // güneşler en üstte (her şeyin önünde)
        drawHUD(g2);

        if (waveWarningTicks > 0)                        drawWaveWarning(g2);
        if (paused && !state.gameOver && !state.victory) drawPauseOverlay(g2);
        if (state.gameOver)  drawGameOver(g2);
        if (state.victory)   drawVictory(g2);
    }

    // ---- Top panel -------------------------------------------------
    private void drawTopPanel(Graphics2D g) {
        g.setColor(new Color(30, 60, 30));
        g.fillRect(0, 0, getWidth(), PH);
        g.setColor(new Color(50, 100, 50));
        g.drawLine(0, PH - 1, getWidth(), PH - 1);

        // Plant cards
        for (int i = 0; i < PLANT_TYPES.length; i++) {
            drawPlantCard(g, i);
        }

        // Sun counter
        int sunX = 2 + PLANT_TYPES.length * (CARD_W + 2) + 8;
        BufferedImage sunIcon = ImageLoader.get(ImageLoader.IMG_SUN);
        if (sunIcon != null) {
            g.drawImage(sunIcon, sunX, CARD_YOFF + 4, 36, 36, null);
        } else {
            g.setColor(new Color(255, 220, 0));
            g.fillOval(sunX, CARD_YOFF + 8, 28, 28);
            g.setColor(new Color(180, 140, 0));
            g.drawOval(sunX, CARD_YOFF + 8, 28, 28);
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString(String.valueOf(state.sunAmount), sunX + 40, CARD_YOFF + 27);

        // ---- Kürek butonu -------------------------------------------
        drawShovelButton(g, sunX + 90);

        // Wave info — küreğin altına, okunabilir şekilde
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.setColor(new Color(180, 230, 180));
        String waveInfo = "Dalga " + state.currentWave + "/" + state.totalWaves
                        + "          Kill: " + state.zombiesKilled;
        g.drawString(waveInfo, sunX + 15, CARD_YOFF + 60);

        // Buttons
        drawSmallButton(g, pauseBtn, paused ? "Devam" : "Durdur",
                new Color(200, 150, 30));
        drawSmallButton(g, saveBtn,  "Kaydet",  new Color(60, 130, 200));
        drawSmallButton(g, menuBtn,  "Menu",    new Color(130, 60, 130));
    }

    private void drawPlantCard(Graphics2D g, int idx) {
        int x = 2 + idx * (CARD_W + 2);
        int cost = PLANT_COSTS[idx];
        boolean affordable = state.sunAmount >= cost;
        boolean selected   = PLANT_TYPES[idx].equals(selectedPlant);

        // Card background
        Color bg = affordable ? PLANT_COLORS[idx].darker() : new Color(60, 60, 60);
        g.setColor(bg);
        g.fillRoundRect(x, CARD_YOFF, CARD_W, CARD_H, 8, 8);

        // Border
        g.setColor(selected ? Color.YELLOW : new Color(80, 80, 80));
        g.setStroke(new BasicStroke(selected ? 3 : 1));
        g.drawRoundRect(x, CARD_YOFF, CARD_W, CARD_H, 8, 8);
        g.setStroke(new BasicStroke(1));

        // Plant name
        g.setFont(new Font("Arial", Font.BOLD, 11));
        g.setColor(affordable ? Color.WHITE : Color.GRAY);
        String name = PLANT_TYPES[idx];
        FontMetrics fm = g.getFontMetrics();
        g.drawString(name, x + (CARD_W - fm.stringWidth(name)) / 2, CARD_YOFF + 20);

        // Plant image (or fallback symbol)
        String imgFile = ImageLoader.forPlant(PLANT_TYPES[idx]);
        BufferedImage img = imgFile != null ? ImageLoader.get(imgFile) : null;
        if (img != null) {
            int imgSize = 40;
            int imgX = x + (CARD_W - imgSize) / 2;
            int imgY = CARD_YOFF + 22;
            if (!affordable) {
                // Draw greyed-out image
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            }
            g.drawImage(img, imgX, imgY, imgSize, imgSize, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        } else {
            g.setFont(new Font("Arial", Font.BOLD, 22));
            g.setColor(affordable ? PLANT_COLORS[idx].brighter() : Color.DARK_GRAY);
            String sym = getPlantSymbol(idx);
            fm = g.getFontMetrics();
            g.drawString(sym, x + (CARD_W - fm.stringWidth(sym)) / 2, CARD_YOFF + 47);
        }

        // Cost — küçük güneş görseli + sayı
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.setColor(new Color(255, 220, 0));
        String costNum = String.valueOf(cost);
        fm = g.getFontMetrics();
        int sunIconSize = 14;
        int totalW = sunIconSize + 2 + fm.stringWidth(costNum);
        int costX = x + (CARD_W - totalW) / 2;
        int costY = CARD_YOFF + 64;
        BufferedImage sunIco = ImageLoader.get(ImageLoader.IMG_SUN);
        if (sunIco != null) {
            g.drawImage(sunIco, costX, costY - sunIconSize + 2, sunIconSize, sunIconSize, null);
        } else {
            g.setColor(new Color(255, 220, 0));
            g.fillOval(costX, costY - sunIconSize + 2, sunIconSize, sunIconSize);
        }
        g.setColor(new Color(255, 220, 0));
        g.drawString(costNum, costX + sunIconSize + 2, costY);
    }

    private String getPlantSymbol(int idx) {
        switch (idx) {
            case 0: return "PS";
            case 1: return "SF";
            case 2: return "WN";
            case 3: return "SP";
            case 4: return "CB";
            default: return "?";
        }
    }

    /** Güneş sayacının sağında gösterilen kürek butonu. */
    private void drawShovelButton(Graphics2D g, int x) {
        Rectangle r = shovelBtn;

        // Arka plan: aktifken altın, değilken kahverengi
        Color bg = shovelSelected ? new Color(200, 160, 0) : new Color(100, 70, 30);
        g.setColor(bg.darker());
        g.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        g.setColor(bg);
        g.setStroke(new BasicStroke(shovelSelected ? 3 : 1));
        g.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        g.setStroke(new BasicStroke(1));

        // Aktifken parlak sarı çerçeve
        if (shovelSelected) {
            g.setColor(new Color(255, 240, 0, 180));
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(r.x - 1, r.y - 1, r.width + 2, r.height + 2, 10, 10);
            g.setStroke(new BasicStroke(1));
        }

        // Kürek görseli
        BufferedImage img = ImageLoader.get("shovel.png");
        if (img != null) {
            int pad = 6;
            g.drawImage(img, r.x + pad, r.y + pad, r.width - pad * 2, r.height - pad * 2, null);
        } else {
            // Fallback: "⛏" yazı
            g.setFont(new Font("Arial", Font.BOLD, 26));
            g.setColor(Color.WHITE);
            g.drawString("[]", r.x + 10, r.y + 40);
        }
    }

    private void drawSmallButton(Graphics2D g, Rectangle r, String text, Color c) {
        g.setColor(c.darker());
        g.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
        g.setColor(c);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
        g.setStroke(new BasicStroke(1));
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, r.x + (r.width - fm.stringWidth(text)) / 2,
                     r.y + r.height / 2 + fm.getAscent() / 2 - 2);
    }

    // ---- Grid ------------------------------------------------------
    private void drawGrid(Graphics2D g) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Color cellBg = ((row + col) % 2 == 0)
                        ? new Color(80, 170, 80)
                        : new Color(70, 155, 70);
                g.setColor(cellBg);
                g.fillRect(GX + col * CW, GY + row * CH, CW, CH);
                g.setColor(new Color(50, 110, 50));
                g.drawRect(GX + col * CW, GY + row * CH, CW, CH);
            }
        }
    }

    // ---- Suns ------------------------------------------------------
    private void drawSuns(Graphics2D g) {
        BufferedImage sunImg = ImageLoader.get(ImageLoader.IMG_SUN);

        for (Sun sun : state.suns) {
            if (sun.isCollected()) continue;

            int sx   = (int) sun.getX();
            int sy   = (int) sun.getY();
            int r    = Sun.RADIUS;           // 25 px
            int size = r * 2;                // 50 px

            // Düşerken: tam opak; yerleştikten sonra zaman geçtikçe hafif solar
            float alpha = sun.isFalling() ? 1.0f
                         : 0.65f + sun.lifetimeRatio() * 0.35f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            if (sunImg != null) {
                g.drawImage(sunImg, sx - size / 2, sy - size / 2, size, size, null);
            } else {
                // Fallback: sarı daire
                g.setColor(new Color(255, 225, 0));
                g.fillOval(sx - r, sy - r, size, size);
                g.setColor(new Color(200, 155, 0));
                g.setStroke(new BasicStroke(2));
                g.drawOval(sx - r, sy - r, size, size);
                g.setStroke(new BasicStroke(1));
            }

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            // "+25" etiketi — güneşin hemen altında
            g.setFont(new Font("Arial", Font.BOLD, 12));
            // Gölge
            g.setColor(new Color(0, 0, 0, 140));
            g.drawString("+25", sx - 10, sy + r + 13);
            // Yazı
            g.setColor(new Color(255, 210, 0));
            g.drawString("+25", sx - 11, sy + r + 12);
        }
    }

    // ---- Plants ----------------------------------------------------
    private void drawPlants(Graphics2D g) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Plant p = state.grid[row][col];
                if (p == null) continue;

                int px = GX + col * CW;
                int py = GY + row * CH;

                // Body: try to draw image, fallback to shape
                String imgFile = ImageLoader.forPlant(p.getName());
                BufferedImage img = imgFile != null ? ImageLoader.get(imgFile) : null;
                if (img != null) {
                    g.drawImage(img, px + 4, py + 4, CW - 8, CH - 20, null);
                } else {
                    g.setColor(p.getColor());
                    g.fillOval(px + 10, py + 10, CW - 20, CH - 20);
                    g.setColor(p.getColor().darker());
                    g.setStroke(new BasicStroke(2));
                    g.drawOval(px + 10, py + 10, CW - 20, CH - 20);
                    g.setStroke(new BasicStroke(1));
                    g.setFont(new Font("Arial", Font.BOLD, 14));
                    g.setColor(Color.WHITE);
                    String sym = p.getSymbol();
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(sym, px + (CW - fm.stringWidth(sym)) / 2,
                                 py + CH / 2 + 5);
                }

                // Health bar
                drawHealthBar(g, px + 8, py + CH - 14, CW - 16, 7, p.getHpRatio());

                // CherryBomb fuse indicator
                if (p instanceof CherryBomb) {
                    CherryBomb cb = (CherryBomb) p;
                    g.setColor(new Color(255, 100, 0, 180));
                    int bw = (int)((CW - 16) * cb.getFuseRatio());
                    g.fillRect(px + 8, py + 4, bw, 5);
                }
            }
        }
    }

    // ---- Zombies ---------------------------------------------------
    private static final int ZW = 60; // zombie draw width
    private static final int ZH = 75; // zombie draw height

    private void drawZombies(Graphics2D g) {
        for (Zombie z : state.zombies) {
            if (!z.isAlive()) continue;

            int zx = (int) z.getX();
            int zy = GY + z.getRow() * CH;

            // Try sprite image
            String imgFile = ImageLoader.forZombie(z.getName());
            BufferedImage img = imgFile != null ? ImageLoader.get(imgFile) : null;

            if (img != null) {
                // Slow: draw with blue tint overlay
                if (z.isSlowed()) {
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
                }
                g.drawImage(img, zx - ZW / 2, zy + CH - ZH - 4, ZW, ZH, null);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                // Blue slow tint overlay
                if (z.isSlowed()) {
                    g.setColor(new Color(80, 140, 255, 70));
                    g.fillRect(zx - ZW / 2, zy + CH - ZH - 4, ZW, ZH);
                }
            } else {
                // Fallback: shapes
                int w = 38, h = 56;
                Color base = z.isSlowed() ? new Color(100, 150, 220) : z.getColor();
                g.setColor(base);
                g.fillRoundRect(zx - w / 2, zy + 5, w, h, 8, 8);
                g.setColor(base.darker());
                g.setStroke(new BasicStroke(2));
                g.drawRoundRect(zx - w / 2, zy + 5, w, h, 8, 8);
                g.setStroke(new BasicStroke(1));
                g.setColor(base.brighter());
                g.fillOval(zx - 14, zy - 8, 28, 28);
                g.setColor(base.darker());
                g.drawOval(zx - 14, zy - 8, 28, 28);
                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.setColor(Color.WHITE);
                FontMetrics fm = g.getFontMetrics();
                String name = z.getName();
                g.drawString(name, zx - fm.stringWidth(name) / 2, zy + 38);
            }

            // Attack indicator (red pulse)
            if (z.isAttacking()) {
                g.setColor(new Color(255, 50, 50, 160));
                g.fillOval(zx - 6, zy + CH - 12, 12, 12);
            }

            // Health bar (below sprite)
            drawHealthBar(g, zx - ZW / 2, zy + CH - 4, ZW, 5, z.getHpRatio());
        }
    }

    // ---- Projectiles -----------------------------------------------
    private void drawProjectiles(Graphics2D g) {
        for (Projectile p : state.projectiles) {
            if (!p.isActive()) continue;
            int px = (int) p.getX();
            int py = GY + p.getRow() * CH + CH / 2;
            g.setColor(p.getColor());
            g.fillOval(px - 7, py - 7, 14, 14);
            g.setColor(p.getColor().darker());
            g.drawOval(px - 7, py - 7, 14, 14);
        }
    }

    // ---- HUD (wave / progress bar) ---------------------------------
    private void drawHUD(Graphics2D g) {
        // Danger line (leftmost column)
        g.setColor(new Color(255, 0, 0, 60));
        g.fillRect(GX, GY, CW, ROWS * CH);

        // Right-edge spawn zone
        g.setColor(new Color(80, 80, 80, 40));
        g.fillRect(GX + (COLS - 1) * CW, GY, CW, ROWS * CH);
    }

    // ---- Shared helpers --------------------------------------------
    private void drawHealthBar(Graphics2D g, int x, int y, int w, int h, float ratio) {
        g.setColor(new Color(80, 0, 0));
        g.fillRect(x, y, w, h);
        Color barColor = ratio > 0.5f ? new Color(50, 200, 50)
                       : ratio > 0.25f ? new Color(230, 180, 0)
                       : new Color(220, 30, 30);
        g.setColor(barColor);
        g.fillRect(x, y, (int)(w * ratio), h);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, w, h);
    }

    // ---- Overlays --------------------------------------------------

    /** Dalga başladığında gösterilen uyarı bandı. */
    private void drawWaveWarning(Graphics2D g) {
        // Opaklık: ilk tick'te tam görünür, zamanla solar
        float ratio = (float) waveWarningTicks / WAVE_WARNING_DURATION;
        int alpha   = (int)(Math.min(1.0f, ratio * 2.5f) * 200); // hızlıca belirir, yavaş solar

        // Koyu kırmızı yatay bant
        int bandH = 70;
        int bandY = getHeight() / 2 - bandH / 2;
        g.setColor(new Color(120, 0, 0, alpha));
        g.fillRect(0, bandY, getWidth(), bandH);
        g.setColor(new Color(200, 0, 0, Math.min(255, alpha + 40)));
        g.setStroke(new BasicStroke(3));
        g.drawRect(0, bandY, getWidth() - 1, bandH - 1);
        g.setStroke(new BasicStroke(1));

        // Ana yazı
        g.setFont(new Font("Arial", Font.BOLD, 32));
        g.setColor(new Color(255, 220, 50, alpha));
        String msg = "A HUGE WAVE OF ZOMBIES IS APPROACHING!";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, bandY + 38);

        // Alt yazı: kaçıncı dalga
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(new Color(255, 160, 160, alpha));
        String sub = "--- DALGA " + state.currentWave + " ---";
        fm = g.getFontMetrics();
        g.drawString(sub, (getWidth() - fm.stringWidth(sub)) / 2, bandY + 58);
    }

    private void drawPauseOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 130));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(new Font("Arial", Font.BOLD, 54));
        g.setColor(Color.WHITE);
        drawCentered(g, "DURDURULDU", getHeight() / 2);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.setColor(new Color(200, 200, 200));
        drawCentered(g, "Devam et: P  |  Kaydet: buton", getHeight() / 2 + 50);
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(new Font("Arial", Font.BOLD, 60));
        g.setColor(new Color(255, 60, 60));
        drawCentered(g, "OYUN BİTTİ!", getHeight() / 2 - 30);
        g.setFont(new Font("Arial", Font.PLAIN, 22));
        g.setColor(Color.WHITE);
        drawCentered(g, "Öldürülen zombi: " + state.zombiesKilled, getHeight() / 2 + 30);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(new Color(200, 200, 200));
        drawCentered(g, "Ana menüye gitmek icin tıklayın", getHeight() / 2 + 70);

        addMouseListenerOnce("gameover");
    }

    private void drawVictory(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(new Font("Arial", Font.BOLD, 60));
        g.setColor(new Color(100, 255, 100));
        drawCentered(g, "ZAFER!", getHeight() / 2 - 30);
        g.setFont(new Font("Arial", Font.PLAIN, 22));
        g.setColor(Color.WHITE);
        drawCentered(g, "Tüm dalgalar tamamlandı!", getHeight() / 2 + 30);
        drawCentered(g, "Öldürülen zombi: " + state.zombiesKilled, getHeight() / 2 + 60);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(new Color(200, 230, 200));
        drawCentered(g, "Ana menüye gitmek icin tıklayın", getHeight() / 2 + 100);

        addMouseListenerOnce("victory");
    }

    private void drawCentered(Graphics2D g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, y);
    }

    /** One-shot listener for end screens — goes to menu on click. */
    private boolean endListenerAdded = false;
    private void addMouseListenerOnce(String context) {
        if (endListenerAdded) return;
        endListenerAdded = true;
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                stopGame();
                SaveManager.deleteSave();
                if (owner instanceof pvz.GameFrame) {
                    ((pvz.GameFrame) owner).showMenu();
                }
            }
        });
    }
}
