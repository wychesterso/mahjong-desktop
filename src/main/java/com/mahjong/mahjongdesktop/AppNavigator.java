package com.mahjong.mahjongdesktop;

import com.mahjong.mahjongdesktop.controllers.CleanupAware;
import com.mahjong.mahjongdesktop.controllers.MainController;

public class AppNavigator {
    private static MainController mainController;

    public static void setMainController(MainController controller) {
        mainController = controller;
    }

    public static void switchTo(String fxml) {
        if (mainController != null) {

            // clean up old controller before switching
            Object oldChild = mainController.getCurrentChildController();
            if (oldChild instanceof CleanupAware cleanupable) {
                cleanupable.cleanup();
            }

            javafx.application.Platform.runLater(() -> mainController.loadView(fxml));
        }
    }
}