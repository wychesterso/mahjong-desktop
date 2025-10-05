package com.mahjong.mahjongdesktop;

import com.mahjong.mahjongdesktop.controllers.MainController;

public class AppNavigator {
    private static MainController mainController;

    public static void setMainController(MainController controller) {
        mainController = controller;
    }

    public static void switchTo(String fxml) {
        if (mainController != null) {
            javafx.application.Platform.runLater(() -> mainController.loadView(fxml));
        }
    }
}