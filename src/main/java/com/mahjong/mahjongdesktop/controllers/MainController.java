package com.mahjong.mahjongdesktop.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainController {

    private Object currentChildController;

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        if (com.mahjong.mahjongdesktop.AppState.getJwt() == null) {
            loadView("login.fxml");
        } else {
            loadView("lobby.fxml");
        }
    }

    @FXML
    public void goToLobby() {
        loadView("lobby.fxml");
    }

    @FXML
    public void goToProfile() {
        loadView("profile.fxml");
    }

    @FXML
    public void logout() {
        com.mahjong.mahjongdesktop.AppState.clear();
        loadView("login.fxml");
    }

    public Object getCurrentChildController() {
        return currentChildController;
    }

    public void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mahjong/mahjongdesktop/" + fxml)
            );

            Node view = loader.load();
            currentChildController = loader.getController();

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}