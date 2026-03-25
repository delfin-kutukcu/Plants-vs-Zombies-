package pvz;

import pvz.io.SaveManager;
import pvz.model.GameState;
import pvz.ui.GamePanel;
import pvz.ui.MenuPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level JFrame; switches between the menu screen and the game.
 */
public class GameFrame extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel     root  = new JPanel(cards);

    private static final String SCREEN_MENU = "MENU";
    private static final String SCREEN_GAME = "GAME";

    private GamePanel currentGamePanel;

    public GameFrame() {
        super("Plants vs Zombies - BIL 211");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        add(root);
        showMenu();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /** Show (or re-show) the main menu. */
    public void showMenu() {
        if (currentGamePanel != null) {
            currentGamePanel.stopGame();
            root.remove(currentGamePanel);
            currentGamePanel = null;
        }

        MenuPanel menu = new MenuPanel(new MenuPanel.MenuListener() {
            @Override public void onNewGame()  { startGame(new GameState()); }
            @Override public void onContinue() { loadGame(); }
        });
        menu.setPreferredSize(
                new Dimension(GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT));
        root.add(menu, SCREEN_MENU);
        cards.show(root, SCREEN_MENU);
        pack();
    }

    private void startGame(GameState state) {
        currentGamePanel = new GamePanel(state, this);
        root.add(currentGamePanel, SCREEN_GAME);
        cards.show(root, SCREEN_GAME);
        pack();
        currentGamePanel.startGame();
        currentGamePanel.requestFocusInWindow();
    }

    private void loadGame() {
        try {
            GameState state = SaveManager.load();
            // Wave was stopped on save; ensure waveActive is false
            state.waveActive = false;
            startGame(state);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Kayit dosyasi yuklenemedi: " + ex.getMessage(),
                    "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }
}
