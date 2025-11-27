package org.yourcompany.yourproject;

public class Bishop extends Piece {

    public Bishop(boolean isWhite, int col, int row) {
        super(isWhite, col, row);
        type = Type.BISHOP;

        if(isWhite == true) {
            this.image = getImage("resources/sprites/white_bishop.png");
        } else {
            this.image = getImage("resources/sprites/black_bishop.png");
        }
    }
    
    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinBounds(targetCol, targetRow)) {
            //System.out.println("Bishop moving from (" + col + ", " + row + ") to (" + targetCol + ", " + targetRow + ")");
            if( Math.abs(targetCol - prevCol) == Math.abs(targetRow - prevRow) ) {
                // A futó csak átlósan léphet
                System.out.println("Bishop diagonal move detected");
                if(isValidSquare(targetCol,targetRow) && !pieceIsOnDiagonalPath(targetCol, targetRow)){
                    
                    System.out.println("Bishop move valid");
                    return true;
                }
            }
        }
        return false;
    }
}