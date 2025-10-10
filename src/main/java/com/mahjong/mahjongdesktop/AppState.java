package com.mahjong.mahjongdesktop;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;

public class AppState {
    private static String jwt;
    private static String userId;
    private static String currentRoomId;
    private static String currentHostId;

    public static String getJwt() {
        return jwt;
    }

    public static void setJwt(String token) {
        jwt = token;
        userId = decodeUserId(token);
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

    public static void clear() {
        jwt = null;
        currentRoomId = null;
    }
}