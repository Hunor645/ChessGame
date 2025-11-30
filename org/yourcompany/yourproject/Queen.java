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
            if( targetCol == prevCol || targetRow == prevRow ||
                Math.abs(targetCol - prevCol) == Math.abs(targetRow - prevRow) ) {
                // The queen may move in any direction: horizontally, vertically or diagonally
                if(isValidSquare(targetCol,targetRow)) {
                    if(targetCol == prevCol || targetRow == prevRow) {
                        // Straight-line (rook-style) move
                        if(!pieceIsOnStraightPath(targetCol, targetRow)){
                            return true;
                        }
                    } else {
                        // Diagonal (bishop-style) move
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