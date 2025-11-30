package org.yourcompany.yourproject;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.InputStream;

import javax.imageio.ImageIO;


//Piece class to represent a chess piece
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
    
        // For robust behavior, ensure the resource path starts with a leading slash
        if (!imagePath.startsWith("/")) {
            fullPath = "/" + imagePath; 
        }

        // Try to load the resource stream first so we can handle missing resources gracefully
        try (InputStream is = Piece.class.getResourceAsStream(fullPath)) {
            if (is == null) {
                System.err.println("ERROR: Image not found on classpath: " + fullPath);
                // Try to load from the source resources folder as fallback (useful when running from IDE)
                try (java.io.InputStream fis = new java.io.FileInputStream("src/main/resources" + fullPath)) {
                    img = ImageIO.read(fis);
                    if (img != null) return img;
                } catch (Exception ex) {
                    // ignore, create placeholder below
                }
                // Create a simple placeholder image so the game can continue running
                int size = Board.SQUARE_SIZE > 0 ? Board.SQUARE_SIZE : 64;
                BufferedImage placeholder = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = placeholder.createGraphics();
                g.setColor(Color.DARK_GRAY);
                g.fillRect(0, 0, size, size);
                g.setColor(Color.RED);
                g.drawLine(0, 0, size, size);
                g.drawLine(size, 0, 0, size);
                g.dispose();
                return placeholder;
            }
            img = ImageIO.read(is);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load image: " + fullPath);
            e.printStackTrace();
            int size = Board.SQUARE_SIZE > 0 ? Board.SQUARE_SIZE : 64;
            BufferedImage placeholder = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = placeholder.createGraphics();
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, size, size);
            g.setColor(Color.RED);
            g.drawLine(0, 0, size, size);
            g.drawLine(size, 0, 0, size);
            g.dispose();
            return placeholder;
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
        // Move the piece back to the position it had before the drag started
        this.col = this.prevCol; 
        this.row = this.prevRow;
        this.xPos = getX(this.col);
        this.yPos = getY(this.row);
    }

    public boolean canMove(int targetCol, int targetRow) {
        // Default behavior: allow every move. Subclasses override with real rules.
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
            return true; // Empty square
        } else {
            if(hittingPiece.isWhite != this.isWhite){
                return true;
            } else {
                // Not a valid target if a friendly piece occupies the square
                hittingPiece = null;
            }
        }
        return false;
    }
    // Check if there are pieces on the straight path to the target square
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

