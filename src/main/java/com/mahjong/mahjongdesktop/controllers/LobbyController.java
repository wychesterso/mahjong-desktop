package com.mahjong.mahjongdesktop.controllers;

import com.mahjong.mahjongdesktop.AppNavigator;
import com.mahjong.mahjongdesktop.AppState;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
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

        refreshRooms();
    }

    private void addJoinButtonToTable() {
        actionsColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Room, Void> call(final TableColumn<Room, Void> param) {
                return new TableCell<>() {

                    private final Button joinBtn = new Button("Join");
                    private final Button viewBtn = new Button("View");
                    private final Button rejoinBtn = new Button("Rejoin");
                    private final HBox buttonBox = new HBox(5);

                    {
                        // JOIN
                        joinBtn.setOnAction(event -> {
                            Room room = getTableView().getItems().get(getIndex());
                            joinRoom(room.getRoomId());
                        });

                        // VIEW
                        viewBtn.setOnAction(event -> {
                            Room room = getTableView().getItems().get(getIndex());
                            navigateToRoom(room.getRoomId());
                        });

                        // REJOIN
                        rejoinBtn.setOnAction(event -> {
                            Room room = getTableView().getItems().get(getIndex());
                            navigateToRoom(room.getRoomId());
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                            return;
                        }

                        Room room = getTableView().getItems().get(getIndex());
                        String currentRoomId = AppState.getCurrentRoomId();
                        buttonBox.getChildren().clear();

                        if (currentRoomId == null) {
                            // user not in any room → show Join + View
                            buttonBox.getChildren().addAll(joinBtn, viewBtn);
                        } else if (currentRoomId.equals(room.getRoomId())) {
                            // user in this room → show Rejoin
                            buttonBox.getChildren().add(rejoinBtn);
                        } else {
                            // user in another room → show View
                            buttonBox.getChildren().add(viewBtn);
                        }

                        setGraphic(buttonBox);
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
                            refreshRooms();
                            navigateToRoom(newRoomId);
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
        fetchCurrentRoom();
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

    private void fetchCurrentRoom() {
        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/room/current-room");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + AppState.getJwt());

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String roomId = in.readLine();
                        Platform.runLater(() -> {
                            AppState.setCurrentRoomId(roomId);
                            roomsTable.refresh();
                        });
                    }
                } else if (responseCode == 204) { // not in a room
                    Platform.runLater(() -> {
                        AppState.setCurrentRoomId(null);
                        roomsTable.refresh();
                    });
                } else {
                    System.err.println("Failed to fetch current room: " + responseCode);
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
                URL url = new URL("http://localhost:8080/room/" + roomId + "/join");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + AppState.getJwt());

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Platform.runLater(() -> {
                        AppState.setCurrentRoomId(roomId);
                        System.out.println("Joined room: " + roomId);
                        refreshRooms();
                        navigateToRoom(roomId);
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

    private void navigateToRoom(String roomId) {
        // System.out.println("Navigating to room page: " + roomId);
        Platform.runLater(() -> {
            AppState.setCurrentRoomId(roomId);
            AppNavigator.switchTo("room.fxml");
        });
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