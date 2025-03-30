package com.example.gomoku;

import java.util.HashSet;
import java.util.Set;

public class ailogic {

    private gameLogic gameLogic;
    private int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

    public ailogic(gameLogic gameLogic) {
        this.gameLogic = gameLogic;
    }

    public int[] aiMove() { // logic for the game

        // DIFFERENT GAME STATES:
        // 1 GAME START
        // start of the game
        if (gameLogic.getBlackMoves().size() <= 2 && gameLogic.getWhiteMoves().size() <= 2) {
            return earlyGameMove();
        } // a move that wins the game
        // 2WINNING MOVE
        int[] winningMove = findWinningMove();
        if (winningMove != null) {
            return winningMove;
        }
        // 3BLOCK PLAYER WIN
        // blocking player move
        int[] blockingMove = findBlockingMove();
        if (blockingMove != null) {
            return blockingMove;
        }
        // 4 WIN NEXT TURN - I have "OPEN THREE",
        // with correct move - guarantee win next turn
        int[] nextMoveWin = findNextMoveWin();
        if (nextMoveWin != null) {
            return nextMoveWin;
        }
        // 5 BLOCK PAYER WIN NEXT TURN - PLAYER has "OPEN THREE"
        // wihtout blocking - loss is guaranteed next turn
        int[] nextMoveBlock = findNextMoveBlock();
        if (nextMoveBlock != null) {
            return nextMoveBlock;
        }
        // 6 STRATEGIC MOVE
        // create neighbors set -  DO NOT CHECK ALL THE BOARD!!!
        // only balck and white pieces!
        // the best possible move - from the neghbors set
        //check which move is best according to patterns.
        // ONGOING GAME
        // check sequence
        int[] strategicMove = findStrategicMove();
        if (strategicMove != null) {
            return strategicMove;
        }
        return findRandomMove();
    }
    public boolean isValidMove(int row, int col) { // checker for outside bounderies and if the move is already made
        if (row < 0 || row >= gameLogic.GRID_SIZE || col < 0 || col >= gameLogic.GRID_SIZE) {
            return false;
        }
        String move = row + "," + col;
        return !gameLogic.getBlackMoves().contains(move) && !gameLogic.getWhiteMoves().contains(move);
    }
    public int[] findWinningMove() {
        Set<String> blackMoves = gameLogic.getBlackMoves();

        // First check for direct winning threats (4 consecutive stones)
        for (String move : blackMoves) {
            String[] coords = move.split(",");
            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);

            for (int[] dir : directions) {
                // Create an array to represent positions in this direction
                // -1=out of bounds/white, 0=empty, 1=black
                int[] line = new int[9]; // Center position at index 4
                line[4] = 1; // Current stone is black

                // Map the line in forward direction
                for (int i = 1; i <= 4; i++) {
                    int r = row + dir[0] * i;
                    int c = col + dir[1] * i;

                    if (r < 0 || r >= gameLogic.GRID_SIZE || c < 0 || c >= gameLogic.GRID_SIZE ||
                            gameLogic.getWhiteMoves().contains(r + "," + c)) {
                        line[4+i] = -1; // Out of bounds or blocked
                    } else if (blackMoves.contains(r + "," + c)) {
                        line[4+i] = 1; // Black stone
                    } else {
                        line[4+i] = 0; // Empty
                    }
                }

                // Map the line in backward direction
                for (int i = 1; i <= 4; i++) {
                    int r = row - dir[0] * i;
                    int c = col - dir[1] * i;

                    if (r < 0 || r >= gameLogic.GRID_SIZE || c < 0 || c >= gameLogic.GRID_SIZE ||
                            gameLogic.getWhiteMoves().contains(r + "," + c)) {
                        line[4-i] = -1; // Out of bounds or blocked
                    } else if (blackMoves.contains(r + "," + c)) {
                        line[4-i] = 1; // Black stone
                    } else {
                        line[4-i] = 0; // Empty
                    }
                }

                // Check for winning threats and return the position to play

                // Check for 4 consecutive stones with an open end: BBBB_
                for (int start = 0; start <= 4; start++) {
                    if (countConsecutive(line, start, 4) && line[start+4] == 0) {
                        // Calculate position to win
                        int winPos = start + 4;
                        int winRow = row + dir[0] * (winPos - 4);
                        int winCol = col + dir[1] * (winPos - 4);
                        return new int[]{winRow, winCol};
                    }
                }

                // Check for reverse pattern: _BBBB
                for (int end = 8; end >= 4; end--) {
                    if (countConsecutive(line, end-3, -4) && line[end-4] == 0) {
                        // Calculate position to win
                        int winPos = end - 4;
                        int winRow = row + dir[0] * (winPos - 4);
                        int winCol = col + dir[1] * (winPos - 4);
                        return new int[]{winRow, winCol};
                    }
                }

                // Check for split patterns, starting with BB_BB
                for (int start = 0; start <= 4; start++) {
                    // Need at least 5 positions from start
                    if (start + 4 >= line.length) continue;

                    // Count total black stones and empty spots in this 5-cell window
                    int blackCount = 0;
                    int emptyPos = -1;

                    for (int i = 0; i < 5; i++) {
                        int pos = start + i;
                        if (line[pos] == 1) {
                            blackCount++;
                        } else if (line[pos] == 0) {
                            // If we already found an empty spot, this might be a split pattern
                            if (emptyPos != -1) {
                                // More than one empty spot, not a direct threat
                                emptyPos = -1;
                                break;
                            }
                            emptyPos = pos;
                        } else {
                            // Blocked by edge or white stone
                            emptyPos = -1;
                            break;
                        }
                    }

                    // If we found exactly 4 black stones and 1 empty spot within 5 positions
                    if (blackCount == 4 && emptyPos != -1) {
                        // Calculate the position to win
                        int offset = emptyPos - 4; // How far from center
                        int winRow = row + dir[0] * offset;
                        int winCol = col + dir[1] * offset;
                        return new int[]{winRow, winCol};
                    }
                }

                // Check for more complex patterns like _BB_BB_, BB__BB, etc.
                // These need a longer window (6-7 positions)
                for (int start = 0; start <= 2; start++) {
                    // Need at least 7 positions from start
                    if (start + 6 >= line.length) continue;

                    // Count total black stones and empty spots in 7-cell window
                    int blackCount = 0;
                    int emptyCount = 0;
                    int firstEmptyPos = -1;

                    for (int i = 0; i < 7; i++) {
                        int pos = start + i;
                        if (line[pos] == 1) {
                            blackCount++;
                        } else if (line[pos] == 0) {
                            if (firstEmptyPos == -1) {
                                firstEmptyPos = pos;
                            }
                            emptyCount++;
                        } else {
                            // Blocked
                            blackCount = 0;
                            break;
                        }
                    }

                    // For patterns like _OO_OO_ with many stones but with gaps
                    if (blackCount >= 4 && emptyCount > 0) {
                        // We need to identify the most critical gap to play

                        // First check if there's a middle gap between 2 stones on each side
                        for (int i = 1; i < 6; i++) {
                            int pos = start + i;
                            if (line[pos] == 0 &&
                                    countStonesInRange(line, start, pos-1) >= 2 &&
                                    countStonesInRange(line, pos+1, start+6) >= 2) {
                                // This is a critical gap like in BB_BB pattern
                                int offset = pos - 4; // How far from center
                                int winRow = row + dir[0] * offset;
                                int winCol = col + dir[1] * offset;
                                return new int[]{winRow, winCol};
                            }
                        }

                        // If no critical middle gap, play the first gap
                        int offset = firstEmptyPos - 4;
                        int winRow = row + dir[0] * offset;
                        int winCol = col + dir[1] * offset;
                        return new int[]{winRow, winCol};
                    }
                }
            }
        }
        return null;
    }

    public int[] findNextMoveWin() {
        Set<String> blackMoves = gameLogic.getBlackMoves();
        for (String move : blackMoves) {
            String[] moveS = move.split(",");
            int row = Integer.parseInt(moveS[0]);
            int col = Integer.parseInt(moveS[1]);
            for (int[] dir : directions) {
                int consecutiveCount = 1;
                int[] openSpots = new int[2];
                int openSpotCount = 0;
                for (int i = 1; i < 4; i++) {
                    int r = row + dir[0] * i;
                    int c = col + dir[1] * i;
                    if (r < 0 || r >= gameLogic.GRID_SIZE || c < 0 || c >= gameLogic.GRID_SIZE) {
                        break;
                    }
                    if (blackMoves.contains(r + "," + c)) {
                        consecutiveCount++;
                    } else if (openSpotCount < 2 && !gameLogic.getWhiteMoves().contains(r + "," + c)) {
                        openSpots[openSpotCount++] = r * 100 + c;
                        break;
                    } else {
                        break;
                    }
                }
                for (int i = 1; i < 4; i++) {
                    int r = row - dir[0] * i;
                    int c = col - dir[1] * i;
                    if (r < 0 || r >= gameLogic.GRID_SIZE || c < 0 || c >= gameLogic.GRID_SIZE) {
                        break;
                    }
                    if (blackMoves.contains(r + "," + c)) {
                        consecutiveCount++;
                    } else if (openSpotCount < 2 && !gameLogic.getWhiteMoves().contains(r + "," + c)) {
                        openSpots[openSpotCount++] = r * 100 + c;
                        break;
                    } else {
                        break;
                    }
                }
                if (consecutiveCount == 3 && openSpotCount == 2) {
                    int pos = openSpots[0];
                    return new int[]{pos / 100, pos % 100};
                }
            }
        }
        return null;
    }

    public int[] earlyGameMove() { // 2 first moves
        if (gameLogic.getBlackMoves().isEmpty() && !gameLogic.getWhiteMoves().contains("6,6")) {
            return new int[]{5, 6};
        } else if (gameLogic.getLastMove().isEmpty()) {
            int center = gameLogic.GRID_SIZE / 2;
            return new int[]{center, center};
        }
        String[] lastMove = gameLogic.getLastMove().split(",");
        int lastRow = Integer.parseInt(lastMove[0]);
        int lastCol = Integer.parseInt(lastMove[1]);

        // check if player played near the border
        if (lastRow <= 3 || lastCol <= 3 || lastRow >= 9 || lastCol >= 9) {
            return CloseBorderInteraction(lastRow, lastCol);
        }

        // find an open spot near the last move
        for (int[] dir : directions) {
            int newRow = lastRow + dir[0];
            int newCol = lastCol + dir[1];

            if (isValidMove(newRow, newCol)) {
                return new int[]{newRow, newCol};
            }
        }

        return findRandomMove();
    }

    private int[] findRandomMove() {
        int center = gameLogic.GRID_SIZE / 2;
        if (isValidMove(center, center)) {
            return new int[]{center, center};
        }
        for (String move : gameLogic.getBlackMoves()) { // try to check near moves already made
            String[] coords = move.split(",");
            int r = Integer.parseInt(coords[0]);
            int c = Integer.parseInt(coords[1]);
            for (int[] dir : directions) {
                for (int i = 1; i < 8; i++) {
                    int newRow = r + dir[0] * i;
                    int newCol = c + dir[1] * i;
                    if (isValidMove(newRow, newCol)) {
                        return new int[]{newRow, newCol};
                    }
                }
            }
        }

        // try cells close to the center
        for (int d = 1; d < gameLogic.GRID_SIZE; d++) {
            for (int i = center - d; i <= center + d; i++) {
                for (int j = center - d; j <= center + d; j++) {
                    if (Math.abs(i - center) == d || Math.abs(j - center) == d) {
                        if (isValidMove(i, j)) {
                            return new int[]{i, j};
                        }
                    }
                }
            }
        }
        return new int[]{0, 0};
    }

    public int[] findBlockingMove() {
        Set<String> whiteMoves = gameLogic.getWhiteMoves();

        // First check for direct winning threats (4 consecutive stones)
        for (String move : whiteMoves) {
            String[] coords = move.split(",");
            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);

            for (int[] dir : directions) {
                // Create an array to represent positions in this direction
                // -1=out of bounds/black, 0=empty, 1=white
                int[] line = new int[9]; // Center position at index 4
                line[4] = 1; // Current stone is white

                // Map the line in forward direction
                for (int i = 1; i <= 4; i++) {
                    int r = row + dir[0] * i;
                    int c = col + dir[1] * i;

                    if (r < 0 || r >= gameLogic.GRID_SIZE || c < 0 || c >= gameLogic.GRID_SIZE ||
                            gameLogic.getBlackMoves().contains(r + "," + c)) {
                        line[4+i] = -1; // Out of bounds or blocked
                    } else if (whiteMoves.contains(r + "," + c)) {
                        line[4+i] = 1; // White stone
                    } else {
                        line[4+i] = 0; // Empty
                    }
                }

                // Map the line in backward direction
                for (int i = 1; i <= 4; i++) {
                    int r = row - dir[0] * i;
                    int c = col - dir[1] * i;

                    if (r < 0 || r >= gameLogic.GRID_SIZE || c < 0 || c >= gameLogic.GRID_SIZE ||
                            gameLogic.getBlackMoves().contains(r + "," + c)) {
                        line[4-i] = -1; // Out of bounds or blocked
                    } else if (whiteMoves.contains(r + "," + c)) {
                        line[4-i] = 1; // White stone
                    } else {
                        line[4-i] = 0; // Empty
                    }
                }

                // Check for winning threats and return the position to block

                // Check for 4 consecutive stones with an open end: OOOO_
                for (int start = 0; start <= 4; start++) {
                    if (countConsecutive(line, start, 4) && line[start+4] == 0) {
                        // Calculate position to block
                        int blockPos = start + 4;
                        int blockRow = row + dir[0] * (blockPos - 4);
                        int blockCol = col + dir[1] * (blockPos - 4);
                        return new int[]{blockRow, blockCol};
                    }
                }

                // Check for reverse pattern: _OOOO
                for (int end = 8; end >= 4; end--) {
                    if (countConsecutive(line, end-3, -4) && line[end-4] == 0) {
                        // Calculate position to block
                        int blockPos = end - 4;
                        int blockRow = row + dir[0] * (blockPos - 4);
                        int blockCol = col + dir[1] * (blockPos - 4);
                        return new int[]{blockRow, blockCol};
                    }
                }

                // Check for split patterns, starting with OO_OO
                for (int start = 0; start <= 4; start++) {
                    // Need at least 5 positions from start
                    if (start + 4 >= line.length) continue;

                    // Count total white stones and empty spots in this 5-cell window
                    int whiteCount = 0;
                    int emptyPos = -1;

                    for (int i = 0; i < 5; i++) {
                        int pos = start + i;
                        if (line[pos] == 1) {
                            whiteCount++;
                        } else if (line[pos] == 0) {
                            // If we already found an empty spot, this might be a split pattern
                            if (emptyPos != -1) {
                                // More than one empty spot, not a direct threat
                                emptyPos = -1;
                                break;
                            }
                            emptyPos = pos;
                        } else {
                            // Blocked by edge or black stone
                            emptyPos = -1;
                            break;
                        }
                    }

                    // If we found exactly 4 white stones and 1 empty spot within 5 positions
                    if (whiteCount == 4 && emptyPos != -1) {
                        // Calculate the position to block
                        int offset = emptyPos - 4; // How far from center
                        int blockRow = row + dir[0] * offset;
                        int blockCol = col + dir[1] * offset;
                        return new int[]{blockRow, blockCol};
                    }
                }

                // Check for more complex patterns like _OO_OO_, OO__OO, etc.
                // These need a longer window (6-7 positions)
                for (int start = 0; start <= 2; start++) {
                    // Need at least 7 positions from start
                    if (start + 6 >= line.length) continue;

                    // Count total white stones and empty spots in 7-cell window
                    int whiteCount = 0;
                    int emptyCount = 0;
                    int firstEmptyPos = -1;

                    for (int i = 0; i < 7; i++) {
                        int pos = start + i;
                        if (line[pos] == 1) {
                            whiteCount++;
                        } else if (line[pos] == 0) {
                            if (firstEmptyPos == -1) {
                                firstEmptyPos = pos;
                            }
                            emptyCount++;
                        } else {
                            // Blocked
                            whiteCount = 0;
                            break;
                        }
                    }

                    // For patterns like _OO_OO_ with many stones but with gaps
                    if (whiteCount >= 4 && emptyCount > 0) {
                        // We need to identify the most critical gap to block

                        // First check if there's a middle gap between 2 stones on each side
                        for (int i = 1; i < 6; i++) {
                            int pos = start + i;
                            if (line[pos] == 0 &&
                                    countStonesInRange(line, start, pos-1) >= 2 &&
                                    countStonesInRange(line, pos+1, start+6) >= 2) {

                                // This is a critical gap like in OO_OO pattern
                                int offset = pos - 4; // How far from center
                                int blockRow = row + dir[0] * offset;
                                int blockCol = col + dir[1] * offset;
                                return new int[]{blockRow, blockCol};
                            }
                        }

                        // If no critical middle gap, block the first gap
                        int offset = firstEmptyPos - 4;
                        int blockRow = row + dir[0] * offset;
                        int blockCol = col + dir[1] * offset;
                        return new int[]{blockRow, blockCol};
                    }
                }
            }
        }
        return null;
    }

    // Helper method to count consecutive stones
    private boolean countConsecutive(int[] line, int start, int count) {
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                if (start + i >= line.length || line[start + i] != 1) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i > count; i--) {
                if (start + i < 0 || line[start + i] != 1) {
                    return false;
                }
            }
        }
        return true;
    }

    // Helper method to count stones in a range
    private int countStonesInRange(int[] line, int start, int end) {
        int count = 0;
        for (int i = start; i <= end; i++) {
            if (line[i] == 1) {
                count++;
            }
        }
        return count;
    }
    public int[] findNextMoveBlock() {
        Set<String> whiteMoves = gameLogic.getWhiteMoves();
        for (String move : whiteMoves) {
            String[] coords = move.split(",");
            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);
            for (int[] dir : directions) {
                int consecutiveCount = 1;
                int[] openSpots = new int[2];
                int openSpotCount = 0;
                for (int i = 1; i < 4; i++) {
                    int r = row + dir[0] * i;
                    int c = col + dir[1] * i;
                    if (r < 0 || r >= gameLogic.GRID_SIZE || c < 0 || c >= gameLogic.GRID_SIZE) {
                        break;
                    }
                    if (whiteMoves.contains(r + "," + c)) {
                        consecutiveCount++;
                    } else if (openSpotCount < 2 && !gameLogic.getBlackMoves().contains(r + "," + c)) {
                        openSpots[openSpotCount++] = r * 100 + c;
                        break;
                    } else {
                        break;
                    }
                }
                for (int i = 1; i < 4; i++) {
                    int r = row - dir[0] * i;
                    int c = col - dir[1] * i;
                    if (r < 0 || r >= gameLogic.GRID_SIZE || c < 0 || c >= gameLogic.GRID_SIZE) {
                        break;
                    }
                    if (whiteMoves.contains(r + "," + c)) {
                        consecutiveCount++;
                    } else if (openSpotCount < 2 && !gameLogic.getBlackMoves().contains(r + "," + c)) {
                        openSpots[openSpotCount++] = r * 100 + c;
                        break;
                    } else {
                        break;
                    }
                }
                if (consecutiveCount == 3 && openSpotCount == 2) {
                    int pos = openSpots[0];
                    return new int[] {pos / 100, pos % 100};
                }
            }
        }
        return null;
    }
    // SET<String> neighbors  ->all available neighbors!
    // for all blackpieces set : get the neighors that are VALID(EMPTY) and add to neighbors SET
    // for each of the neighbors set check best pattern !!! this is  the move to DO.
    private int[] findStrategicMove() {
        Set<String> neighborPositions = new HashSet<>();
        findNeighborPositions(gameLogic.getBlackMoves(), neighborPositions);
        int bestScore = -1;
        int[] bestMove = null;
        for (String position : neighborPositions) {
            String[] moves = position.split(",");
            int row = Integer.parseInt(moves[0]);
            int col = Integer.parseInt(moves[1]);
            int score = evaluateMove(row, col);
            if (score > bestScore) {
                bestScore = score;
                bestMove = new int[]{row, col};
            }
        }
        return bestMove != null ? bestMove : findRandomMove();
    }

    private void findNeighborPositions(Set<String> pieces, Set<String> neighborPositions) {
        for (String piece : pieces) {
            String[] moves = piece.split(",");
            int row = Integer.parseInt(moves[0]);
            int col = Integer.parseInt(moves[1]);
            for (int r = -1; r <= 1; r++) {
                for (int c = -1; c <= 1; c++) {
                    if (r == 0 && c == 0) continue;
                    int newRow = row + r;
                    int newCol = col + c;
                    if (isValidMove(newRow, newCol)) {
                        neighborPositions.add(newRow + "," + newCol);
                    }
                }
            }
        }
    }

    public int evaluateMove(int row, int col) {
        int score = 0;
        Set<String> blackMoves = new HashSet<>(gameLogic.getBlackMoves());
        blackMoves.add(row + "," + col);
        // score pattern creation
        if (wouldCreatePattern(row, col, blackMoves, "LiveFour")) score += 100;
        else if (wouldCreatePattern(row, col, blackMoves, "DeadFour")) score += 50;
        else if (wouldCreatePattern(row, col, blackMoves, "LiveThree")) score += 30;
        else if (wouldCreatePattern(row, col, blackMoves, "DeadThree")) score += 10;
        else if (wouldCreatePattern(row, col, blackMoves, "LiveTwo")) score += 5;

        // extra if closer to middle
        int center = gameLogic.GRID_SIZE / 2;
        int distanceFromCenter = Math.abs(row - center) + Math.abs(col - center);
        score += Math.max(0, 10 - distanceFromCenter);

        return score;
    }

    private boolean wouldCreatePattern(int row, int col, Set<String> playerMoves, String targetPattern) {
        Set<String> tempPlayerMoves = new HashSet<>(playerMoves);
        tempPlayerMoves.add(row + "," + col);
        Set<String> opponentMoves;
        if (tempPlayerMoves.containsAll(gameLogic.getBlackMoves())) {
            // attacking
            opponentMoves = new HashSet<>(gameLogic.getWhiteMoves());
        } else {
            // blocking
            opponentMoves = new HashSet<>(gameLogic.getBlackMoves());
        }
        return simulatePattern(row, col, tempPlayerMoves, opponentMoves, targetPattern);
    }
    private boolean simulatePattern(int row, int col, Set<String> playerMoves, Set<String> opponentMoves, String targetPattern) {
        for (int[] dir : directions) {
            int count = 1;
            boolean openStart = false, openEnd = false;

            // check positive direction
            for (int i = 1; i < 5; i++) {
                int r = row + dir[0] * i, c = col + dir[1] * i;
                if (r < 0 || r >= gameLogic.GRID_SIZE || c < 0 || c >= gameLogic.GRID_SIZE ||
                        !playerMoves.contains(r + "," + c)) {
                    openEnd = r >= 0 && r < gameLogic.GRID_SIZE && c >= 0 && c < gameLogic.GRID_SIZE &&
                            !playerMoves.contains(r + "," + c) && !opponentMoves.contains(r + "," + c);
                    break;
                }
                count++;
            } // check negative direction
            for (int i = 1; i < 5; i++) {
                int r = row - dir[0] * i, c = col - dir[1] * i;
                if (r < 0 || r >= gameLogic.GRID_SIZE || c < 0 || c >= gameLogic.GRID_SIZE ||
                        !playerMoves.contains(r + "," + c)) {
                    openStart = r >= 0 && r < gameLogic.GRID_SIZE && c >= 0 && c < gameLogic.GRID_SIZE &&
                            !playerMoves.contains(r + "," + c) && !opponentMoves.contains(r + "," + c);
                    break;
                }
                count++;
            }

            String pattern = null;
            if (count >= 5) pattern = "FiveInARow";
            else if (count == 4) pattern = (openStart && openEnd) ? "LiveFour" : "DeadFour";
            else if (count == 3) pattern = (openStart && openEnd) ? "LiveThree" : "DeadThree";
            else if (count == 2) pattern = (openStart && openEnd) ? "LiveTwo" : "DeadTwo";

            if (pattern != null && pattern.equals(targetPattern)) {
                return true;
            }
        }
        return false;
    }
    public int[] CloseBorderInteraction(int row, int col) { // check if player is near the border
        int[] result = new int[]{row, col};

        // corner checkers
        if (row <= 3 && col <= 3) {
            result[0] = row + directions[2][0];
            result[1] = col + directions[2][1];
        }
        else if (row <= 3 && col >= 9) {
            result[0] = row + directions[2][0];
            result[1] = col - directions[2][1];
        }
        else if (row >= 9 && col <= 3) {
            result[0] = row - directions[2][0];
            result[1] = col + directions[2][1];
        }
        else if (row >= 9 && col >= 9) {
            result[0] = row - directions[2][0];
            result[1] = col - directions[2][1];
        }
        // close border checkers but not corners
        else if (row <= 3) {
            result[0] = row + 1;
            result[1] = col;
        }
        else if (row >= 9) {
            result[0] = row - 1;
            result[1] = col;
        }
        else if (col <= 3) {
            result[0] = row;
            result[1] = col + 1;
        }
        else if (col >= 9) {
            result[0] = row;
            result[1] = col - 1;
        }
        if (!isValidMove(result[0], result[1])) {
            int center = gameLogic.GRID_SIZE / 2;
            if (isValidMove(center, center)) {
                return new int[]{center, center};
            }
            return findRandomMove();
        }

        return result;
    }
}