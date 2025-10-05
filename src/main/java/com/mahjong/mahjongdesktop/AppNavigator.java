package com.mahjong.mahjongdesktop;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AppNavigator {
    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}