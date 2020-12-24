/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrabblegame.scrabble_game;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.IPAddress;
import java.net.Socket;
import java.net.SocketException;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;

/**
 * FXML Controller class
 *
 * @author Stephen He
 * @author Victor Ouy
 * @author Lucas
 */
public class SettingsController {

    private String server;
    private int serverPort;
    private Stage primaryStage;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="serverIp"
    private TextField serverIp; // Value injected by FXMLLoader

    @FXML // fx:id="portNumber"
    private TextField portNumber; // Value injected by FXMLLoader

    /**
     * Error message popup dialog
     *
     * @param msg
     */
    private void errorAlert(String msg) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle("ERROR");
        dialog.setHeaderText("INPUT ERROR");
        dialog.setContentText(msg);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();
    }

    /**
     * Sets the primary stage
     *
     * @param pStage
     */
    public void setStage(Stage pStage) {
        this.primaryStage = pStage;
    }

    /**
     * This method validates the user's inputs
     *
     * @param event
     */
    @FXML
    void startGame(ActionEvent event) throws IOException {
        try {
            if (isValidServer() && isValidPort()) {
                Socket socket = new Socket(server, serverPort);

                Stage closeStage = (Stage) serverIp.getScene().getWindow();
                closeStage.close();

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(App.class.getResource("/fxml/MenuScrabbleFXML.fxml"));

                AnchorPane anchor = (AnchorPane) loader.load();
                primaryStage.setTitle("Menu");

                MenuScrabbleFXMLController menuController = loader.getController();
                menuController.setProperties(primaryStage, socket);

                Scene scene = new Scene(anchor);
                primaryStage.setScene(scene);
                primaryStage.show();
            } else {
                errorAlert("IP address or port is invalid!");
            }
        } catch (SocketException badSocket) {
            errorAlert("IP address or port is invalid!");
        } catch (NumberFormatException nfe) {
            errorAlert("IP address or port is invalid!");
        } catch (IOException ioExp) {
            errorAlert("IP address or port is invalid!");
        }
    }

    /**
     * This method verifies if the text entered refers to a valid server Ip
     * address.
     *
     * @param serverName Text entered.
     * @return True if server is valid, false otherwise
     */
    private boolean isValidServer() {
        server = serverIp.getText().trim();
        if (IP_checker(server) != 1) {
            serverIp.clear();
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks the IP address inputted but user if it is valid
     *
     * @param hostName
     * @return int 1 or 2
     */
    private int IP_checker(String hostName) {
        int return_value = 0;
        if (hostName.equals("localhost")) {
            return_value = 1;
        } else {
            IPAddressString str = new IPAddressString(hostName);
            IPAddress addr = str.getAddress();
            if (addr != null) {
                return_value = 1;
            }
        }
        return return_value;
    }

    /**
     * Verify if text entered is valid. (No letters and such).
     *
     * @return True if port is valid, false otherwise.
     */
    private boolean isValidPort() {
        try {
            serverPort = Integer.parseInt(portNumber.getText().trim());
            return true;
        } catch (NumberFormatException nfe) {
            portNumber.clear();
            return false;
        }
    }

    /**
     * Initializes for settingsController
     */
    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert serverIp != null : "fx:id=\"serverIp\" was not injected: check your FXML file 'Settings.fxml'.";
        assert portNumber != null : "fx:id=\"portNumber\" was not injected: check your FXML file 'Settings.fxml'.";
    }
}
