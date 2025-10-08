package com.mahjong.mahjongdesktop;

public class AppState {
    private static String jwt;
    private static String currentRoomId;

    public static String getJwt() {
        return jwt;
    }

    public static void setJwt(String token) {
        jwt = token;
    }

    public static String getCurrentRoomId() {
        return currentRoomId;
    }

    public static void setCurrentRoomId(String roomId) {
        currentRoomId = roomId;
    }

    public static void clear() {
        jwt = null;
        currentRoomId = null;
    }
}