package org.yourcompany.yourproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Simple JSON save/load for the chess game.
 * This implementation writes a compact JSON file and parses it with minimal logic
 * so no external JSON library is required.
 */
public class GameSave {

    private static final String DEFAULT_SAVE = "game_save.json";
    // Save the current game state to the default file
    public static boolean save(GamePanel gp) {
        return save(gp, DEFAULT_SAVE);
    }
    // Save the current game state to the specified file
    public static boolean save(GamePanel gp, String path) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            sb.append("\"currentPlayer\":").append(gp.currentPlayer).append(',');
            sb.append("\"pieces\":[");

            for (int i = 0; i < GamePanel.pieces.size(); i++) {
                Piece p = GamePanel.pieces.get(i);
                sb.append('{');
                sb.append("\"type\":\"").append(p.type.name()).append("\"");
                sb.append(',').append("\"isWhite\":").append(p.isWhite);
                sb.append(',').append("\"col\":").append(p.col);
                sb.append(',').append("\"row\":").append(p.row);
                sb.append(',').append("\"prevCol\":").append(p.prevCol);
                sb.append(',').append("\"prevRow\":").append(p.prevRow);
                sb.append(',').append("\"moved\":").append(p.moved);
                sb.append(',').append("\"twoStepped\":").append(p.twoStepped);
                sb.append('}');
                if (i < GamePanel.pieces.size() - 1) sb.append(',');
            }

            sb.append("]}");

            try (FileOutputStream fos = new FileOutputStream(path)) {
                fos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            }
            System.out.println("Saved game to " + path);
            return true;
        } catch (Exception e) {
            System.err.println("Error saving game: " + e.getMessage());
            return false;
        }
    }
    // Load the game state from the default file if it exists
    public static boolean loadIfExists(GamePanel gp) {
        return loadIfExists(gp, DEFAULT_SAVE);
    }
    // Load the game state from the specified file if it exists
    public static boolean loadIfExists(GamePanel gp, String path) {
        File f = new File(path);
        if (!f.exists()) return false;
        return load(gp, path);
    }
    // Load the game state from the specified file
    public static boolean load(GamePanel gp, String path) {
        try {
            File f = new File(path);
            if (!f.exists()) return false;
            byte[] data;
            try (FileInputStream fis = new FileInputStream(f)) {
                data = fis.readAllBytes();
            }
            String json = new String(data, StandardCharsets.UTF_8).trim();

            // Minimal parsing: expect the exact structure we write.
            boolean currentPlayer = false;
            ArrayList<PieceData> list = new ArrayList<>();

            // find currentPlayer
            int idx = json.indexOf("\"currentPlayer\"");
            if (idx >= 0) {
                int colon = json.indexOf(':', idx);
                int comma = json.indexOf(',', colon);
                String val = json.substring(colon + 1, comma).trim();
                currentPlayer = Boolean.parseBoolean(val);
            }

            int pIdx = json.indexOf("\"pieces\"");
            if (pIdx >= 0) {
                int arrStart = json.indexOf('[', pIdx);
                int arrEnd = json.indexOf(']', arrStart);
                if (arrStart >= 0 && arrEnd >= 0) {
                    String arr = json.substring(arrStart + 1, arrEnd).trim();
                    if (!arr.isEmpty()) {
                        // Split piece objects (assumes no nested objects and simple formatting)
                        ArrayList<String> objs = splitObjects(arr);
                        for (String obj : objs) {
                            PieceData pd = parsePiece(obj);
                            if (pd != null) list.add(pd);
                        }
                    }
                }
            }

            // Rebuild pieces
            GamePanel.pieces.clear();
            for (PieceData pd : list) {
                Piece np;
                switch (pd.type) {
                    case "KING" -> np = new King(pd.isWhite, pd.col, pd.row);
                    case "QUEEN" -> np = new Queen(pd.isWhite, pd.col, pd.row);
                    case "ROOK" -> np = new Rook(pd.isWhite, pd.col, pd.row);
                    case "BISHOP" -> np = new Bishop(pd.isWhite, pd.col, pd.row);
                    case "KNIGHT" -> np = new Knight(pd.isWhite, pd.col, pd.row);
                    case "PAWN" -> np = new Pawn(pd.isWhite, pd.col, pd.row);
                    default -> np = new Piece(pd.isWhite, pd.col, pd.row);
                }
                np.prevCol = pd.prevCol;
                np.prevRow = pd.prevRow;
                np.moved = pd.moved;
                np.twoStepped = pd.twoStepped;
                np.xPos = np.getX(np.col);
                np.yPos = np.getY(np.row);
                GamePanel.pieces.add(np);
            }

            gp.currentPlayer = currentPlayer;
            synchronized (GamePanel.simPieces) {
                GamePanel.simPieces.clear();
                GamePanel.simPieces.addAll(GamePanel.pieces);
            }

            System.out.println("Loaded game from " + path);
            return true;
        } catch (Exception e) {
            System.err.println("Error loading game: " + e.getMessage());
            return false;
        }
    }
    // Helper class to hold piece data during loading
    private static class PieceData {
        String type;
        boolean isWhite;
        int col, row, prevCol, prevRow;
        boolean moved, twoStepped;
    }
    // Split JSON array of objects into individual object strings
    private static ArrayList<String> splitObjects(String arr) {
        ArrayList<String> out = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < arr.length(); i++) {
            char c = arr.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    out.add(arr.substring(start, i + 1));
                }
            }
        }
        return out;
    }
    // Parse a single piece JSON object into PieceData
    private static PieceData parsePiece(String obj) {
        PieceData pd = new PieceData();
        String s = obj.trim();
        if (s.startsWith("{")) s = s.substring(1);
        if (s.endsWith("}")) s = s.substring(0, s.length() - 1);
        String[] parts = s.split(",");
        for (String part : parts) {
            String[] kv = part.split(":", 2);
            if (kv.length < 2) continue;
            String key = kv[0].trim();
            if (key.startsWith("\"") && key.endsWith("\"")) key = key.substring(1, key.length() - 1);
            String val = kv[1].trim();
            if (val.startsWith("\"") && val.endsWith("\"")) val = val.substring(1, val.length() - 1);

            switch (key) {
                case "type" -> pd.type = val;
                case "isWhite" -> pd.isWhite = Boolean.parseBoolean(val);
                case "col" -> pd.col = Integer.parseInt(val);
                case "row" -> pd.row = Integer.parseInt(val);
                case "prevCol" -> pd.prevCol = Integer.parseInt(val);
                case "prevRow" -> pd.prevRow = Integer.parseInt(val);
                case "moved" -> pd.moved = Boolean.parseBoolean(val);
                case "twoStepped" -> pd.twoStepped = Boolean.parseBoolean(val);
            }
        }
        return pd;
    }
}
