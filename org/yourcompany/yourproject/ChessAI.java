package org.yourcompany.yourproject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simple Chess AI implementation.
 *
 * This AI enumerates legal moves for its color and selects one at random.
 * It simulates moves on a deep-copied board (`GamePanel.simPieces`) so that
 * move legality checks (including king-safety) are performed exactly the same
 * way as for human players.
 */
public class ChessAI {
    private final boolean isWhite;
    private final Random random = new Random();

    public ChessAI(boolean isWhite) {
        this.isWhite = isWhite;
    }

    /**
     * Find all legal moves for this AI's color and execute a randomly
     * chosen legal move.
     *
     * The method:
     * 1. Creates a deep copy of `GamePanel.pieces` into `GamePanel.simPieces`.
     * 2. For each simulated friendly piece, checks all target squares and
     *    validates each candidate move by running it on a fresh simulated
     *    board and verifying king-safety.
     * 3. Maps simulated pieces back to real pieces and performs the chosen
     *    move on the real `GamePanel.pieces` list.
     *
     * Returns true if a move was made; false otherwise.
     */
    public boolean makeMove(GamePanel gamePanel) {
        System.out.println("ChessAI: makeMove() called for " + (isWhite?"White":"Black"));
        List<Move> possibleMoves = new ArrayList<>();
        // Create a deep copy of the real pieces into a fresh simulated list so simulation cannot
        // accidentally mutate the real objects. We will assign this list to GamePanel.simPieces
        // during simulation checks.
        List<Piece> simCopy = new ArrayList<>();
        synchronized (GamePanel.pieces) {
            for (Piece p : GamePanel.pieces) {
                Piece copy = createPieceCopy(p);
                simCopy.add(copy);
            }
        }

        // Set the global simPieces to our copy so piece helpers operate on the
        // simulated board during validation.
        synchronized (GamePanel.simPieces) {
            GamePanel.simPieces.clear();
            GamePanel.simPieces.addAll(simCopy);
        }

        // Iterate simulated pieces and collect legal moves using the exact same checks as humans
        List<Piece> simSnapshot = new ArrayList<>(GamePanel.simPieces);
        for (Piece simPiece : simSnapshot) {
            if (simPiece.isWhite != isWhite) continue;
            int origCol = simPiece.col;
            int origRow = simPiece.row;
            

            for (int col = 0; col < 8; col++) {
                for (int row = 0; row < 8; row++) {
                    if (col == origCol && row == origRow) continue;

                    // Use the simulated piece's canMove against the simulated board
                    if (!simPiece.canMove(col, row)) continue;

                    // Prepare a fresh simulation state for this trial so tests do
                    // not interfere with one another
                    simCopy.clear();
                    synchronized (GamePanel.pieces) {
                        for (Piece p : GamePanel.pieces) {
                            simCopy.add(createPieceCopy(p));
                        }
                    }
                    synchronized (GamePanel.simPieces) {
                        GamePanel.simPieces.clear();
                        GamePanel.simPieces.addAll(simCopy);
                    }
                    //GamePanel.repaint();

                    // Find the corresponding simulated piece in this new sim list
                    Piece simPieceLive = findPieceInSimByPositionAndType(origCol, origRow, simPiece.type, simPiece.isWhite);
                    if (simPieceLive == null) continue; // shouldn't happen

                    // Apply the candidate move to the simulated piece
                    simPieceLive.col = col;
                    simPieceLive.row = row;
                    simPieceLive.xPos = simPieceLive.getX(col);
                    simPieceLive.yPos = simPieceLive.getY(row);

                    // Remove any captured piece from the simulated board
                    Piece capturedOnSim = simPieceLive.getHittingPiece(col, row);
                    if (capturedOnSim != null) {
                        synchronized (GamePanel.simPieces) {
                            GamePanel.simPieces.remove(capturedOnSim);
                        }
                    }

                    // King-safety check on simulated board: ensure own king is not
                    // attacked after the simulated move
                    Piece king = null;
                    for (Piece p : GamePanel.simPieces) {
                        if (p.type == Type.KING && p.isWhite == isWhite) { king = p; break; }
                    }

                    boolean valid = true;
                    if (king != null) {
                        List<Piece> enemySnapshot = new ArrayList<>(GamePanel.simPieces);
                        for (Piece enemy : enemySnapshot) {
                            if (enemy.isWhite != isWhite && enemy != simPieceLive) {
                                if (enemy.canMove(king.col, king.row)) {
                                    valid = false;
                                    break;
                                }
                            }
                        }
                    }

                    // If the simulated move keeps our king safe, map simulated pieces
                    // back to the real pieces and record this move as legal.
                    if (valid) {
                        Piece realPiece = findRealPieceByPositionAndType(origCol, origRow, simPiece.type, simPiece.isWhite);
                        Piece realCaptured = null;
                        if (capturedOnSim != null) {
                            realCaptured = findRealPieceByPositionAndType(capturedOnSim.col, capturedOnSim.row, capturedOnSim.type, capturedOnSim.isWhite);
                        }
                        if (realPiece != null) {
                            possibleMoves.add(new Move(realPiece, col, row, realCaptured));
                        }
                    }
                }
            }
        }
        
        if (possibleMoves.isEmpty()) {
            System.out.println("ChessAI: no legal moves found");
            return false;
        }
        
        // Choose a random move from the legal moves
        Move chosen = possibleMoves.get(random.nextInt(possibleMoves.size()));
        
        // Apply the chosen move to the real pieces list
        synchronized (GamePanel.pieces) {
            chosen.piece.col = chosen.targetCol;
            chosen.piece.row = chosen.targetRow;
            chosen.piece.updatePosition();
            
            if (chosen.capturedPiece != null) {
                GamePanel.pieces.remove(chosen.capturedPiece);
            }
            
            // Synchronize simPieces with the updated real pieces list
            GamePanel.simPieces.clear();
            GamePanel.simPieces.addAll(GamePanel.pieces);
        }
        System.out.println("ChessAI: moved " + chosen.piece.type + " to " + chosen.piece.col + "," + chosen.piece.row);
        
        // Check whether a pawn promotion is necessary and perform it.
        if (chosen.piece.type == Type.PAWN) {
            if ((isWhite && chosen.piece.row == 0) || (!isWhite && chosen.piece.row == 7)) {
                // Promotion required - AI promotes to queen by default
                promotePiece(chosen.piece);
            }
        }
        
        return true;
    }
    
