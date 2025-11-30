package org.yourcompany.yourproject;

import java.awt.Graphics2D;

//Board class to draw the chess board
public class Board implements Cloneable {
    private int colWith = 8;
    private int colHeight = 8;
    private int colSize = 100;
    public static final int SQUARE_SIZE = 100;
    public static final int HALF_SQUARE_SIZE = SQUARE_SIZE / 2;
    //drawing the chess board
    public void draw(Graphics2D g) {
        boolean isWhite = true;
        for (int row = 0; row < colHeight; row++) {
            for (int col = 0; col < colWith; col++) {
                if (isWhite) {
                    g.setColor(java.awt.Color.WHITE);
                } else {
                    g.setColor(java.awt.Color.BLACK);
                }
                //we should change the color after each row, beacause the chess board is checkered
                g.fillRect(col * colSize, row * colSize, colSize, colSize);
                isWhite = !isWhite;
            }
            isWhite = !isWhite;
        }
    }
    
}