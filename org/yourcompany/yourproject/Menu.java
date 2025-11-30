package org.yourcompany.yourproject;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Menu extends JPanel {
    private final JButton startButton;
    private final JButton aiButton;
    private final JButton saveButton;
    private final JButton loadButton;
    private final JButton exitButton;
    private final JLabel titleLabel;
    private final MenuListener listener;

    public interface MenuListener {
        void onStartGame();
        void onStartGameWithAI();
        void onSaveGame();
        void onLoadGame();
        void onExit();
    }

    public Menu(MenuListener listener) {
        this.listener = listener;
        setLayout(new GridBagLayout());
        setBackground(new Color(40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 20, 10, 20);

        // Title label
        gbc.gridy = 0;
        titleLabel = new JLabel("CHESS GAME");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 60));
        titleLabel.setForeground(new Color(255, 215, 0));
        add(titleLabel, gbc);

        // Start game (two-player)
        gbc.gridy = 1;
        startButton = createButton("Start Game");
        startButton.addActionListener(e -> {
            if (listener != null) listener.onStartGame();
        });
        add(startButton, gbc);

        // Play against AI
        gbc.gridy = 2;
        aiButton = createButton("Play vs AI");
        aiButton.addActionListener(e -> {
            if (listener != null) listener.onStartGameWithAI();
        });
        add(aiButton, gbc);

        // Load saved game
        gbc.gridy = 3;
        loadButton = createButton("Load");
        loadButton.addActionListener(e -> {
            if (listener != null) listener.onLoadGame();
        });
        add(loadButton, gbc);

        // Save current game
        gbc.gridy = 4;
        saveButton = createButton("Save");
        saveButton.addActionListener(e -> {
            if (listener != null) listener.onSaveGame();
        });
        add(saveButton, gbc);

        // Exit application
        gbc.gridy = 5;
        exitButton = createButton("Exit");
        exitButton.addActionListener(e -> {
            if (listener != null) listener.onExit();
        });
        add(exitButton, gbc);
    }
    // Create a styled button with hover effects
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setPreferredSize(new Dimension(200, 50));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        
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
    // Paint the menu background with a gradient
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(30, 30, 30),
            0, getHeight(), new Color(50, 50, 50)
        );
        g2.setPaint(gradient);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}