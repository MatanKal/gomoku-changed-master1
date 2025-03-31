package com.example.gomoku;

import java.util.HashSet;
import java.util.Set;

public class gameLogic {
    public static final int GRID_SIZE = 13;
    private boolean isBlackTurn = false;
    private Set<String> BMoves = new HashSet<>();
    private Set<String> WMoves = new HashSet<>();
    private int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
    private String LastMove = "";

    public gameLogic() {
        resetGame();
    }

    public boolean isBlackTurn() {
        return isBlackTurn;
    }

    public void changeTurn() {
        isBlackTurn = !isBlackTurn;
    }

    public boolean placePiece(int row, int col) {
        String move = row + "," + col;
        if (BMoves.contains(move) || WMoves.contains(move)) {
            return false; // cell is already occupied
        }

        if (isBlackTurn) {
            BMoves.add(move);
        } else {
            WMoves.add(move);
        }
        return true;
    }

    public char checkWin(int row, int col) {
        String move = row + "," + col;
        Set<String> playerMoves = BMoves.contains(move) ? BMoves : WMoves;
        for (int[] dir : directions) {
            int count = 1;
            for (int i = 1; i < 5; i++) {
                int r = row + dir[0] * i;
                int c = col + dir[1] * i;
                if (!playerMoves.contains(r + "," + c)) break;
                count++;
            }

            for (int i = 1; i < 5; i++) {
                int r = row - dir[0] * i;
                int c = col - dir[1] * i;
                if (!playerMoves.contains(r + "," + c)) break;
                count++;
            }

            if (count >= 5) {
                return BMoves.contains(move) ? 'B' : 'W'; // return the winner
            }
        }
        return ' ';
    }
    public String StatePosition(int row, int col) {
        String move = row + "," + col;
        Set<String> playerMoves = BMoves.contains(move) ? BMoves : WMoves;

        // find the best pattern
        String bestPattern = null;

        for (int[] dir : directions) {
            int count = 1;
            boolean openStart = false, openEnd = false;

            //check in positive direction
            for (int i = 1; i < 5; i++) {
                int r = row + dir[0] * i, c = col + dir[1] * i;
                if (r < 0 || r >= GRID_SIZE || c < 0 || c >= GRID_SIZE || !playerMoves.contains(r + "," + c)) {
                    // check if position is empty
                    openEnd = r >= 0 && r < GRID_SIZE && c >= 0 && c < GRID_SIZE &&
                            !BMoves.contains(r + "," + c) && !WMoves.contains(r + "," + c);
                    break;
                }
                count++;
            }

            // check in negative direction
            for (int i = 1; i < 5; i++) {
                int r = row - dir[0] * i, c = col - dir[1] * i;
                if (r < 0 || r >= GRID_SIZE || c < 0 || c >= GRID_SIZE || !playerMoves.contains(r + "," + c)) {
                    openStart = r >= 0 && r < GRID_SIZE && c >= 0 && c < GRID_SIZE &&
                            !BMoves.contains(r + "," + c) && !WMoves.contains(r + "," + c);
                    break;
                }
                count++;
            }

            String pattern = null;
            if (count >= 5) pattern = "FiveInARow";
            else if (count == 4) pattern = (openStart && openEnd) ? "LiveFour" : "DeadFour";
            else if (count == 3) pattern = (openStart && openEnd) ? "LiveThree" : "DeadThree";
            else if (count == 2) pattern = (openStart && openEnd) ? "LiveTwo" : "DeadTwo";

            // if win return
            if (pattern != null && pattern.equals("FiveInARow")) {
                return pattern;
            }

            if (pattern != null && (bestPattern == null || isBetterPattern(pattern, bestPattern))) {
                bestPattern = pattern;
            }
        }

        return bestPattern;
    }


    public boolean isBetterPattern(String pattern1, String pattern2) {
        if (pattern1 == null) return false;
        if (pattern2 == null) return true;
        if (pattern1.equals("FiveInARow")) return true;
        if (pattern2.equals("FiveInARow")) return false;

        if (pattern1.equals("LiveFour")) return true;
        if (pattern2.equals("LiveFour")) return false;

        if (pattern1.equals("DeadFour")) return true;
        if (pattern2.equals("DeadFour")) return false;

        if (pattern1.equals("LiveThree")) return true;
        if (pattern2.equals("LiveThree")) return false;

        if (pattern1.equals("DeadThree")) return true;
        if (pattern2.equals("DeadThree")) return false;

        if (pattern1.equals("LiveTwo")) return true;
        if (pattern2.equals("LiveTwo")) return false;
        return false;
    }





    public boolean isInBounds(int row, int col) {
        return row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE;
    }

    public void resetGame() {
        BMoves.clear();
        WMoves.clear();
    }
    public Set<String> getBlackMoves() {
        return new HashSet<>(BMoves);
    }
    public Set<String> getWhiteMoves() {
        return new HashSet<>(WMoves);
    }
    public void SetLastMove(int row, int col) {
        LastMove = row + "," + col;
    }
    public String getLastMove() {
        return LastMove;
    }
}