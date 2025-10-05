module com.mahjong.mahjongdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires okhttp3;


    opens com.mahjong.mahjongdesktop to javafx.fxml;
    exports com.mahjong.mahjongdesktop;
    exports com.mahjong.mahjongdesktop.controllers;
    opens com.mahjong.mahjongdesktop.controllers to javafx.fxml;
}