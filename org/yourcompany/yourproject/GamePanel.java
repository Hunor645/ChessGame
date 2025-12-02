package org.yourcompany.yourproject;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JPanel;


public class GamePanel extends JPanel implements Runnable  {
    public static final int WIDTH = 1200;
    public static final int HEIGHT = 900;
    final int FPS = 60;
    public Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();

    // Static lists to hold the pieces on the board
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> capturedPieces = new ArrayList<>();
    ArrayList<Piece> promotedPieces = new ArrayList<>();
    Piece activePiece , checkingPiece = null;
    public static Piece castlingPiece = null;

    boolean currentPlayer = true; //true = white, false = black
    
    boolean aiMode = false;  // Whether AI opponent is enabled
    ChessAI ai = null;  // The AI instance when enabled

    boolean isPaused = false;  // Pause state flag

    // Listener interface to notify when returning to menu
    public interface GamePanelListener {
        void onReturnToMenu();
    }
    private GamePanelListener listener;

    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver = false;
    boolean stealMate = false;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH,HEIGHT));
        setBackground(Color.black);
        setFocusable(true);
        // Ensure static game state is reset when creating a new GamePanel instance
        synchronized (pieces) {
            pieces.clear();
        }
        synchronized (simPieces) {
            simPieces.clear();
        }
        castlingPiece = null;
        currentPlayer = true;
        activePiece = null;
        gameOver = false;
        stealMate = false;
        promotion = false;
        
        // Only add listeners if they aren't already registered (prevent duplicate registration)
        if (Arrays.stream(getMouseListeners()).noneMatch(l -> l == mouse)) {
            addMouseListener(mouse);
        }
        if (Arrays.stream(getMouseMotionListeners()).noneMatch(l -> l == mouse)) {
            addMouseMotionListener(mouse);
        }
        
        // Keyboard listener for pause, save, and menu return
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    isPaused = !isPaused;
                }
                if (e.getKeyCode() == KeyEvent.VK_S && (e.getModifiers() & java.awt.event.InputEvent.CTRL_MASK) != 0) {
                    // Ctrl+S: Open save dialog
                    SaveLoadDialog dialog = new SaveLoadDialog(
                        (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(GamePanel.this),
                        true
                    );
                    dialog.setVisible(true);
                    int slot = dialog.getSelectedSlot();
                    if (slot >= 0) {
                        SaveManager.saveToSlot(GamePanel.this, slot);
                        javax.swing.JOptionPane.showMessageDialog(GamePanel.this, "Save successful!");
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // ESC: Return to menu
                    if (listener != null) {
                        listener.onReturnToMenu();
                    }
                }
            }
        });

        // Mouse listener for menu button click
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                // Check whether the click occurred inside the MENU button area
                int buttonX = WIDTH - 300;
                int buttonY = 15;
                int buttonWidth = 110;
                int buttonHeight = 45;
                
                if (e.getX() >= buttonX && e.getX() <= buttonX + buttonWidth &&
                    e.getY() >= buttonY && e.getY() <= buttonY + buttonHeight) {
                    if (listener != null) {
                        listener.onReturnToMenu();
                    }
                }
            }
        });

        // Initialize pieces for a new game
        setPieces();
        copyPieces(pieces, simPieces);
    }   

    public void LaunchGameThread(){
        gameThread = new Thread(this);
        gameThread.start();
        
    }
    
    public void setAIMode(boolean aiEnabled) {
        this.aiMode = aiEnabled;
        if (aiEnabled) {
            this.ai = new ChessAI(false);  // create AI (black)
        }
    }
    
    public void setGamePanelListener(GamePanelListener listener) {
        this.listener = listener;
    }

    public void Prom(){
         pieces.add(new King(true,3,1));
         pieces.add(new Queen(true,3,2));
         pieces.add(new King(false,0,6));
         pieces.add(new Pawn(false,7,6));
    }


    public void setPieces(){
        pieces.add(new Rook(true,0,7));
        pieces.add(new Rook(true,7,7));
        pieces.add(new Knight(true,1,7));
        pieces.add(new Knight(true,6,7));
        pieces.add(new Bishop(true,5,7));
        pieces.add(new Bishop(true,2,7));
        pieces.add(new Queen(true,3,7));
        pieces.add(new King(true,4,7));
        pieces.add(new Pawn(true,0,6));
        pieces.add(new Pawn(true,1,6));
        pieces.add(new Pawn(true,2,6));
        pieces.add(new Pawn(true,3,6));
        pieces.add(new Pawn(true,4,6));
        pieces.add(new Pawn(true,5,6));
        pieces.add(new Pawn(true,6,6));
        pieces.add(new Pawn(true,7,6));

        //Black pieces
        pieces.add(new Rook(false,0,0));
        pieces.add(new Rook(false,7,0));
        pieces.add(new Knight(false,1,0));
        pieces.add(new Knight(false,6,0));
        pieces.add(new Bishop(false,5,0));
        pieces.add(new Bishop(false,2,0));
        pieces.add(new Queen(false,3,0));
        pieces.add(new King(false,4,0));
        pieces.add(new Pawn(false,0,1));
        pieces.add(new Pawn(false,1,1));
        pieces.add(new Pawn(false,2,1));
        pieces.add(new Pawn(false,3,1));
        pieces.add(new Pawn(false,4,1));
        pieces.add(new Pawn(false,5,1));
        pieces.add(new Pawn(false,6,1));
        pieces.add(new Pawn(false,7,1));
    }

    // Utility method to copy pieces from one list to another
    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> destination){
        synchronized(destination) {
            destination.clear();
            for(int i = 0; i < source.size(); i++){
                destination.add(source.get(i));
            }
        }
    }

    /**
     * Evaluate the loaded position for checkmate/stalemate and set
     * `gameOver`/`stealMate` accordingly so no further moves are allowed.
     * This should be called after a saved game is loaded into `pieces`/`simPieces`.
     */
    public void evaluateLoadedGameState() {
        // Reset transient state
        activePiece = null;
        checkingPiece = null;
        castlingPiece = null;
        promotion = false;

        // Ensure simPieces contains the current pieces
        copyPieces(pieces, simPieces);

        // Find the king for the side to move
        Piece king = null;
        for (Piece p : simPieces) {
            if (p.type == Type.KING && p.isWhite == currentPlayer) {
                king = p;
                break;
            }
        }

        if (king == null) {
            // No king found: treat as game over to be safe
            gameOver = true;
            stealMate = false;
            return;
        }
        // Find any attacker of the king (checking piece)
        Piece attacker = null;
        for (Piece p : new ArrayList<>(simPieces)) {
            if (p.isWhite != king.isWhite) {
                if (p.canMove(king.col, king.row)) {
                    attacker = p;
                    
                    break;
                }
            }
        }

        if (attacker != null) {
            // Use existing checkmate detection which expects activePiece to be the checking piece
            activePiece = attacker;
            checkingPiece = attacker;
            currentPlayer = !currentPlayer;  // Temporarily switch to the side to move
            boolean checkmate = isCheckmate();
            currentPlayer = !currentPlayer;  // Switch back after checkmate evaluation
            gameOver = checkmate;
            stealMate = false;
            // Clear activePiece to prevent accidental dragging after load
            activePiece = null;
        } else {
            // No check: check for stalemate
            gameOver = false;
            stealMate = isStaleMate();
        }
        System.out.println("evaluateLoadedGameState: currentPlayer=" + (currentPlayer?"white":"black") +
                ", attacker=" + (attacker==null?"none":attacker.type + "@" + attacker.col + "," + attacker.row) +
                ", gameOver=" + gameOver + ", stalemate=" + stealMate);
    }

    // Game loop to update and repaint the game panel
    @Override
    public void run(){
        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while(gameThread != null){
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if(delta >= 1){
                update();
                repaint();
                delta--;
            }
        }
    }
    // Update game state
    private void update(){
        if(promotion){
           
            promoting();
            repaint();
        }
        // If the game is over (checkmate/stalemate) don't allow any interaction
        else if (gameOver || stealMate) {
            // Clear any transient interaction state so no piece stays active
            activePiece = null;
            canMove = false;
            validSquare = false;
            promotion = false;
            // reset mouse pressed so release logic won't try to finalize moves
            if (mouse != null) mouse.pressed = false;
            return;
        }
        // Only process game updates if not paused, not in promotion, and game is ongoing
        else if(!gameOver && !stealMate){
            if(mouse.pressed){
                if(activePiece == null){
                    for(Piece p : simPieces){
                        if(p.isWhite == currentPlayer && p.col == mouse.x / Board.SQUARE_SIZE && p.row == mouse.y / Board.SQUARE_SIZE){
                                activePiece = p;
                                                    
                        }
                    }
                }
                else {
                        simulate();
                }

            }
            if(!mouse.pressed){
               // Mouse released: finalize or revert the tentative move
                if(activePiece != null){
                    
                    // Validation: only finalize the move if the simulated target square is valid
                    if(validSquare){ 
                        // Véglegesítés
                        copyPieces(simPieces, pieces);
                        activePiece.updatePosition(); 
                        if(castlingPiece != null){
                            castlingPiece.updatePosition();
                        }
                        if(isKinginCheck() && isCheckmate()){
                            System.out.println("Black is CHECKMATE - White won!");
                            gameOver = true;

                        }else if(isStaleMate() && !isKinginCheck()){
                            System.out.println("Black is in STALEMATE");
                            stealMate = true;
                        }
                        else{
                            if(isKinginCheck()){
                                System.out.println("Black is in CHECK");
                            } else {
                                System.out.println("Black is safe");
                            }
                            if(canPromote(activePiece)){
                                promotion = true;
                            }
                            else {
                                isStaleMate();
                                changePlayer();
                            }
                        }
                        
                       
                       
                    } else {
                        copyPieces(pieces, simPieces);
                        // Revert piece back to its previous coordinates
                        activePiece.resetPosition();
                        activePiece = null;
                    
                    }                                                     
                }
            }
        }
        
    }
    
