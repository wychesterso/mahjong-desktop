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
import javafx.scene.layout.HBox;

public class RoomController {

    private static final List<String> SEATS = List.of("EAST", "SOUTH", "WEST", "NORTH");

    @FXML private Label roomTitleLabel;
    @FXML private Label hostLabel;
    @FXML private Label seatsLabel;
    @FXML private TableView<PlayerRow> playersTable;
    @FXML private TableColumn<PlayerRow, String> seatColumn;
    @FXML private TableColumn<PlayerRow, String> playerNameColumn;
    @FXML private TableColumn<PlayerRow, String> botColumn;
    @FXML private TableColumn<PlayerRow, Void> actionColumn;
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
        setupActionColumn();

        playersTable.setRowFactory(tv -> {
            TableRow<PlayerRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    PlayerRow clicked = row.getItem();
                    if ("Vacant".equals(clicked.getPlayerName())) {
                        confirmSeatSwitch(clicked.getSeat());
                    }
                }
            });
            return row;
        });

        playerNameColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Vacant")) {
                        setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        loadRoomInfo();
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button addBotBtn = new Button("Add Bot");
            private final Button kickBtn = new Button("Kick");
            private final Button transferBtn = new Button("Transfer Host");

            {
                addBotBtn.setOnAction(e -> {
                    PlayerRow row = getTableView().getItems().get(getIndex());
                    handleAddBot(row.getSeat());
                });

                kickBtn.setOnAction(e -> {
                    PlayerRow row = getTableView().getItems().get(getIndex());
                    handleKick(row.getSeat());
                });

                transferBtn.setOnAction(e -> {
                    PlayerRow row = getTableView().getItems().get(getIndex());
                    handleTransferHost(row.getSeat());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                PlayerRow row = getTableView().getItems().get(getIndex());
                String currentUser = AppState.getUserId();
                String hostId = AppState.getCurrentHostId();

                // user is not host OR row is user - no buttons
                if (!Objects.equals(currentUser, hostId) || row.getPlayerName().equals(AppState.getUserId())) {
                    setGraphic(null);
                    return;
                }

                HBox box = new HBox(6);

                if ("Vacant".equals(row.getPlayerName())) {
                    // vacant seat - can add bot
                    box.getChildren().add(addBotBtn);
                } else {
                    boolean isBot = row.getBotStatus().contains("🤖");
                    if (isBot) {
                        // bot occupied - can kick
                        box.getChildren().add(kickBtn);
                    } else {
                        // human occupied - can kick or transfer host
                        box.getChildren().addAll(kickBtn, transferBtn);
                    }
                }

                setGraphic(box);
            }
        });
    }

    private void handleAddBot(String seat) {
        String roomId = AppState.getCurrentRoomId();
        if (roomId == null) return;

        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/room/" + roomId + "/add-bot?seat=" + seat);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + AppState.getJwt());
                conn.setDoOutput(true);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Platform.runLater(this::loadRoomInfo);
                } else {
                    Platform.runLater(() -> showError("Failed to add bot. (" + responseCode + ")"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Error adding bot."));
            }
        }).start();
    }

    private void handleKick(String seat) {

    }

    private void handleTransferHost(String seat) {

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

                AppState.setCurrentHostId(dto.getHostId());

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

        // display seats in order
        for (String seat : SEATS) {
            String playerName = dto.getPlayerNames().get(seat);
            Boolean bot = dto.getBotStatuses().get(seat);

            if (playerName == null) {
                players.add(new PlayerRow(seat, "Vacant", "—"));
            } else {
                String type = (bot != null && bot) ? "🤖 Bot" : "🙎 Human";
                players.add(new PlayerRow(seat, playerName, type));
            }
        }
    }

    @FXML
    private void refreshRoom() {
        loadRoomInfo();
    }

    @FXML
    private void backToLobby() {
        AppNavigator.switchTo("lobby.fxml");
    }

    private void confirmSeatSwitch(String seat) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Switch Seat");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to move to the " + seat + " seat?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            switchSeat(seat);
        }
    }

    private void switchSeat(String seat) {
        String roomId = AppState.getCurrentRoomId();
        if (roomId == null) return;

        new Thread(() -> {
            try {
                URL url = new URL("http://localhost:8080/room/" + roomId + "/seat?newSeat=" + seat);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + AppState.getJwt());
                conn.setDoOutput(true);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Platform.runLater(() -> {
                        loadRoomInfo(); // refresh after switching
                    });
                } else if (responseCode == 401) {
                    Platform.runLater(() -> showError("Unauthorized. Please log in again."));
                } else {
                    Platform.runLater(() -> showError("Failed to switch seat. (" + responseCode + ")"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Error switching seat."));
            }
        }).start();
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
                conn.setRequestProperty("Authorization", "Bearer " + AppState.getJwt());
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