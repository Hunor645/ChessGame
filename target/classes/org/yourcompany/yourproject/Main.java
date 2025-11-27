/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package org.yourcompany.yourproject;

import javax.swing.JFrame;

/**
 *
 * @author Hunor nem admin
 */
public class Main {
    private static JFrame window;
    private static GamePanel gamePanel;
    private static Menu menuPanel;

    public static void main(String[] args) {
        window = new JFrame("Chess Game");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setSize(GamePanel.WIDTH, GamePanel.HEIGHT);

        // Menü panel
        menuPanel = new Menu(new Menu.MenuListener() {
            @Override
            public void onStartGame() {
                startGame();
            }

            @Override
            public void onExit() {
                System.exit(0);
            }

            @Override
            public void onSave() {
                if (gamePanel != null) {
                    GameSave.save(gamePanel);
                }
            }

            @Override
            public void onLoad() {
                // If no game running, start one first
                if (gamePanel == null) {
                    startGame();
                }
                GameSave.loadIfExists(gamePanel);
            }
        });

        window.add(menuPanel);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private static void startGame() {
        // Menü eltávolítása, játék panel hozzáadása
        window.remove(menuPanel);
        
        gamePanel = new GamePanel();
        window.add(gamePanel);
        window.revalidate();
        window.repaint();
        
        gamePanel.LaunchGameThread();
        // Mentés a kilépéskor: ha a program bezárul, mentsük az aktuális állást
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                GameSave.save(gamePanel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }
}
