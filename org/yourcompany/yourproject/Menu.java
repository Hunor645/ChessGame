package org.yourcompany.yourproject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Menu extends JPanel {
    private JButton startButton;
    private JButton exitButton;
    private JLabel titleLabel;
    private MenuListener listener;

    public interface MenuListener {
        void onStartGame();
        void onExit();
        void onSave();
        void onLoad();
    }

    public Menu(MenuListener listener) {
        this.listener = listener;
        setLayout(new GridBagLayout());
        setBackground(new Color(40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);

        // Cím
        titleLabel = new JLabel("SAKK JÁTÉK");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 60));
        titleLabel.setForeground(new Color(255, 215, 0)); // Arany szín
        add(titleLabel, gbc);

        // Start gomb
        gbc.gridy = 1;
        startButton = createButton("Játék indítása");
        startButton.addActionListener(e -> {
            if (listener != null) listener.onStartGame();
        });
        add(startButton, gbc);

        // Betöltés gomb
        gbc.gridy = 2;
        JButton loadButton = createButton("Betöltés");
        loadButton.addActionListener(e -> {
            if (listener != null) listener.onLoad();
        });
        add(loadButton, gbc);

        // Mentés gomb
        gbc.gridy = 3;
        JButton saveButton = createButton("Mentés");
        saveButton.addActionListener(e -> {
            if (listener != null) listener.onSave();
        });
        add(saveButton, gbc);

        // Kilépés gomb
        gbc.gridy = 4;
        exitButton = createButton("Kilépés");
        exitButton.addActionListener(e -> {
            if (listener != null) listener.onExit();
        });
        add(exitButton, gbc);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setPreferredSize(new Dimension(200, 60));
        button.setBackground(new Color(70, 130, 180)); // Acél kék
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // Hover effekt
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(100, 150, 200));
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(70, 130, 180));
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Háttér gradiens
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(30, 30, 30),
            0, getHeight(), new Color(50, 50, 50)
        );
        g2.setPaint(gradient);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}