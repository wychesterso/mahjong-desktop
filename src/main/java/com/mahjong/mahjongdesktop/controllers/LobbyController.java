package com.mahjong.mahjongdesktop.controllers;

import com.mahjong.mahjongdesktop.AppState;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class LobbyController {

    @FXML
    private TableView<Room> roomsTable;

    @FXML
    private TableColumn<Room, String> roomIdColumn;

    @FXML
    private TableColumn<Room, String> hostColumn;

    @FXML
    private TableColumn<Room, String> playersColumn;

    @FXML
    private TableColumn<Room, Void> actionsColumn;

    private final ObservableList<Room> rooms = FXCollections.observableArrayList();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        // setup three columns for displaying list of rooms
        roomIdColumn.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        hostColumn.setCellValueFactory(new PropertyValueFactory<>("host"));
        playersColumn.setCellValueFactory(new PropertyValueFactory<>("playersDisplay"));

        addJoinButtonToTable();
        roomsTable.setItems(rooms);

        fetchRooms();
    }

    private void addJoinButtonToTable() {
        actionsColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Room, Void> call(final TableColumn<Room, Void> param) {
                return new TableCell<>() {
                    private final Button joinBtn = new Button("Join");

                    {
                        joinBtn.setOnAction(event -> {
                            Room room = getTableView().getItems().get(getIndex());
                            joinRoom(room.getRoomId());
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : joinBtn);
                    }
                };
            }
        });
    }

    @FXML
    public void createRoom() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/room/create");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Authorization", "Bearer " + AppState.getJwt());

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            response.append(line);
                        }
                        // parse response for roomId
                        var jsonNode = objectMapper.readTree(response.toString());
                        String newRoomId = jsonNode.get("roomId").asText();

                        Platform.runLater(() -> {
                            // navigate to room page (implement navigation logic)
                            System.out.println("Room created: " + newRoomId);
                            // TODO: navigateToRoom(newRoomId);
                        });
                    }
                } else {
                    System.err.println("Failed to create room: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    public void refreshRooms() {
        fetchRooms();
    }

    private void fetchRooms() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/room");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + AppState.getJwt());

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        List<Room> fetchedRooms = List.of(
                                objectMapper.readValue(in, Room[].class)
                        );
                        Platform.runLater(() -> rooms.setAll(fetchedRooms));
                    }
                } else {
                    System.err.println("Failed to fetch rooms: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void joinRoom(String roomId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/room/" + roomId + "/join?seat=SEAT1");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + AppState.getJwt());

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Platform.runLater(() -> {
                        System.out.println("Joined room: " + roomId);
                        // TODO: navigateToRoom(roomId);
                    });
                } else {
                    System.err.println("Failed to join room: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Room {
        private String roomId;
        private String host;
        private int availableSeats;

        public Room() {}

        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public int getAvailableSeats() { return availableSeats; }
        public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

        public String getPlayersDisplay() {
            int currentPlayers = 4 - availableSeats; // since backend gives empty seats
            return currentPlayers + "/4";
        }
    }
}