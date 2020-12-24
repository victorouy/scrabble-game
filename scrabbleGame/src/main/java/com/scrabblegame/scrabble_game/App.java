package com.scrabblegame.scrabble_game;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

/**
 * Main class of client side. Displays controller settings
 *
 * @author Stephen He
 * @author Victor Ouy
 * @author Lucas
 */
public class App extends Application {

    private Stage primaryStage;
    private Parent settingsPane;
    
    /**
     * This method starts the game by displaying the configuration form so users can connect to a server.
     * It then displays the game board to the user.
     * @param stage
     * @throws IOException 
     */
    @Override
    public void start(Stage stage) throws IOException {
        
        this.primaryStage = stage;
        this.primaryStage.setTitle("Scrabble Game");
        
        this.initSettingsController();
        
        Scene scene = new Scene(settingsPane);
        scene.getStylesheets().add(getClass().getResource("/styles/settings.css").toExternalForm());
        this.primaryStage.setScene(scene);
        this.primaryStage.show();
    }
    
    /**
     * This method initializes the configuration form for users. It does so by fetching the proper fxml file from resources.
     */
    private void initSettingsController(){
        
        try{
            // Instantiate a FXMLLoader object
            FXMLLoader loader = new FXMLLoader();
            
            // Connect the FXMLLoader to the fxml file that is stored in the jar
            loader.setLocation(App.class
                    .getResource("/fxml/Settings.fxml"));
            
            settingsPane = (BorderPane) loader.load();
            
            SettingsController rootController = loader.getController();
            rootController.setStage(primaryStage);
        } 
        catch (IOException ex) {
            errorAlert(ex.getMessage());
        }
    }
    
    /**
     * Error message popup dialog
     *
     * @param msg
     */
    private void errorAlert(String msg) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle("Error");
        dialog.setHeaderText("Error");
        dialog.setContentText(msg);
        dialog.show();
    }
    
    public static void main(String[] args) {
        launch();
    }
}