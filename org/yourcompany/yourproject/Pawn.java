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
        int direction = isWhite ? -1 : 1; // White pawns move up (towards row 0), black pawns move down
        hittingPiece = getHittingPiece(targetCol, targetRow);
        
        if(targetCol == prevCol && targetRow == prevRow + direction && hittingPiece == null) {
            // Pawn moves a single square forward
            if(isValidSquare(targetCol,targetRow)){
                return true;
            }
        }
        // Initial double-step: two squares forward from the starting rank
        if((isWhite && prevRow == 6) || (!isWhite && prevRow == 1)) {
            if(targetCol == prevCol && targetRow == prevRow + 2 * direction && hittingPiece == null) {
                if(isValidSquare(targetCol,targetRow) && isValidSquare(targetCol, prevRow + direction)){
                    return true;
                }
            }
        }
        // Capture diagonally
        if(Math.abs(targetCol - prevCol) == 1 && targetRow == prevRow + direction  && hittingPiece != null) {
            if(isValidSquare(targetCol,targetRow)){
                return true;
            }
        }
        // En passant capture
        if(Math.abs(targetCol - prevCol) == 1 && targetRow == prevRow + direction && hittingPiece == null) {
            for (Piece p : GamePanel.simPieces) {
                if (p.col == targetCol && p.row == prevRow && p.type == Type.PAWN && p.isWhite != this.isWhite && p.twoStepped) {
                    
                    if(isValidSquare(targetCol,targetRow)){
                        // Pawn that can be captured via en passant
                        hittingPiece = p;
                        return true;
                    }
                }
            }
        }
        return false;
        }
}
