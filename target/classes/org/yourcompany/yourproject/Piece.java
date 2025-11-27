package org.yourcompany.yourproject;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;



public class Piece {
    public Type type;   
    public BufferedImage image;
    public int xPos;
    public int yPos;
    public int col,row, prevCol, prevRow;
    public boolean isWhite;
    public Piece hittingPiece = null;
    public boolean moved = false;
    public boolean twoStepped = false;

    public Piece(boolean isWhite, int col, int row) {
        this.isWhite = isWhite;
        this.col = col;
        this.row = row;
        this.prevCol = col;
        this.prevRow = row;
        this.xPos = getX(col);
        this.yPos = getY(row);
    }

    public BufferedImage getImage(String imagePath) {
        BufferedImage img = null;
    
        String fullPath = imagePath; 
    
        // a megbízható működéshez:
        if (!imagePath.startsWith("/")) {
            fullPath = "/" + imagePath; 
        }

        try {
            // Használd a Piece osztályt a betöltéshez, így biztosan a megfelelő classpath-on keres:
            img = ImageIO.read(Piece.class.getResourceAsStream(fullPath));

        } catch (Exception e) {
            System.err.println("HIBA: Nem található a kép: " + fullPath);
            e.printStackTrace();
        }
        return img;
    }

    public int getX(int col) {
        return col * Board.SQUARE_SIZE;
    }
    public int getY(int row) {
        return row * Board.SQUARE_SIZE;
    }
    public int getCol(int xPos) {
        return (xPos) / Board.SQUARE_SIZE;
    }
    public int getRow(int yPos) {
        return (yPos) / Board.SQUARE_SIZE;
    }

    public int getIndex(){
        for(int i = 0; i < GamePanel.pieces.size(); i++){
            if(GamePanel.pieces.get(i) == this){
                return i;
            }
        }
        return 0;
    }

    public void updatePosition() {
        if(type == Type.PAWN){
            if(Math.abs(row - prevRow) == 2){
                twoStepped = true;
            }
        }
        this.xPos = getX(this.col);
        this.yPos = getY(this.row);
        prevCol = getCol(this.xPos);
        prevRow = getRow(this.yPos);
        moved = true;
    }

    public void resetPosition(){
        // A bábu visszakerül az egér-nyomva tartás előtti helyére
        this.col = this.prevCol; 
        this.row = this.prevRow;
        this.xPos = getX(this.col);
        this.yPos = getY(this.row);
    }

    public boolean canMove(int targetCol, int targetRow) {
        // Alapértelmezett viselkedés: minden lépés engedélyezett
        return true;
    }
    public boolean isWithinBounds(int targetCol, int targetRow) {
        return targetCol >= 0 && targetCol < 8 && targetRow >= 0 && targetRow < 8;
    }

    public Piece getHittingPiece(int targetCol, int targetRow) {
        for (Piece p : GamePanel.simPieces) {
            if (p.col == targetCol && p.row == targetRow && p != this) {
                return p;
            }
        }
        return null;
    }
    public boolean isValidSquare(int targetCol, int targetRow) {
        hittingPiece = getHittingPiece(targetCol, targetRow);
        if (hittingPiece == null) {
            return true; // Üres mező
        } else {
            if(hittingPiece.isWhite != this.isWhite){
                return true;
            } else {
                hittingPiece = null; // Csak akkor érvényes, ha ellenfél bábuja van ott
            }
        }
        return false;
    }
    public boolean pieceIsOnStraightPath(int targetCol, int targetRow) {
        for(int c = prevCol-1; c > targetCol; c--) {
            for(Piece piece : GamePanel.simPieces) {
                if(piece.col == c && piece.row == targetRow) {
                    hittingPiece = piece;
                    return true; 
                }
            }
        }

        for(int c = prevCol+1; c < targetCol; c++) {
            for(Piece piece : GamePanel.simPieces) {
                if(piece.col == c && piece.row == targetRow) {
                    hittingPiece = piece;
                    return true; 
                }
            }
        }

        for(int c = prevRow-1; c > targetRow; c--) {
            for(Piece piece : GamePanel.simPieces) {
                if(piece.col == targetCol && piece.row == c) {
                    hittingPiece = piece;
                    return true; 
                }
            }
        }

        for(int c = prevRow+1; c < targetRow; c++) {
            for(Piece piece : GamePanel.simPieces) {
                if(piece.col == targetCol && piece.row == c) {
                    hittingPiece = piece;
                    return true; 
                }
            }
        }
        return false;
    }
    public boolean pieceIsOnDiagonalPath(int targetCol, int targetRow) {
        if(targetRow < prevRow){
            for(int c = prevCol -1; c > targetCol; c--) {
                int diff = Math.abs(prevCol - c);
                for(Piece piece : GamePanel.simPieces) {
                    if(piece.col == c && piece.row == prevRow - diff) {
                        hittingPiece = piece;
                        return true; 
                    }
                }
            }

             for(int c = prevCol+1; c < targetCol; c++) {
                int diff = Math.abs(prevCol - c);
                for(Piece piece : GamePanel.simPieces) {
                    if(piece.col == c && piece.row == prevRow - diff) {
                        hittingPiece = piece;
                        return true; 
                    }
                }
            }
        }

        if(targetRow > prevRow){
            for(int c = prevCol -1; c > targetCol; c--) {
                int diff = Math.abs(prevCol - c);
                for(Piece piece : GamePanel.simPieces) {
                    if(piece.col == c && piece.row == prevRow + diff) {
                        hittingPiece = piece;
                        return true; 
                    }
                }
            }

             for(int c = prevCol+1; c < targetCol; c++) {
                int diff = Math.abs(prevCol - c);
                for(Piece piece : GamePanel.simPieces) {
                    if(piece.col == c && piece.row == prevRow + diff) {
                        hittingPiece = piece;
                        return true; 
                    }
                }
            }
        }
        return false;
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(image, xPos, yPos, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
    }


}

