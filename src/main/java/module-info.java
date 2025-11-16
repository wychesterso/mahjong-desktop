module com.mahjong.mahjongdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires okhttp3;
    requires spring.messaging;
    requires spring.websocket;

    opens com.mahjong.mahjongdesktop.controllers to javafx.fxml, com.fasterxml.jackson.databind;
    opens com.mahjong.mahjongdesktop.dto.state to com.fasterxml.jackson.databind;
    opens com.mahjong.mahjongdesktop.dto.response to com.fasterxml.jackson.databind;
    
    exports com.mahjong.mahjongdesktop;
    exports com.mahjong.mahjongdesktop.controllers;
}