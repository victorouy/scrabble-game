module com.scrabblegame.scrabble_game {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens com.scrabblegame.scrabble_game to javafx.fxml;
    exports com.scrabblegame.scrabble_game;
    requires inet.ipaddr;
    requires org.slf4j;
    requires diction;
}
