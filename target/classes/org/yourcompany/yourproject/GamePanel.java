
package org.yourcompany.yourproject;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JPanel;


public class GamePanel extends JPanel implements Runnable  {
    public static final int WIDTH = 1200;
    public static final int HEIGHT = 900;
    final int FPS = 60;
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();

    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> capturedPieces = new ArrayList<>();
    ArrayList<Piece> promotedPieces = new ArrayList<>();
    Piece activePiece , checkingPiece = null;
    public static Piece castlingPiece = null;

    public static final int WHITE = 0;
    public static final int BLACK = 1;
    boolean currentPlayer = true; //true = white, false = black

    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver = false;
    boolean stealMate = false;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH,HEIGHT));
        setBackground(Color.black);
        // Only add listeners if they aren't already registered (prevent duplicate registration)
        if (Arrays.stream(getMouseListeners()).noneMatch(l -> l == mouse)) {
            addMouseListener(mouse);
        }
        if (Arrays.stream(getMouseMotionListeners()).noneMatch(l -> l == mouse)) {
            addMouseMotionListener(mouse);
        }

        //Prom();
        setPieces();
        copyPieces(pieces, simPieces);
        // Ha van mentés, töltsük be és folytassuk onnan
        GameSave.loadIfExists(this);
    }   

    public void LaunchGameThread(){
        gameThread = new Thread(this);
        gameThread.start();
        
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
       /* pieces.add(new Knight(true,1,7));
        pieces.add(new Knight(true,6,7));
        pieces.add(new Bishop(true,5,7));
        pieces.add(new Bishop(true,2,7));
        pieces.add(new Queen(true,3,7));*/
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
    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> destination){
        synchronized(destination) {
            destination.clear();
            for(int i = 0; i < source.size(); i++){
                destination.add(source.get(i));
            }
        }
    }

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

    private void update(){
        if(promotion){
           
            promoting();
        }
        else if(!gameOver && !stealMate){
            // System.out.println("update");
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
               // System.out.println("Mouse released");
                if(activePiece != null){
                    System.out.println("Finalizing move for piece: " + activePiece.type + " to (" + activePiece.col + ", " + activePiece.row + ")");
                    
                    // ELLENŐRZÉS: Csak érvényes lépés esetén véglegesítjük a lépést!
                    if(validSquare){ 
                        // Véglegesítés
                        copyPieces(simPieces, pieces);
                        activePiece.updatePosition(); 
                        if(castlingPiece != null){
                            castlingPiece.updatePosition();
                        }
                        if(isKinginCheck() && isCheckmate()){
                            System.out.println("King is in check!");
                            gameOver = true;

                        }else if(isStaleMate() && !isKinginCheck()){
                            System.out.println("Stalemate!");
                            stealMate = true;
                        }
                        else{
                            if(canPromote(activePiece)){
                                System.out.println("Promotion available!");
                                promotion = true;
                            }
                            else {
                                isStaleMate();
                                changePlayer();
                            }
                        }
                        
                       
                       
                    } else {
                        copyPieces(pieces, simPieces);
                        // Lépés visszaállítása
                        activePiece.resetPosition(); // <--- HÍVJA AZ ÚJ resetPosition()-t!
                        activePiece = null;
                    
                    }                                                     
                }
            }
        }
        
    }
    
    public void simulate(){

        canMove = false;
        validSquare = false;
        System.out.println("Simulating move...");
        copyPieces(pieces, simPieces);

        if(castlingPiece != null){
            castlingPiece.col = castlingPiece.prevCol;
            castlingPiece.xPos = castlingPiece.getX(castlingPiece.col);
            castlingPiece = null;
        }
        System.out.println("Simulating move for piece: " + activePiece.type + " from (" + activePiece.prevCol + ", " + activePiece.prevRow + ") to (" + activePiece.getCol(mouse.x) + ", " + activePiece.getRow(mouse.y) + ")");
        //copyPieces(pieces, simPieces);
        activePiece.col = activePiece.getCol(mouse.x);
        activePiece.row = activePiece.getRow(mouse.y);
        activePiece.xPos = activePiece.getX(activePiece.col);
        activePiece.yPos = activePiece.getY(activePiece.row);
        
        if(activePiece.canMove(activePiece.col, activePiece.row)){
            validSquare = true;
            canMove = true;
            System.out.println("Hitting piece " + activePiece.hittingPiece);
            
            if(activePiece.hittingPiece != null){
                //capture
                simPieces.remove(activePiece.hittingPiece.getIndex());
            }
            System.out.println("After capture, simPieces size: " + simPieces.size());
            
        
            checkCastling();

            System.out.println("Simulated move to (" + activePiece.col + ", " + activePiece.row + ")");
            if(isIllegal(activePiece) || opponentCanCaptureKing()){
                validSquare = false;
            }
            System.out.println("Move valid: " + validSquare);
            if(activePiece.col == activePiece.prevCol && activePiece.row == activePiece.prevRow){
                validSquare = false;
            }
        }
    }

    private void checkCastling(){
        if(castlingPiece != null){
            if(castlingPiece.col == 0){
                //király oldal
                castlingPiece.col = castlingPiece.col +3;
            } else if(castlingPiece.col ==7){
                //bástya oldal
                castlingPiece.col -=2;
            }
            castlingPiece.xPos = castlingPiece.getX(castlingPiece.col);
        }
    }

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
    private void promoting(){
        System.err.println("Promoting...");
        if(mouse.pressed){
            for(Piece p : promotedPieces){
                if(mouse.x / Board.SQUARE_SIZE == p.col && mouse.y / Board.SQUARE_SIZE == p.row){
                       //promote
                       simPieces.remove(activePiece.getIndex());
                       System.out.println("Chosen piece: " + p.type);
                       switch(p.type){
                           case QUEEN -> simPieces.add(new Queen(activePiece.isWhite, activePiece.col, activePiece.row));
                           case ROOK -> simPieces.add(new Rook(activePiece.isWhite, activePiece.col, activePiece.row));
                           case BISHOP -> simPieces.add(new Bishop(activePiece.isWhite, activePiece.col, activePiece.row));
                           case KNIGHT -> simPieces.add(new Knight(activePiece.isWhite, activePiece.col, activePiece.row));
                       }
                       
                       System.out.println("Remove ");
                       copyPieces(simPieces, pieces);
                       activePiece = null;
                       promotion = false;
                       System.out.println("Promoted to " + p.type);
                       changePlayer();
                       break;
                }
            }
        }
    }

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
    private boolean isKinginCheck(){
        Piece king = getKing(true);
        if(activePiece.canMove(king.col, king.row)){
            checkingPiece = activePiece;
            return true;
        }
        else{
            checkingPiece = null;
        }
         
        return false;   
    }

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
        return true;

    }
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
    @Override
    public synchronized void paintComponent(Graphics g){
        super.paintComponent(g);
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
            if(currentPlayer){
                g2.drawString("White's turn", 840, 750);
                if((checkingPiece != null && checkingPiece.isWhite == false) || opponentCanCaptureKing()){
                    g2.setColor(Color.RED);
                    g2.drawString("Check!", 840, 700);
                }
            } else {
                g2.drawString("Black's turn", 840, 750);
                if((checkingPiece != null && checkingPiece.isWhite == true) || opponentCanCaptureKing()){
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
            g2.setColor(Color.CYAN);
            g2.drawString("Checkmate! Game Over " + who + " wins.", 100, 400);
        }
        if(stealMate){
            g2.setColor(Color.CYAN);
            g2.drawString("Stalemate! Game Over. It's a draw.", 100, 400);
        }
    }   
}