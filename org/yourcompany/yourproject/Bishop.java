package org.yourcompany.yourproject;
//Bishop piece class
public class Bishop extends Piece {

    //importing the image for the bishop piece
    public Bishop(boolean isWhite, int col, int row) {
        super(isWhite, col, row);
        type = Type.BISHOP;

        if(isWhite == true) {
            this.image = getImage("resources/sprites/white_bishop.png");
        } else {
            this.image = getImage("resources/sprites/black_bishop.png");
        }
    }
    //checking if the bishop can move to the target square
    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinBounds(targetCol, targetRow)) {
            if( Math.abs(targetCol - prevCol) == Math.abs(targetRow - prevRow) ) {
                // The bishop may only move diagonally
                if(isValidSquare(targetCol,targetRow) && !pieceIsOnDiagonalPath(targetCol, targetRow)){
                    return true;
                }
            }
        }
        return false;
    }
}