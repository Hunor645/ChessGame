package org.yourcompany.yourproject;

public class Rook extends Piece {

    public Rook(boolean isWhite, int x, int y) {
        super(isWhite, x, y);
        type = Type.ROOK;

        if(isWhite == true) {
            this.image = getImage("resources/sprites/white_rook.png");
        } else {
            this.image = getImage("resources/sprites/black_rook.png");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinBounds(targetCol, targetRow)) {
            //System.out.println("Rook moving from (" + col + ", " + row + ") to (" + targetCol + ", " + targetRow + ")");
            if(targetCol == prevCol || targetRow == prevRow) {
                // A bástya csak egyenes vonalban léphet
                if(isValidSquare(targetCol,targetRow) && !pieceIsOnStraightPath(targetCol, targetRow)){
                    return true;
                }
            }
        }
        return false;
    }
}   