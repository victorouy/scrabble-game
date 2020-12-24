package com.scrabblegame.scrabble_game;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Stephen He
 * @author Victor Ouy
 * @author Lucas
 */
public class MenuScrabbleFXMLController {

    private Stage primaryStage;
    private Socket socket;
    private ScrabbleBoardController boardController;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
    private Label titleMenu;

    @FXML
    private CheckBox checkBoxStart;

    /**
     * Closes the application
     *
     * @param event
     */
    @FXML
    void onExit(ActionEvent event) {
        
        Platform.exit();
    }

    /**
     * When click button Play Now, will launch scrabble board fxml and send its
     * properties to be set up
     *
     * @param event
     * @throws IOException
     */
    @FXML
    void onPlay(ActionEvent event) throws IOException {
        try {
            Stage closeStage = (Stage) titleMenu.getScene().getWindow();
            closeStage.close();
            FXMLLoader loader = new FXMLLoader();

            // Connect the FXMLLoader to the fxml file that is stored in the jar
            loader.setLocation(App.class
                    .getResource("/fxml/scrabble_board.fxml"));

            GridPane boardPane = (GridPane) loader.load();

            boardController = loader.getController();
            boardController.setFirstMove(checkBoxStart.isSelected());
            boardController.setStart(this.socket);

            Scene scene = new Scene(boardPane);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException ex) {
            errorAlert(ex.getMessage());
        }
    }

    /**
     * Sets up properties
     *
     * @param primaryStage
     * @param serverIp
     * @param serverPort
     */
    public void setProperties(Stage primaryStage, Socket socket) {
        this.primaryStage = primaryStage;
        this.socket = socket;
    }

    /**
     * Error message popup dialog
     *
     * @param msg
     */
    private void errorAlert(String msg) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle("ERROR");
        dialog.setHeaderText("ERROR LOADING BOARD");
        dialog.setContentText(msg);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();
    }
}
