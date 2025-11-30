/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package org.yourcompany.yourproject;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

public class Main {
    private static JFrame window;
    private static GamePanel gamePanel;
    private static Menu menuPanel;

    public static void main(String[] args) {
        window = new JFrame("Chess Game");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setSize(GamePanel.WIDTH, GamePanel.HEIGHT);

        // Main menu panel for starting/loading/saving games
        menuPanel = new Menu(new Menu.MenuListener() {
            @Override
            public void onStartGame() {
                startGame(false);
            }

            @Override
            public void onStartGameWithAI() {
                startGame(true);
            }

            @Override
            public void onSaveGame() {
                if (gamePanel != null) {
                    SaveLoadDialog dialog = new SaveLoadDialog(window, true);
                    dialog.setVisible(true);
                    int slot = dialog.getSelectedSlot();
                    if (slot >= 0) {
                        SaveManager.saveToSlot(gamePanel, slot);
                        JOptionPane.showMessageDialog(window, "Save successful!");
                    }
                }
            }

            @Override
            public void onLoadGame() {
                SaveLoadDialog dialog = new SaveLoadDialog(window, false);
                dialog.setVisible(true);
                int slot = dialog.getSelectedSlot();
                if (slot >= 0) {
                    window.remove(menuPanel);
                    gamePanel = new GamePanel();
                    
                    // Set listener to return to menu on ESC or MENU button click
                    gamePanel.setGamePanelListener(new GamePanel.GamePanelListener() {
                        @Override
                        public void onReturnToMenu() {
                            gamePanel.gameThread = null;
                            window.remove(gamePanel);
                            window.add(menuPanel);
                            window.revalidate();
                            window.repaint();
                            menuPanel.requestFocusInWindow();
                        }
                    });
                    
                    window.add(gamePanel);
                    window.revalidate();
                    window.repaint();
                    
                    // Request focus for input handling
                    gamePanel.requestFocusInWindow();
                    
                    // Load the game state from the selected save slot
                    SaveManager.loadFromSlot(gamePanel, slot);
                    
                    // Launch the game thread
                    gamePanel.LaunchGameThread();
                    JOptionPane.showMessageDialog(window, "Load successful!");
                }
            }

            @Override
            public void onExit() {
                System.exit(0);
            }
        });

        window.add(menuPanel);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private static void startGame(boolean withAI) {
        window.remove(menuPanel);
        
        gamePanel = new GamePanel();
        gamePanel.setAIMode(withAI);
        
        // Set up ESC key in GamePanel to return to the main menu
        gamePanel.setGamePanelListener(new GamePanel.GamePanelListener() {
            @Override
            public void onReturnToMenu() {
                // Stop the game thread
                gamePanel.gameThread = null;
                
                // Return to menu
                window.remove(gamePanel);
                window.add(menuPanel);
                window.revalidate();
                window.repaint();
                
                // Give focus back to the menu for input
                menuPanel.requestFocusInWindow();
            }
        });
        
        window.add(gamePanel);
        window.revalidate();
        window.repaint();
        
        // Give keyboard focus to the GamePanel for input handling
        gamePanel.requestFocusInWindow();
        
        gamePanel.LaunchGameThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                GameSave.save(gamePanel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }
}
