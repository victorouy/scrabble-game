package com.scrabblegame.scrabble_game;

import com.scrabblegame.data.GameState;
import com.scrabblegame.data.TileBean;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Stephen He
 * @author Victor Ouy
 * @author Lucas
 */
public class RackTileSwapController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    
    @FXML // fx:id="playerTile0"
    private ImageView playerTile0; // Value injected by FXMLLoader

    @FXML // fx:id="playerTile1"
    private ImageView playerTile1; // Value injected by FXMLLoader

    @FXML // fx:id="playerTile2"
    private ImageView playerTile2; // Value injected by FXMLLoader

    @FXML // fx:id="playerTile3"
    private ImageView playerTile3; // Value injected by FXMLLoader

    @FXML // fx:id="playerTile4"
    private ImageView playerTile4; // Value injected by FXMLLoader

    @FXML // fx:id="playerTile5"
    private ImageView playerTile5; // Value injected by FXMLLoader

    @FXML // fx:id="playerTile6"
    private ImageView playerTile6; // Value injected by FXMLLoader
    
    @FXML
    private HBox dragInImages;
    
    private ImageView selectedImageView;
    private ArrayList<Integer> rackReplacePositions;
    private GameState currentGame;
    private int tempRackPosition;
    
    /**
     * This method sets the GameState object that passes to it the reference.
     * It passes the reference so that it can manipulate the fields in that will
     * send to and receive from server
     * 
     * @param currentGame 
     */
    public void setGameState(GameState currentGame){
        this.currentGame = currentGame;
        setSwapLetters();
    }
    
    private void setSwapLetters() {
        ArrayList<TileBean> rack = this.currentGame.getRack();
        playerTile0.setImage(new Image("/images/" + rack.get(0).getLetter() + "_tile.PNG"));
        playerTile1.setImage(new Image("/images/" + rack.get(1).getLetter() + "_tile.PNG"));
        playerTile2.setImage(new Image("/images/" + rack.get(2).getLetter() + "_tile.PNG"));
        playerTile3.setImage(new Image("/images/" + rack.get(3).getLetter() + "_tile.PNG"));
        playerTile4.setImage(new Image("/images/" + rack.get(4).getLetter() + "_tile.PNG"));
        playerTile5.setImage(new Image("/images/" + rack.get(5).getLetter() + "_tile.PNG"));
        playerTile6.setImage(new Image("/images/" + rack.get(6).getLetter() + "_tile.PNG"));
    }
    
    /**
     * Calls GameState object method swapComment to send to server and receive
     * new letters
     * 
     * @param event 
     */
    @FXML
    void saveSwaps(ActionEvent event) {
        if (rackReplacePositions.size() > 0) {
            this.currentGame.swapCommit(rackReplacePositions);
            Stage stage = (Stage) dragInImages.getScene().getWindow();
            stage.close();
        }
        else {
            errorAlert("Must choose at least one letter to swap");
        }
    }
        
        /**
     * Error message popup dialog
     *
     * @param msg
     */
    private void errorAlert(String message) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle("INVALID");
        dialog.setHeaderText("Invalid move");
        dialog.setContentText(message);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();
    }
    
    /**
     * Event handler for dragging and dropping letter tiles
     *
     * @param event The event that contains the item being dragged
     */
    @FXML
    void handleTileSwapDrag(MouseEvent event) {
        ImageView dragged = (ImageView) event.getSource();
        this.tempRackPosition = Integer.parseInt(dragged.getId().charAt(10) + "");
        this.selectedImageView = dragged;
        Dragboard db = dragged.startDragAndDrop(TransferMode.ANY);
        ClipboardContent cb = new ClipboardContent();

        cb.putImage(dragged.getImage());
        db.setDragView(dragged.getImage());
        db.setContent(cb);

        event.consume();
    }
    
    /**
     * Receives the image from one ImageView to another
     *
     * @param event The drag event
     */
    @FXML
    void handleTileSwapDropped(DragEvent event) {
        ImageView receiver = (ImageView) event.getTarget();

        boolean success = false;
        if (selectedImageView != null) {
            success = true;
            receiver.setImage(selectedImageView.getImage());
            
            rackReplacePositions.add(tempRackPosition);
        }
        this.selectedImageView.setImage(null);
        ImageView imageView = (ImageView) event.getSource();
        imageView = null;

        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Readies the ImageView to transfer images (receive tile)
     *
     * @param event The drag event
     */
    @FXML
    void handleTileSwapDragOver(DragEvent event) {
        ImageView hovered = (ImageView) event.getTarget();

        if (event.getGestureSource() != dragInImages && selectedImageView.getImage() != null && hovered.getImage() == null) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }
    
    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        this.rackReplacePositions = new ArrayList<>();
    }
}
