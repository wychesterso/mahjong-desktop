package com.mahjong.mahjongdesktop.controllers;

import com.mahjong.mahjongdesktop.AppState;
import com.mahjong.mahjongdesktop.AppNavigator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RoomController {

    @FXML private Label roomTitleLabel;
    @FXML private Label hostLabel;
    @FXML private Label seatsLabel;
    @FXML private TableView<PlayerRow> playersTable;
    @FXML private TableColumn<PlayerRow, String> seatColumn;
    @FXML private TableColumn<PlayerRow, String> playerNameColumn;
    @FXML private TableColumn<PlayerRow, String> botColumn;
    @FXML private Button refreshButton;
    @FXML private Button leaveButton;

    private final ObservableList<PlayerRow> players = FXCollections.observableArrayList();
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    public void initialize() {
        seatColumn.setCellValueFactory(new PropertyValueFactory<>("seat"));
        playerNameColumn.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        botColumn.setCellValueFactory(new PropertyValueFactory<>("botStatus"));
        playersTable.setItems(players);

        loadRoomInfo();
    }

    private void loadRoomInfo() {
        String roomId = AppState.getCurrentRoomId();
        if (roomId == null) {
            showError("No room selected. Returning to lobby.");
            AppNavigator.switchTo("lobby.fxml");
            return;
        }

        roomTitleLabel.setText("Room #" + roomId);

        new Thread(() -> {
            try {
                RoomInfoDTO dto = fetchRoomInfo(roomId);
                if (dto == null) {
                    Platform.runLater(() -> {
                        showError("Room not found. Returning to lobby.");
                        AppNavigator.switchTo("lobby.fxml");
                    });
                    return;
                }

                Platform.runLater(() -> updateUI(dto));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        showError("Failed to load room info. Check connection.")
                );
            }
        }).start();
    }

    private RoomInfoDTO fetchRoomInfo(String roomId) throws IOException {
        URL url = new URL("http://localhost:8080/room/" + roomId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + AppState.getJwt());

        if (conn.getResponseCode() != 200) return null;

        return mapper.readValue(conn.getInputStream(), RoomInfoDTO.class);
    }

    private void updateUI(RoomInfoDTO dto) {
        hostLabel.setText("Host: " + dto.getHostId());
        seatsLabel.setText("Available seats: " + dto.getNumAvailableSeats());

        players.clear();

        // merge player and bot info
        for (Map.Entry<String, String> entry : dto.getPlayerNames().entrySet()) {
            String seat = entry.getKey();
            String playerName = entry.getValue();
            boolean isBot = dto.getBotStatuses().getOrDefault(seat, false);
            players.add(new PlayerRow(seat, playerName, isBot ? "🤖 Bot" : "🙎 Human"));
        }
    }

    @FXML
    private void refreshRoom() {
        loadRoomInfo();
    }

    @FXML
    private void leaveRoom() {
        String roomId = AppState.getCurrentRoomId();
        if (roomId == null) return;

        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/room/" + roomId + "/exit");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.getResponseCode(); // trigger request
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.runLater(() -> {
                    AppState.setCurrentRoomId(null);
                    AppNavigator.switchTo("lobby.fxml");
                });
            }
        }).start();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    //========== INNER DATA CLASSES ==========//

    public static class PlayerRow {
        private final String seat;
        private final String playerName;
        private final String botStatus;

        public PlayerRow(String seat, String playerName, String botStatus) {
            this.seat = seat;
            this.playerName = playerName;
            this.botStatus = botStatus;
        }

        public String getSeat() { return seat; }
        public String getPlayerName() { return playerName; }
        public String getBotStatus() { return botStatus; }
    }

    // mirror the backend DTO
    public static class RoomInfoDTO {
        private String roomId;
        private String hostId;
        private int numAvailableSeats;
        private Map<String, String> playerNames;
        private Map<String, Boolean> botStatuses;

        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }

        public String getHostId() { return hostId; }
        public void setHostId(String hostId) { this.hostId = hostId; }

        public int getNumAvailableSeats() { return numAvailableSeats; }
        public void setNumAvailableSeats(int numAvailableSeats) { this.numAvailableSeats = numAvailableSeats; }

        public Map<String, String> getPlayerNames() { return playerNames; }
        public void setPlayerNames(Map<String, String> playerNames) { this.playerNames = playerNames; }

        public Map<String, Boolean> getBotStatuses() { return botStatuses; }
        public void setBotStatuses(Map<String, Boolean> botStatuses) { this.botStatuses = botStatuses; }
    }
}