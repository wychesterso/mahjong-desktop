package com.mahjong.mahjongdesktop.controllers;

import com.mahjong.mahjongdesktop.AppNavigator;
import com.mahjong.mahjongdesktop.AppState;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import okhttp3.*;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final OkHttpClient client = new OkHttpClient();

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
        Request request = new Request.Builder()
                .url("http://localhost:8080/auth/login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                showError("Connection failed");
            }

            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jwt = response.body().string();
                    AppState.setJwt(jwt);  // store JWT
                    javafx.application.Platform.runLater(() -> {
                        AppNavigator.switchTo("lobby.fxml");
                    }); // navigate to lobby
                } else {
                    showError("Login failed: " + response.code());
                }
            }
        });
    }

    @FXML
    private void handleGoToRegister() {
        AppNavigator.switchTo("register.fxml");
    }

    private void showError(String message) {
        javafx.application.Platform.runLater(() -> errorLabel.setText(message));
    }
}