/**
     * Simulate moving the currently active piece to the mouse position.
     *
     * Copies the real pieces into simPieces, applies the tentative move,
     * resolves captures and castling, and then checks whether the move
     * would leave the current player's king in check. Results are stored
     * in canMove and validSquare.
     */
    public void simulate(){

        canMove = false;
        validSquare = false;
        copyPieces(pieces, simPieces);

        if(castlingPiece != null){
            castlingPiece.col = castlingPiece.prevCol;
            castlingPiece.xPos = castlingPiece.getX(castlingPiece.col);
            castlingPiece = null;
        }
        //copyPieces(pieces, simPieces);
        activePiece.col = activePiece.getCol(mouse.x);
        activePiece.row = activePiece.getRow(mouse.y);
        activePiece.xPos = activePiece.getX(activePiece.col);
        activePiece.yPos = activePiece.getY(activePiece.row);
        
        if(activePiece.canMove(activePiece.col, activePiece.row)){
            validSquare = true;
            canMove = true;
            if(activePiece.hittingPiece != null){
                // Capture the piece on the simulated board
                simPieces.remove(activePiece.hittingPiece.getIndex());
            }
            
        
            checkCastling();

            if(isIllegal(activePiece) || opponentCanCaptureKing()){
                validSquare = false;
            }
            if(activePiece.col == activePiece.prevCol && activePiece.row == activePiece.prevRow){
                validSquare = false;
            }
        }
    }

    private void checkCastling(){
        if(castlingPiece != null){
            if(castlingPiece.col == 0){
                // Kingside: move rook three squares towards the king
                castlingPiece.col = castlingPiece.col +3;
            } else if(castlingPiece.col ==7){
                // Queenside: move rook two squares towards the king
                castlingPiece.col -=2;
            }
            castlingPiece.xPos = castlingPiece.getX(castlingPiece.col);
        }
    }
    // Change the current player and reset twoStepped flags
    private void changePlayer(){
        if(currentPlayer == true){
            currentPlayer = false;

            for(Piece p : pieces){
                if(p.isWhite == false){
                    p.twoStepped = false; 
                }   
            }
        } else {
            currentPlayer = true;

            for(Piece p : pieces){
                if(p.isWhite == true){
                    p.twoStepped = false; 
                }
            }
        }
        activePiece = null;
        
        // Let the AI play a move when AI mode is enabled
        repaint();
        if (aiMode && !currentPlayer) {
            // AI controls the black side (false), so it moves when currentPlayer is false
            try {
                Thread.sleep(500);  // Small delay so the human can see the move
                if (ai != null) {
                    ai.makeMove(this);
                    // Update simPieces to match pieces after AI's move
                    copyPieces(pieces, simPieces);
                    
                    // Immediately check if the opponent (white) is in check/checkmate/stalemate after AI's move
                    currentPlayer = true;  // Switch to white BEFORE checking
                    
                    // Check for check
                    if (isKinginCheck()) {
                        System.out.println("White is in CHECK");
                        if (isCheckmate()) {
                            System.out.println("White is CHECKMATE - AI won!");
                            gameOver = true;
                        }
                    } else if (isStaleMate()) {
                        System.out.println("White is in STALEMATE");
                        stealMate = true;
                    } else {
                        System.out.println("White is safe");
                    }
                    // NOTE: do NOT call changePlayer() here to avoid recursion
                    // The game loop will call changePlayer() again when white moves, or the game ends
                    // Ensure the UI updates immediately so the panel shows check/checkmate.
                    // Use SwingUtilities.invokeLater so repaint runs on the EDT.
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        System.out.println("Requesting repaint on EDT after AI move");
                        repaint();
                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean canPromote(Piece piece){
        if(piece.type == Type.PAWN){
            if(piece.isWhite && piece.row == 0 || !piece.isWhite && piece.row == 7){
                promotedPieces.clear();
                promotedPieces.add(new Queen(piece.isWhite, 9, 2));
                promotedPieces.add(new Rook(piece.isWhite, 9, 3));
                promotedPieces.add(new Bishop(piece.isWhite, 9, 4));
                promotedPieces.add(new Knight(piece.isWhite, 9, 5));
                return true;
            }
        }
        return false;
    }
    /**
     * Handle pawn promotion: lets the user choose a replacement piece from the
     * promotedPieces list and applies that choice to both simPieces and pieces.
     */
    private void promoting(){
        if(mouse.pressed){
            for(Piece p : promotedPieces){
                if(mouse.x / Board.SQUARE_SIZE == p.col && mouse.y / Board.SQUARE_SIZE == p.row){
                       //promote
                       simPieces.remove(activePiece.getIndex());
                       switch(p.type){
                           case QUEEN -> simPieces.add(new Queen(activePiece.isWhite, activePiece.col, activePiece.row));
                           case ROOK -> simPieces.add(new Rook(activePiece.isWhite, activePiece.col, activePiece.row));
                           case BISHOP -> simPieces.add(new Bishop(activePiece.isWhite, activePiece.col, activePiece.row));
                           case KNIGHT -> simPieces.add(new Knight(activePiece.isWhite, activePiece.col, activePiece.row));
                       }
                       
                       copyPieces(simPieces, pieces);
                       activePiece = null;
                       promotion = false;
                       changePlayer();
                       break;
                }
            }
        }
    }
    // Check if moving the piece would leave its own king in check
    private boolean isIllegal(Piece king){
        if(king.type == Type.KING){
            ArrayList<Piece> snapshot = new ArrayList<>(simPieces);
            for(Piece p : snapshot){
                if(p != king && p.isWhite != king.isWhite && p.canMove(king.col, king.row)){
                    return true;
                }
            }
        }
        return false;
    }
    // Check if the current player's king is in check
    private boolean isKinginCheck(){
        Piece king = getKing(true);
        if (king == null) return false;

        for (Piece p : new ArrayList<>(simPieces)) {
            if (p.isWhite != king.isWhite) {
                if (p.canMove(king.col, king.row)) {
                    checkingPiece = p;
                    return true;
                }
            }
        }
        checkingPiece = null;
        return false;
    }
    // Get the king piece for the specified side
    private Piece getKing(boolean opponent){
        Piece king = null;
        for(Piece p : simPieces){
            if(opponent){
                if(p.type == Type.KING && p.isWhite != currentPlayer){
                    return p;
                }
            }
            else {
                if(p.type == Type.KING && p.isWhite == currentPlayer){
                    return p;
                }
            }
        }
        return null;
    }
    // Check if the opponent can capture the current player's king
    public synchronized boolean opponentCanCaptureKing(){
        Piece king  = getKing(false);
        // Create a snapshot to avoid ConcurrentModificationException during iteration
        ArrayList<Piece> snapshot = new ArrayList<>(simPieces);
        for(Piece p : snapshot){
            if(p.isWhite != king.isWhite && p.canMove(king.col, king.row)){
                return true;
            }
        }
        return false;
    }
    /**
     * Determine whether the side to move is checkmated.
     *
     * Assumes activePiece/checkingPiece describe the current check.
     * First checks if the king has any legal escape square. If not, it then
     * scans the line of attack between the checking piece and the king to see
     * whether any friendly piece can block the attack or capture the attacker.
     */
    public synchronized boolean isCheckmate(){
        Piece king = getKing(true);
        if(kingCanMove(king)){
            return false;
        }
        else {

            int colDiff = Math.abs(activePiece.col - king.col);
            int rowDiff = Math.abs(activePiece.row - king.row);

            if(colDiff == 0){
                if(checkingPiece.row < king.row){
                    for(int r = checkingPiece.row +1; r < king.row; r++){
                        for(Piece p : simPieces){
                            if(p != king && p.isWhite == king.isWhite && p.canMove(checkingPiece.col, r)){
                                return false;
                            }
                        }
                    }
                }
                if(checkingPiece.row > king.row){
                    for(int r = checkingPiece.row -1; r > king.row; r--){
                        for(Piece p : simPieces){
                            if(p != king && p.isWhite == king.isWhite && p.canMove(checkingPiece.col, r)){
                                return false;
                            }
                        }
                    }
                }
            }
            else if(rowDiff == 0){
                if(checkingPiece.col < king.col){
                    for(int c = checkingPiece.col +1; c < king.col; c++){
                        for(Piece p : simPieces){
                            if(p != king && p.isWhite == king.isWhite && p.canMove(c, checkingPiece.row)){
                                return false;
                            }
                        }
                    }
                }
                if(checkingPiece.col > king.col){
                    for(int c = checkingPiece.col -1; c > king.col; c--){
                        for(Piece p : simPieces){
                            if(p != king && p.isWhite == king.isWhite && p.canMove(c, checkingPiece.row)){
                                return false;
                            }
                        }
                    }
                }
            }
            else if(colDiff == rowDiff){
                if(checkingPiece.row < king.row) {
                    if(checkingPiece.col < king.col){
                        for(int c = checkingPiece.col, row = checkingPiece.row ; c < king.col; c++, row++){
                            for(Piece p : simPieces){
                                if(p != king && p.isWhite == king.isWhite && p.canMove(c, row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingPiece.col > king.col){
                        for(int c = checkingPiece.col, row = checkingPiece.row ; c > king.col; c--, row++){
                            for(Piece p : simPieces){
                                if(p != king && p.isWhite == king.isWhite && p.canMove(c, row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
                if(checkingPiece.row > king.row) {
                    if(checkingPiece.col < king.col){
                        for(int c = checkingPiece.col, row = checkingPiece.row ; c < king.col; c++, row--){
                            for(Piece p : simPieces){
                                if(p != king && p.isWhite == king.isWhite && p.canMove(c, row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingPiece.col > king.col){
                        for(int c = checkingPiece.col, row = checkingPiece.row ; c > king.col; c--, row--){
                            for(Piece p : simPieces){
                                if(p != king && p.isWhite == king.isWhite && p.canMove(c, row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            else{
                //Knight check
            }
             
        }
        currentPlayer = !currentPlayer;
        return true;

    }
    /**
     * Determine whether the current position is a stalemate for the side to move.
     *
     * Quick-exits if only two pieces remain (king vs king), then checks whether
     * the king has any legal move, and finally scans all friendly moves to see
     * if any move avoids leaving the king in check.
     */
    public synchronized boolean isStaleMate(){
        int count = 0;
         ArrayList<Piece> snapshot = new ArrayList<>(simPieces);
        for(Piece p : snapshot){
                count++;
        }
        if(count <= 2){
            return true;
        }

        Piece king = getKing(true);
        if(kingCanMove(king)){
           return false;
        }
        else {
            synchronized (snapshot){
                for(Piece p : snapshot){
                    if(p.isWhite == king.isWhite){
                        for(int col =0; col <8; col++){
                            for(int row =0; row <8; row++){
                                if(p.canMove(col, row)){
                                    //simulate
                                    copyPieces(pieces, simPieces);
                                    p.col = col;
                                    p.row = row;
                                    p.xPos = p.getX(col);
                                    p.yPos = p.getY(row);
                                    
                                    if(isIllegal(p) == false){
                                        copyPieces(pieces, simPieces);
                                        return false;
                                    }
                                    copyPieces(pieces, simPieces);
                                    p.resetPosition();
                                }
                            }
                        }
                    }
                }
            }            
        }
        return true;
    }

    /**
     * Returns true if the given king has at least one legal move to any of the
     * eight surrounding squares that does not leave it in check.
     */
    public boolean kingCanMove(Piece king){
        for(int colPlus = -1; colPlus <= 1; colPlus++){
            for(int rowPlus = -1; rowPlus <= 1; rowPlus++){
                if(colPlus == 0 && rowPlus == 0){
                    continue;
                }
                if(isValidMove(king, colPlus, rowPlus)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Helper for kingCanMove / isStaleMate.
     *
     * Temporarily moves the king by (colPlus, rowPlus), checks if the king
     * could move there according to its own movement rules and whether the
     * resulting position would still be legal (king not in check).
     */
    public synchronized boolean isValidMove(Piece king, int colPlus, int rowPlus){
        boolean isValidMove = false;

        king.col = king.prevCol + colPlus;
        king.row = king.prevRow + rowPlus;

        if(king.canMove(king.col, king.row)){
            if(king.hittingPiece != null){
                //capture
                simPieces.remove(king.hittingPiece.getIndex());
            }
            if(isIllegal(king) == false){
                isValidMove = true;
            }

        }
        
        king.resetPosition();
        copyPieces(pieces, simPieces);
        return isValidMove;
    }
    // Paint the game components
    @Override
    public synchronized void paintComponent(Graphics g){
        super.paintComponent(g);
       // System.out.println("paintComponent called - currentPlayer=" + currentPlayer + ", gameOver=" + gameOver + ", stealMate=" + stealMate);
        Graphics2D g2 = (Graphics2D)g;
        board.draw(g2);
        // Create a snapshot to avoid ConcurrentModificationException during iteration
        ArrayList<Piece> snapshot = new ArrayList<>(simPieces);
        for(Piece p : snapshot){
            p.draw(g2);
        }

        if(activePiece != null){
            if(canMove){
                if(isIllegal(activePiece) || opponentCanCaptureKing()){ 
                    g2.setColor(new Color(255,0,0,100));
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activePiece.col * Board.SQUARE_SIZE, activePiece.row * Board.SQUARE_SIZE,
                    Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                    activePiece.draw(g2);
                } else {
                    g2.setColor(new Color(0,255,0,100));
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activePiece.col * Board.SQUARE_SIZE, activePiece.row * Board.SQUARE_SIZE,
                    Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                    activePiece.draw(g2);}
                
            }
        }

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("book Antiqua", Font.PLAIN, 40));
        g2.setColor(Color.white);

        if(promotion){
            g2.drawString("Promote to:", 850, 200);
            for(Piece p : promotedPieces){
                g2.drawImage(p.image, p.getX(p.col), p.getY(p.row), Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
            }
        }
        else{
            
            // Show whose turn it is and whether that side is in check
            if (currentPlayer) {
                g2.setColor(Color.WHITE);
                g2.drawString("White's turn", 840, 750);
                if (isKinginCheck() || opponentCanCaptureKing()){
                    g2.setColor(Color.RED);
                    g2.drawString("Check!", 840, 700);
                }
            } else {
                g2.setColor(Color.WHITE);
                g2.drawString("Black's turn", 840, 750);
                if (isKinginCheck() || opponentCanCaptureKing()){
                    g2.setColor(Color.RED);
                    g2.drawString("Check!", 840, 700);
                }
            }
        }
        if(gameOver){
            String who = "";
            if(currentPlayer){
                who = "White";
            } else {
                who = "Black";
            }
            g2.setColor(Color.RED);
            g2.drawString("Checkmate! Game Over " + who + " wins.", 100, 400);
        }
        if(stealMate){
            g2.setColor(Color.RED);
            g2.drawString("Stalemate! Game Over. It's a draw.", 100, 400);
        }
        
        // Draw the MENU button in the top-right corner
        int buttonX = WIDTH - 300;
        int buttonY = 15;
        int buttonWidth = 110;
        int buttonHeight = 45;
        
        g2.setColor(new Color(80, 80, 80));
        g2.fillRect(buttonX, buttonY, buttonWidth, buttonHeight);
        g2.setColor(Color.YELLOW);
        g2.setStroke(new java.awt.BasicStroke(3));
        g2.drawRect(buttonX, buttonY, buttonWidth, buttonHeight);
        
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(Color.YELLOW);
        String buttonText = "MENU";
        java.awt.FontMetrics fm = g2.getFontMetrics();
        int textX = buttonX + (buttonWidth - fm.stringWidth(buttonText)) / 2;
        int textY = buttonY + ((buttonHeight - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(buttonText, buttonX + (buttonWidth - fm.stringWidth(buttonText)) / 2, buttonY + ((buttonHeight - fm.getHeight()) / 2) + fm.getAscent());
        
        // Helper text describing keyboard shortcuts
        g2.setFont(new Font("book Antiqua", Font.PLAIN, 14));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString("P - Pause | Ctrl+S - Save | ESC - Menu", 900, HEIGHT - 10);
    }   
}