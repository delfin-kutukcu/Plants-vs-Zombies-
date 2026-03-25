package pvz.ui;

import pvz.io.SaveManager;

import javax.swing.*;
import java.awt.*;

/**
 * Start-menu screen shown when the application launches.
 */
public class MenuPanel extends JPanel {

    public interface MenuListener {
        void onNewGame();
        void onContinue();
    }

    public MenuPanel(MenuListener listener) {
        setLayout(new BorderLayout());
        setBackground(new Color(34, 85, 34));

        // ---- title --------------------------------------------------
        JLabel title = new JLabel("PLANTS vs ZOMBIES", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 52));
        title.setForeground(new Color(255, 240, 50));
        title.setBorder(BorderFactory.createEmptyBorder(90, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // ---- subtitle -----------------------------------------------
        JLabel sub = new JLabel("BIL 211 - Proje", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.ITALIC, 18));
        sub.setForeground(new Color(200, 230, 200));

        // ---- buttons ------------------------------------------------
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setLayout(new GridLayout(3, 1, 0, 18));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(40, 260, 80, 260));

        JButton newGame = makeButton("Yeni Oyun");
        newGame.addActionListener(e -> listener.onNewGame());

        JButton cont = makeButton("Devam Et");
        cont.setEnabled(SaveManager.hasSave());
        cont.addActionListener(e -> listener.onContinue());

        btnPanel.add(newGame);
        btnPanel.add(cont);
        btnPanel.add(sub);

        add(btnPanel, BorderLayout.CENTER);
    }

    private JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 22));
        btn.setBackground(new Color(80, 160, 80));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
