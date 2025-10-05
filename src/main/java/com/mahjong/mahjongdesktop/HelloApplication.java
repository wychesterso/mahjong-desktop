package com.mahjong.mahjongdesktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        AppNavigator.setStage(stage);

        if (AppState.getJwt() != null) {
            AppNavigator.switchTo("lobby.fxml");
        } else {
            AppNavigator.switchTo("login.fxml");
        }

        stage.setTitle("Mahjong Client");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}