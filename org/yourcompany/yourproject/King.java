package org.yourcompany.yourproject;

public class King extends Piece {
    public King(boolean isWhite, int col, int row) {
        super(isWhite, col, row);
        type = Type.KING;

        if(isWhite == true) {
            this.image = getImage("resources/sprites/white_king.png");
        } else {
            this.image = getImage("resources/sprites/black_king.png");
        }
    }
    //checking if the king can move to the target square
    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinBounds(targetCol, targetRow)) {
            if( Math.abs(targetCol - prevCol) + Math.abs(targetRow - prevRow) == 1 || Math.abs(targetCol - prevCol) * Math.abs(targetRow - prevRow) == 1){
                // The king may move exactly one square in any direction
                if(isValidSquare(targetCol,targetRow)){
                    return true;
                }
            }
            if(moved == false && targetCol == prevCol + 2 && targetRow == prevRow && pieceIsOnStraightPath(targetCol, targetRow) == false){
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == prevCol + 3 && piece.row == targetRow && piece.moved == false){
                        GamePanel.castlingPiece = piece;
                        return true;
                    }
                }
            }

            if(moved == false && targetCol == prevCol-2 && targetRow == prevRow && pieceIsOnStraightPath(targetCol, targetRow) == false){
                Piece p[] = new Piece[2];
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == prevCol - 3 && piece.row == targetRow){
                        p[0] = piece;
                    }
                    if(piece.col == prevCol -4 && piece.row == targetRow){
                        p[1] = piece;
                    }
                }
                // After the loop: if the intermediate square is empty (p[0] == null)
                // and the rook (p[1]) exists and has not moved yet, castling is allowed
                if(p[0] == null && p[1] != null && p[1].moved == false){
                    GamePanel.castlingPiece = p[1];
                    return true;
                }
               
            
            } 
        }
    return false;
    }
}