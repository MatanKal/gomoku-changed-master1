package com.example.gomoku;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert;

import java.io.Console;

public class GameController {
    @FXML
    private GridPane gridPane;

    private static final int GRID_SIZE = 13;
    private static final double CELL_SIZE = 39;
    private gameLogic logic = new gameLogic();
    private ailogic ai = new ailogic(logic);

    @FXML
    public ImageView boardImage;

    @FXML
    public void initialize() {
        Image image = new Image(getClass().getResourceAsStream("board.jpg"));
        boardImage.setImage(image);
        setupBoard();
        if (logic.isBlackTurn()) {
            makeAIMove();
        }
    }
    private void setupBoard() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setStyle("-fx-border-color: transparent;");
                final int r = row;
                final int c = col;
                cell.setOnMouseClicked(event -> placePiece(event, r, c));
                gridPane.add(cell, col, row);
            }
        }
    }

    private void placePiece(MouseEvent event, int row, int col) {
        // return if move is invalid
        if (!logic.placePiece(row, col)) {
            return;
        }

        // update look of the cell
        StackPane cell = (StackPane) gridPane.getChildren().get(row * GRID_SIZE + col);
        Circle piece = new Circle(12);
        piece.setFill(Color.WHITE);
        cell.getChildren().add(piece);
        logic.SetLastMove(row, col);

        // print pattern found
        String stateFound = logic.StatePosition(row, col);
        if (stateFound != null) {
            System.out.println("Player created: " + stateFound);
        }

        // print winner
        char winner = logic.checkWin(row, col);
        if (winner == 'W' || winner == 'B') {
            showWinnerAlert(winner == 'B' ? 1 : 2);
           // resetGame();
            return;
        }

        // change turns
        logic.changeTurn();
        makeAIMove();
    }

    private void makeAIMove() {
        int[] aiMove = ai.aiMove();

        if (!logic.placePiece(aiMove[0], aiMove[1])) {
            System.err.println("AI attempted invalid move: " + aiMove[0] + "," + aiMove[1]);
            return;
        }

        logic.SetLastMove(aiMove[0], aiMove[1]);

        StackPane cell = (StackPane) gridPane.getChildren().get(aiMove[0] * GRID_SIZE + aiMove[1]);
        Circle piece = new Circle(12);
        piece.setFill(Color.BLACK);
        cell.getChildren().add(piece);
        String stateFound = logic.StatePosition(aiMove[0], aiMove[1]);
        if (stateFound != null) {
            System.out.println("AI created: " + stateFound);
        }

        char winner = logic.checkWin(aiMove[0], aiMove[1]);
        if (winner == 'W' || winner == 'B') {
            showWinnerAlert(winner == 'B' ? 1 : 2);
           // resetGame();
            return;
        }

        logic.changeTurn();
    }

    private void showWinnerAlert(int winner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText((winner == 1 ? "Black" : "White") + " wins!");
        alert.showAndWait();
    }

   /* private void resetGame() {
        gridPane.getChildren().clear();
        logic = new gameLogic();
        initialize();
    }*/
}
