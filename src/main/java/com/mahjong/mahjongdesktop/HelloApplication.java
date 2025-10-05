package com.mahjong.mahjongdesktop;

import com.mahjong.mahjongdesktop.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mahjong/mahjongdesktop/main.fxml"));
        Scene scene = new Scene(loader.load());

        MainController mainController = loader.getController();
        AppNavigator.setMainController(mainController);

        stage.setScene(scene);
        stage.setTitle("Mahjong Client");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}