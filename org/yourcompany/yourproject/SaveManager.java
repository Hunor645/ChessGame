package org.yourcompany.yourproject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * SaveManager
 *
 * Manages multiple save slots for the game. Save files are stored under
 * the `saves` directory as JSON files, e.g. `saves/slot_0.json`.
 *
 * Responsibilities:
 * - List available save slots
 * - Save and load game state to/from slots
 * - Delete slots
 */
public class SaveManager {
    private static final String SAVES_DIR = "saves";
    private static final int MAX_SLOTS = 10;

    static {
        // Create the saves directory if it does not already exist
        File dir = new File(SAVES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * List all available save slots.
     */
    public static List<SaveSlot> listSaveSlots() {
        List<SaveSlot> slots = new ArrayList<>();
        for (int i = 0; i < MAX_SLOTS; i++) {
            File f = getSaveFile(i);
            if (f.exists()) {
                String name = extractGameInfo(i);
                slots.add(new SaveSlot(i, name, f.lastModified()));
            }
        }
        return slots;
    }

    /**
     * Save the current game state to the specified slot.
     */
    public static boolean saveToSlot(GamePanel gp, int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) return false;

        File f = getSaveFile(slot);
        try {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            sb.append("\"currentPlayer\":").append(gp.currentPlayer).append(',');
            sb.append("\"pieces\":[");

            synchronized (GamePanel.pieces) {
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
            }

            sb.append("]}");

            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            }
            System.out.println("Saved to slot " + slot + " (" + f.getAbsolutePath() + ")");
            return true;
        } catch (Exception e) {
            System.err.println("Error saving to slot " + slot + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Load a saved game state from the specified slot into the provided
     * GamePanel instance.
     */
    public static boolean loadFromSlot(GamePanel gp, int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) return false;

        File f = getSaveFile(slot);
        if (!f.exists()) return false;

        try {
            byte[] data;
            try (FileInputStream fis = new FileInputStream(f)) {
                data = fis.readAllBytes();
            }
            String json = new String(data, StandardCharsets.UTF_8).trim();

            boolean currentPlayer = false;
            ArrayList<PieceData> list = new ArrayList<>();

            // currentPlayer
            int idx = json.indexOf("\"currentPlayer\"");
            if (idx >= 0) {
                int colon = json.indexOf(':', idx);
                int comma = json.indexOf(',', colon);
                String val = json.substring(colon + 1, comma).trim();
                currentPlayer = Boolean.parseBoolean(val);
            }

            // pieces
            int pIdx = json.indexOf("\"pieces\"");
            if (pIdx >= 0) {
                int arrStart = json.indexOf('[', pIdx);
                int arrEnd = json.indexOf(']', arrStart);
                if (arrStart >= 0 && arrEnd >= 0) {
                    String arr = json.substring(arrStart + 1, arrEnd).trim();
                    if (!arr.isEmpty()) {
                        ArrayList<String> objs = splitObjects(arr);
                        for (String obj : objs) {
                            PieceData pd = parsePiece(obj);
                            if (pd != null) list.add(pd);
                        }
                    }
                }
            }

            // Rebuild pieces
            synchronized (GamePanel.pieces) {
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
            }

            gp.currentPlayer = currentPlayer;
            synchronized (GamePanel.simPieces) {
                GamePanel.simPieces.clear();
                GamePanel.simPieces.addAll(GamePanel.pieces);
            }

            System.out.println("Loaded from slot " + slot);
            return true;
        } catch (Exception e) {
            System.err.println("Error loading from slot " + slot + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a save slot file.
     */
    public static boolean deleteSlot(int slot) {
        if (slot < 0 || slot >= MAX_SLOTS) return false;
        File f = getSaveFile(slot);
        return f.exists() && f.delete();
    }

    private static File getSaveFile(int slot) {
        return new File(SAVES_DIR + File.separator + "slot_" + slot + ".json");
    }

    private static String extractGameInfo(int slot) {
        File f = getSaveFile(slot);
        if (!f.exists()) return "Empty slot";
        try {
            byte[] data = Files.readAllBytes(f.toPath());
            String json = new String(data, StandardCharsets.UTF_8);
            // Simple info: only show the last modification time of the save
            long modified = f.lastModified();
            return "Slot " + slot + " - " + new java.util.Date(modified);
        } catch (Exception e) {
            return "Slot " + slot;
        }
    }

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

    private static class PieceData {
        String type;
        boolean isWhite;
        int col, row, prevCol, prevRow;
        boolean moved, twoStepped;
    }

    public static class SaveSlot {
        public int slot;
        public String name;
        public long lastModified;

        public SaveSlot(int slot, String name, long lastModified) {
            this.slot = slot;
            this.name = name;
            this.lastModified = lastModified;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
