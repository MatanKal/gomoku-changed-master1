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
        }
        // a move that wins the game
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
        // 5 BLOCK PLAYER WIN NEXT TURN - PLAYER has "OPEN THREE"
        // without blocking - loss is guaranteed next turn
        int[] nextMoveBlock = findNextMoveBlock();
        if (nextMoveBlock != null) {
            return nextMoveBlock;
        }
        // 6 STRATEGIC MOVE
        int[] strategicMove = findStrategicMove();
        if (strategicMove != null) {
            return strategicMove;
        }
        return findRandomMove();
    }
    private boolean isValidMove(int row, int col) { // checker for outside bounderies and if the move is already made
        if (row < 0 || row >= gameLogic.GRID_SIZE || col < 0 || col >= gameLogic.GRID_SIZE) {
            return false;
        }
        String move = row + "," + col;
        return !gameLogic.getBlackMoves().contains(move) && !gameLogic.getWhiteMoves().contains(move);
    }
    private int[] findWinningMove() {
        Set<String> blackMoves = gameLogic.getBlackMoves();
        // first check for direct winning threats (4 consecutive stones)
        for (String move : blackMoves) {
            String[] moves = move.split(",");
            int row = Integer.parseInt(moves[0]);
            int col = Integer.parseInt(moves[1]);
            for (int[] dir : directions) {
                // create an array to represent positions in this direction
                // -1=out of bounds/white, 0=empty, 1=black
                int[] line = new int[9]; // center is at index 4
                line[4] = 1; // current stone is black
                for (int i = 1; i <= 4; i++) {//forward direction
                    int r = row + dir[0] * i;
                    int c = col + dir[1] * i;
                    if (r < 0 || r >= gameLogic.GRID_SIZE || c < 0 || c >= gameLogic.GRID_SIZE || gameLogic.getWhiteMoves().contains(r + "," + c))
                    {
                        line[4+i] = -1; // out of border or blocked
                    } else if (blackMoves.contains(r + "," + c)) {
                        line[4+i] = 1; // black!
                    } else {
                        line[4+i] = 0; // empty
                    }
                }
                for (int i = 1; i <= 4; i++) {//backward direction
                    int r = row - dir[0] * i;
                    int c = col - dir[1] * i;
                    if (r < 0 || r >= gameLogic.GRID_SIZE || c < 0 || c >= gameLogic.GRID_SIZE || gameLogic.getWhiteMoves().contains(r + "," + c))
                    {
                        line[4-i] = -1; // out of border or blocked
                    } else if (blackMoves.contains(r + "," + c)) {
                        line[4-i] = 1; // black
                    } else {
                        line[4-i] = 0; // empty
                    }
                }

                // check for a 4 pattern with an open end: OOOO_
                for (int start = 0; start <= 4; start++)
                {
                    if (countConsecutive(line, start, 4) && line[start+4] == 0) {//find the winning move
                        int winPos = start + 4;
                        int winRow = row + dir[0] * (winPos - 4);
                        int winCol = col + dir[1] * (winPos - 4);
                        return new int[]{winRow, winCol};
                    }
                }

                // check for 4 pattern with an open start: _OOOO
                for (int end = 8; end >= 4; end--) {
                    if (countConsecutive(line, end-3, -4) && line[end-4] == 0) {//find the winning move
                        int winPos = end - 4;
                        int winRow = row + dir[0] * (winPos - 4);
                        int winCol = col + dir[1] * (winPos - 4);
                        return new int[]{winRow, winCol};
                    }
                }
                for (int start = 0; start <= 4; start++) {// look for patterns with one gap like "OOO_O", "OO_OO", "O_OOO"
                    if (start + 4 >= line.length) continue;//count black and empty spots
                    int blackCount = 0;
                    int emptyPos = -1;
                    for (int i = 0; i < 5; i++) {
                        int pos = start + i;
                        if (line[pos] == 1) {
                            blackCount++;
                        } else if (line[pos] == 0) {//empty spot
                            if (emptyPos != -1) {//not a win yet- more than one empty spot
                                emptyPos = -1;
                                break;
                            }
                            emptyPos = pos;
                        } else {//blocked
                            emptyPos = -1;
                            break;
                        }
                    }
                    if (blackCount == 4 && emptyPos != -1) {// one empty spot in a 5 cell window
                        int offset = emptyPos - 4; // how far from center
                        int winRow = row + dir[0] * offset;
                        int winCol = col + dir[1] * offset;
                        return new int[]{winRow, winCol};
                    }
                }

                // _OO_OO_ patterns, hard to see and needs 7 cell window like "OO_OO_O", "O_OO_OO" etc.
                for (int start = 0; start <= 2; start++) {
                    if (start + 6 >= line.length) continue;
                    // count total black pieces and empty spots in 7 cell window
                    int blackCount = 0;
                    int emptyCount = 0;
                    int firstEmptyPos = -1;
                    for (int i = 0; i < 7; i++)
                    {// count pieces and gaps in a 7-position window
                        int pos = start + i;
                        if (line[pos] == 1) {
                            blackCount++;
                        } else if (line[pos] == 0) {
                            if (firstEmptyPos == -1) {
                                firstEmptyPos = pos;
                            }
                            emptyCount++;
                        } else {
                            // blocked
                            blackCount = 0;
                            break;
                        }
                    }
                    // for patterns with many stones but with gaps:
                    if (blackCount >= 4 && emptyCount > 0) {
                        // there is a pattern with a gap(gaps?)
                        // first check if there's a middle gap between 2 stones on each side
                        for (int i = 1; i < 6; i++) {
                            int pos = start + i;
                            if (line[pos] == 0 && countStonesInRange(line, start, pos-1) >= 2 && countStonesInRange(line, pos+1, start+6) >= 2)
                            {
                                int offset = pos - 4; // how far from center
                                int winRow = row + dir[0] * offset;
                                int winCol = col + dir[1] * offset;
                                return new int[]{winRow, winCol};
                            }
                        }
                        // if no critical middle gap, play the first gap
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

    private int[] findNextMoveWin() {
        Set<String> blackMoves = gameLogic.getBlackMoves();
        for (String move : blackMoves) {
            String[] moveS = move.split(",");
            int row = Integer.parseInt(moveS[0]);
            int col = Integer.parseInt(moveS[1]);
            for (int[] dir : directions) {// search for opened 3 in a row
                int consecutiveCount = 1;
                int[] openSpots = new int[2];
                int openSpotCount = 0;
                for (int i = 1; i < 4; i++) {// forward direction
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
                for (int i = 1; i < 4; i++) {// backward direction
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

    private int[] earlyGameMove() { // 2 first moves
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
        return new int[]{0, 0};
    }
    private int[] findBlockingMove() {
        Set<String> whiteMoves = gameLogic.getWhiteMoves();
        // first check for direct 4 in a row(4 consecutive pieces)
        for (String move : whiteMoves) {
            String[] moves = move.split(",");
            int row = Integer.parseInt(moves[0]);
            int col = Integer.parseInt(moves[1]);
            for (int[] dir : directions) {
                // create an array to represent positions in this direction
                // -1=out of border/black, 0=empty, 1=white
                int[] line = new int[9]; // center is at index 4
                line[4] = 1; // current piece is white

                // line = forward direction
                for (int i = 1; i <= 4; i++) {
                    int r = row + dir[0] * i;
                    int c = col + dir[1] * i;

                    if (r < 0 || r >= gameLogic.GRID_SIZE || c < 0 || c >= gameLogic.GRID_SIZE ||
                            gameLogic.getBlackMoves().contains(r + "," + c)) {
                        line[4+i] = -1; // out of border or blocked
                    } else if (whiteMoves.contains(r + "," + c)) {
                        line[4+i] = 1; // white!
                    } else {
                        line[4+i] = 0; // empty
                    }
                }

                // line = backward direction
                for (int i = 1; i <= 4; i++) {
                    int r = row - dir[0] * i;
                    int c = col - dir[1] * i;
                    if (r < 0 || r >= gameLogic.GRID_SIZE || c < 0 || c >= gameLogic.GRID_SIZE || gameLogic.getBlackMoves().contains(r + "," + c)) {
                        line[4-i] = -1; // out of border or blocked
                    }
                    else if (whiteMoves.contains(r + "," + c))
                    {
                        line[4-i] = 1; // white
                    }
                    else
                    {
                        line[4-i] = 0; // empty
                    }
                }

                // check for a 4 pattern with an open end: OOOO_
                for (int start = 0; start <= 4; start++) {
                    if (countConsecutive(line, start, 4) && line[start+4] == 0) { //find the blocking move
                        int blockPos = start + 4;
                        int blockRow = row + dir[0] * (blockPos - 4);
                        int blockCol = col + dir[1] * (blockPos - 4);
                        return new int[]{blockRow, blockCol};
                    }
                }
                // check for 4 pattern with an open start: _OOOO
                for (int end = 8; end >= 4; end--) {
                    if (countConsecutive(line, end-3, -4) && line[end-4] == 0) {//find the blocking move
                        int blockPos = end - 4;
                        int blockRow = row + dir[0] * (blockPos - 4);
                        int blockCol = col + dir[1] * (blockPos - 4);
                        return new int[]{blockRow, blockCol};
                    }
                }

                // look for patterns with one gap like OOO_O, OO_OO, O_OOO
                for (int start = 0; start <= 4; start++) {
                    if (start + 4 >= line.length) continue;//count white and empty spots
                    int whiteCount = 0;
                    int emptyPos = -1;
                    for (int i = 0; i < 5; i++) {
                        int pos = start + i;
                        if (line[pos] == 1) {
                            whiteCount++;
                        } else if (line[pos] == 0) {//empty spot
                            if (emptyPos != -1) {//not a threat yet- more than one empty spot
                                emptyPos = -1;
                                break;
                            }
                            emptyPos = pos;
                        } else {//blocked
                            emptyPos = -1;
                            break;
                        }
                    }

                    if (whiteCount == 4 && emptyPos != -1) {// one empty spot in a 5 cell window
                        int offset = emptyPos - 4; // how far from center
                        int blockRow = row + dir[0] * offset;
                        int blockCol = col + dir[1] * offset;
                        return new int[]{blockRow, blockCol};
                    }
                }

                // _OO_OO_ patterns, hard to see and needs 7 cell window like "OO_OO_O", "O_OO_OO" etc.
                for (int start = 0; start <= 2; start++) {
                    if (start + 6 >= line.length) continue;
                    // count total white pieces and empty spots in 7 cell window
                    int whiteCount = 0;
                    int emptyCount = 0;
                    int firstEmptyPos = -1;
                    for (int i = 0; i < 7; i++) {// count pieces and gaps in a 7-position window
                        int pos = start + i;
                        if (line[pos] == 1) {
                            whiteCount++;
                        } else if (line[pos] == 0) {
                            if (firstEmptyPos == -1) {
                                firstEmptyPos = pos;
                            }
                            emptyCount++;
                        } else {
                            // blocked
                            whiteCount = 0;
                            break;
                        }
                    }

                    // for patterns with many stones but with gaps:
                    if (whiteCount >= 4 && emptyCount > 0) {
                        // there is a pattern with a gap(gaps?)
                        // first check if there's a middle gap between 2 stones on each side
                        for (int i = 1; i < 6; i++) {
                            int pos = start + i;
                            if (line[pos] == 0 && countStonesInRange(line, start, pos-1) >= 2 &&
                                    countStonesInRange(line, pos+1, start+6) >= 2) {
                                int offset = pos - 4; // how far from center
                                int blockRow = row + dir[0] * offset;
                                int blockCol = col + dir[1] * offset;
                                return new int[]{blockRow, blockCol};
                            }
                        }
                        // if no critical middle gap, block the first gap
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

    // helper function to count consecutive stones
    private boolean countConsecutive(int[] line, int start, int count) {
        if (count > 0) {// casees like OOOO_
            for (int i = 0; i < count; i++) {
                if (start + i >= line.length || line[start + i] != 1) {
                    return false;
                }
            }
        } else {// for cases like _OOOO to go backwards
            for (int i = 0; i > count; i--) {
                if (start + i < 0 || line[start + i] != 1) {
                    return false;
                }
            }
        }
        return true;
    }

    // helper function to count stones in a range
    private int countStonesInRange(int[] line, int start, int end) {
        int count = 0;
        for (int i = start; i <= end; i++) {
            if (line[i] == 1) {
                count++;
            }
        }
        return count;
    }
    private int[] findNextMoveBlock() {
        Set<String> whiteMoves = gameLogic.getWhiteMoves();
        for (String move : whiteMoves) {
            String[] coords = move.split(",");
            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);
            for (int[] dir : directions) {
                int consecutiveCount = 1;
                int[] openSpots = new int[2];
                int openSpotCount = 0;
                for (int i = 1; i < 4; i++) {// forward direction
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
                for (int i = 1; i < 4; i++) {// backward direction
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
    // for each of the neighbors set check best pattern
    private int[] findStrategicMove() {
        Set<String> neighborPositions = new HashSet<>();
        findNeighborPositions(gameLogic.getBlackMoves(), neighborPositions);
        int[] bestMove = bestMove(neighborPositions);
        return bestMove != null ? bestMove : findRandomMove();
    }

    private void findNeighborPositions(Set<String> pieces, Set<String> neighborPositions) {
        for (String piece : pieces) {
            String[] moves = piece.split(",");
            int row = Integer.parseInt(moves[0]);
            int col = Integer.parseInt(moves[1]);
            int newRow, newCol;
            for (int[] dir : directions) {
                newRow = row + dir[0];
                newCol = col + dir[1];
                if (isValidMove(newRow, newCol)) {
                    neighborPositions.add(newRow + "," + newCol);
                }
                newRow = row - dir[0];
                newCol = col - dir[1];
                if (isValidMove(newRow, newCol)) {
                    neighborPositions.add(newRow + "," + newCol);
                }
            }
        }
    }

    private int[] bestMove(Set<String> neighborPositions) {
        Set<String> blackMoves = new HashSet<>(gameLogic.getBlackMoves());
        int[] bestMove = null;
        String bestPattern = null;
        for (String position : neighborPositions) {
            String[] moves = position.split(",");
            int moveRow = Integer.parseInt(moves[0]);
            int moveCol = Integer.parseInt(moves[1]);
            String currentPattern = null;
            if (wouldCreatePattern(moveRow, moveCol, blackMoves, "DeadFour")) currentPattern = "DeadFour";
            else if (wouldCreatePattern(moveRow, moveCol, blackMoves, "LiveThree")) currentPattern = "LiveThree";
            else if (wouldCreatePattern(moveRow, moveCol, blackMoves, "DeadThree")) currentPattern = "DeadThree";
            else if (wouldCreatePattern(moveRow, moveCol, blackMoves, "LiveTwo")) currentPattern = "LiveTwo";
            else if (wouldCreatePattern(moveRow, moveCol, blackMoves, "DeadTwo")) currentPattern = "DeadTwo";

            if (currentPattern != null && (bestPattern == null || gameLogic.isBetterPattern(currentPattern, bestPattern))) {
                bestPattern = currentPattern;
                bestMove = new int[]{moveRow, moveCol};
            }
        }
        return bestMove;
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
    private int[] CloseBorderInteraction(int row, int col) { // check if player is near the border
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