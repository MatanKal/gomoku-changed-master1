package com.example.gomoku;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GomokuApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GomokuApplication.class.getResource("Game-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 750 , 750);
        stage.setTitle("Gomoku game");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}