package com.mahjong.mahjongdesktop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahjong.mahjongdesktop.network.GameMessageHandler;
import com.mahjong.mahjongdesktop.network.GameSocketClient;

import java.util.Base64;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class AppState {
    private static String jwt;
    private static String userId;
    private static String currentRoomId;
    private static String currentHostId;

    private static GameSocketClient gameSocketClient;
    private static GameMessageHandler gameMessageHandler;

    private static final BooleanProperty loggedIn = new SimpleBooleanProperty(false);

    public static BooleanProperty loggedInProperty() {
        return loggedIn;
    }

    public static boolean isLoggedIn() {
        return loggedIn.get();
    }

    public static String getJwt() {
        return jwt;
    }

    public static void setJwt(String token) {
        jwt = token;
        userId = decodeUserId(token);
        loggedIn.set(token != null);
    }

    private static String decodeUserId(String jwt) {
        try {
            String[] parts = jwt.split("\\."); // header.payload.signature
            if (parts.length < 2) return null;

            String payload = parts[1];
            String json = new String(Base64.getUrlDecoder().decode(payload));

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> claims = mapper.readValue(json, Map.class);

            return claims.get("sub").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getUserId() {
        return userId;
    }

    public static String getCurrentRoomId() {
        return currentRoomId;
    }

    public static void setCurrentRoomId(String roomId) {
        currentRoomId = roomId;
    }

    public static String getCurrentHostId() {
        return currentHostId;
    }

    public static void setCurrentHostId(String hostId) {
        currentHostId = hostId;
    }

    public static GameSocketClient getGameSocketClient() {
        return gameSocketClient;
    }

    public static void setGameSocketClient(GameSocketClient gameSocketClient) {
        AppState.gameSocketClient = gameSocketClient;
    }

    public static GameMessageHandler getGameMessageHandler() {
        return gameMessageHandler;
    }

    public static void setGameMessageHandler(GameMessageHandler gameMessageHandler) {
        AppState.gameMessageHandler = gameMessageHandler;
    }

    public static void clear() {
        jwt = null;
        userId = null;
        currentRoomId = null;
        currentHostId = null;
        gameSocketClient = null;
        gameMessageHandler = null;

        loggedIn.set(false);
    }
}