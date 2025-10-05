package com.mahjong.mahjongdesktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        AppNavigator.setStage(stage);
        AppNavigator.switchTo("login.fxml");
        stage.setTitle("Mahjong Client");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}