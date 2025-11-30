package org.yourcompany.yourproject;

public class Knight extends Piece {

    public Knight(boolean isWhite, int col, int row) {
        super(isWhite, col, row);
        type = Type.KNIGHT;

        if(isWhite == true) {
            this.image = getImage("resources/sprites/white_knight.png");
        } else {
            this.image = getImage("resources/sprites/black_knight.png");
        }
    }
    //checking if the knight can move to the target square
    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinBounds(targetCol, targetRow)) {
            if( (Math.abs(targetCol - prevCol) == 2 && Math.abs(targetRow - prevRow) == 1) ||
                (Math.abs(targetCol - prevCol) == 1 && Math.abs(targetRow - prevRow) == 2) ) {
                // The knight moves in an "L" shape (2 by 1 or 1 by 2)
                if(isValidSquare(targetCol,targetRow)){
                    return true;
                }
            }
        }
        return false;
    }
    

}