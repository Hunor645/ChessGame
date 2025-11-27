package org.yourcompany.yourproject;

public class Pawn extends Piece {

    public Pawn(boolean color, int col, int row) {
        super(color, col, row);

        type = Type.PAWN;
        
        if(color) {
            this.image = getImage("resources/sprites/white_pawn.png");
        } else {
            this.image = getImage("resources/sprites/black_pawn.png");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {
        if(!isWithinBounds(targetCol, targetRow)) {
            return false;
        }
        //System.out.println("Pawn moving from (" + prevCol + ", " + prevRow + ") to (" + targetCol + ", " + targetRow + ")");
        int direction = isWhite ? -1 : 1; // Fehér bábuk felfelé, fekete lefelé lépnek
        //System.err.println("Direction: " + direction);
        hittingPiece = getHittingPiece(targetCol, targetRow);
        
        //System.out.println("Direction: " + (prevRow + direction));
        if(targetCol == prevCol && targetRow == prevRow + direction && hittingPiece == null) {
            // A gyalog egy mezőt léphet előre
            //System.out.println("Pawn single step move detected");
            if(isValidSquare(targetCol,targetRow)){
                //System.out.println("Pawn move valid");
                return true;
            }
        }
        // Kezdőlépés: két mező előre
        if((isWhite && prevRow == 6) || (!isWhite && prevRow == 1)) {
            if(targetCol == prevCol && targetRow == prevRow + 2 * direction && hittingPiece == null) {
                if(isValidSquare(targetCol,targetRow) && isValidSquare(targetCol, prevRow + direction)){
                    //System.out.println("Pawn move valid");
                    return true;
                }
            }
        }
        // Ütés átlósan
        if(Math.abs(targetCol - prevCol) == 1 && targetRow == prevRow + direction  && hittingPiece != null) {
            if(isValidSquare(targetCol,targetRow)){
                //System.out.println("Pawn move valid");
                return true;
            }
        }
        // En Passant ütés
        if(Math.abs(targetCol - prevCol) == 1 && targetRow == prevRow + direction && hittingPiece == null) {
            for (Piece p : GamePanel.simPieces) {
                if (p.col == targetCol && p.row == prevRow && p.type == Type.PAWN && p.isWhite != this.isWhite && p.twoStepped) {
                    
                    if(isValidSquare(targetCol,targetRow)){
                        hittingPiece = p;  // Az en passant ütött bábu
                        return true;
                    }
                }
            }
        }
        return false;
        }
        //System.out.println("Pawn move invalid");
}