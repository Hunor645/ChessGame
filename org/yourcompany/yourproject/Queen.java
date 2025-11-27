package org.yourcompany.yourproject;

public class Queen extends Piece {

    public Queen(boolean isWhite, int col, int row) {
        super(isWhite, col, row);
        type = Type.QUEEN;

        if(isWhite == true) {
            this.image = getImage("resources/sprites/white_queen.png");
        } else {
            this.image = getImage("resources/sprites/black_queen.png");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinBounds(targetCol, targetRow)) {
           // System.out.println("Queen moving from (" + col + ", " + row + ") to (" + targetCol + ", " + targetRow + ")");
            if( targetCol == prevCol || targetRow == prevRow ||
                Math.abs(targetCol - prevCol) == Math.abs(targetRow - prevRow) ) {
                // A királynő bármilyen irányban léphet
                if(isValidSquare(targetCol,targetRow)) {
                    if(targetCol == prevCol || targetRow == prevRow) {
                        // Egyenes vonalú lépés
                        if(!pieceIsOnStraightPath(targetCol, targetRow)){
                            return true;
                        }
                    } else {
                        // Átlós lépés
                        if(!pieceIsOnDiagonalPath(targetCol, targetRow)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}