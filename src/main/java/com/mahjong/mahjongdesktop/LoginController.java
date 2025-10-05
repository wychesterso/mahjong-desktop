package com.mahjong.mahjongdesktop;

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
                    AppState.setJwt(jwt);  // Store JWT
                    AppNavigator.switchTo("lobby.fxml"); // Show next screen
                } else {
                    showError("Login failed: " + response.code());
                }
            }
        });
    }

    private void showError(String message) {
        javafx.application.Platform.runLater(() -> errorLabel.setText(message));
    }
}