    /**
     * Promote a pawn to a queen. The AI always promotes to a queen.
     */
    private void promotePiece(Piece pawn) {
        synchronized (GamePanel.pieces) {
            GamePanel.pieces.remove(pawn);
            Queen queen = new Queen(pawn.isWhite, pawn.col, pawn.row);
            GamePanel.pieces.add(queen);
            
            GamePanel.simPieces.clear();
            GamePanel.simPieces.addAll(GamePanel.pieces);
        }
    }
    
    private boolean isValidMove(Piece piece) {
        // This method is no longer used; keep for compatibility
        // fallback: simple check on current board
        Piece king = getKing();
        if (king == null) return true;
        List<Piece> snapshot = new ArrayList<>(GamePanel.pieces);
        for (Piece enemyPiece : snapshot) {
            if (enemyPiece.isWhite != isWhite && enemyPiece != piece) {
                if (enemyPiece.canMove(king.col, king.row)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private Piece getKing() {
        for (Piece p : GamePanel.pieces) {
            if (p.type == Type.KING && p.isWhite == isWhite) {
                return p;
            }
        }
        return null;
    }

    // Helper: create a fresh copy of a Piece (same concrete type) with its flags
    private Piece createPieceCopy(Piece p) {
        Piece copy = switch (p.type) {
            case PAWN -> new Pawn(p.isWhite, p.col, p.row);
            case ROOK -> new Rook(p.isWhite, p.col, p.row);
            case KNIGHT -> new Knight(p.isWhite, p.col, p.row);
            case BISHOP -> new Bishop(p.isWhite, p.col, p.row);
            case QUEEN -> new Queen(p.isWhite, p.col, p.row);
            case KING -> new King(p.isWhite, p.col, p.row);
            default -> new Piece(p.isWhite, p.col, p.row);
        };
        copy.prevCol = p.prevCol;
        copy.prevRow = p.prevRow;
        copy.moved = p.moved;
        copy.twoStepped = p.twoStepped;
        return copy;
    }

    // Find a piece in the current GamePanel.simPieces by original position/type/color
    private Piece findPieceInSimByPositionAndType(int col, int row, Type type, boolean isWhite) {
        for (Piece p : GamePanel.simPieces) {
            if (p.col == col && p.row == row && p.type == type && p.isWhite == isWhite) {
                return p;
            }
        }
        return null;
    }

    // Find the real piece in GamePanel.pieces by position/type/color
    private Piece findRealPieceByPositionAndType(int col, int row, Type type, boolean isWhite) {
        for (Piece p : GamePanel.pieces) {
            if (p.col == col && p.row == row && p.type == type && p.isWhite == isWhite) {
                return p;
            }
        }
        // If not found by exact position (e.g., captured piece), try matching by type and color only
        for (Piece p : GamePanel.pieces) {
            if (p.type == type && p.isWhite == isWhite) return p;
        }
        return null;
    }

    private static class Move {
        Piece piece;
        int targetCol;
        int targetRow;
        Piece capturedPiece;

        Move(Piece piece, int col, int row, Piece captured) {
            this.piece = piece;
            this.targetCol = col;
            this.targetRow = row;
            this.capturedPiece = captured;
        }
    }
}
