module com.mahjong.mahjongdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires okhttp3;

    opens com.mahjong.mahjongdesktop.controllers to javafx.fxml, com.fasterxml.jackson.databind;
    exports com.mahjong.mahjongdesktop;
    exports com.mahjong.mahjongdesktop.controllers;
}