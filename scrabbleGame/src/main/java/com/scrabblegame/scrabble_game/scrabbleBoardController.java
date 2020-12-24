/**
 * Sample Skeleton for 'scrabble_board.fxml' Controller Class
 */
package com.scrabblegame.scrabble_game;

import com.scrabblegame.data.AiTileBean;
import com.scrabblegame.data.GameState;
import com.scrabblegame.data.TileBean;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Stephen He
 * @author Victor Ouy
 * @author Lucas
 */
public class ScrabbleBoardController {

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

    @FXML // fx:id="playBtn"
    private Button playBtn; // Value injected by FXMLLoader

    @FXML // fx:id="passBtn"
    private Button passBtn; // Value injected by FXMLLoader

    @FXML // fx:id="clearBtn"
    private Button clearBtn; // Value injected by FXMLLoader

    @FXML // fx:id="swapBtn"
    private Button swapBtn; // Value injected by FXMLLoader

//    @FXML // fx:id="wordsAnchorPane"
//    private AnchorPane wordsAnchorPane; // Value injected by FXMLLoader
    @FXML // fx:id="vboxWordsPlayed"
    private VBox vboxWordsPlayed; // Value injected by FXMLLoader

    @FXML
    private GridPane scrabbleGrid;

    @FXML // fx:id="cpuLastScore"
    private Label cpuLastScore; // Value injected by FXMLLoader

    @FXML // fx:id="cpuTotalScore"
    private Label cpuTotalScore; // Value injected by FXMLLoader

    @FXML // fx:id="playerLastScore"
    private Label playerLastScore; // Value injected by FXMLLoader

    @FXML // fx:id="playerTotalScore"
    private Label playerTotalScore; // Value injected by FXMLLoader

    @FXML // fx:id="remainingTiles"
    private Label remainingTiles; // Value injected by FXMLLoader

    @FXML
    private ImageView startTile;

    private DropShadow highlightTileBorder;
    private List<ImageView> highlightedTiles;
    private List<ImageView> highlightedAiTiles;

    private GameState currentGame;
    private ImageView selectedImageView;
    private boolean aiStarts;
    private boolean firstPlay;

    private DialogPane swapRootLayout;
    private Dialog swapGUI;
    private HashMap<String, String> specialCoords;
    private Socket socket;

    @FXML
    void onClear(MouseEvent event) {
        //Disable borders.
        disableHighlights(this.highlightedTiles);
        this.highlightedTiles.clear();

        removeFromBoard();
        this.currentGame.revertBoard();

        setPlayerRack();
    }

    private void removeFromBoard() {
        ArrayList<String> toRemove = this.currentGame.getTempInBoard();

        for (String position : toRemove) {
            if (!resetToSpecial(position)) {
                String[] coords = position.split(";");
                int row = Integer.parseInt(coords[0]);
                int col = Integer.parseInt(coords[1]);

                for (Node node : scrabbleGrid.getChildren()) {
                    //Gets the coordinates of the node
                    int nodeRow = 0;
                    if (GridPane.getRowIndex(node) != null) {
                        nodeRow = GridPane.getRowIndex(node);
                    }
                    int nodeCol = 0;
                    if (GridPane.getColumnIndex(node) != null) {
                        nodeCol = GridPane.getColumnIndex(node);
                    }

                    //checks if the coords are matching what we want
                    if (nodeRow == row && nodeCol == col) {
                        ImageView toClear = (ImageView) node;
                        toClear.setImage(null);
                    }
                }
            }
        }
    }

    private boolean resetToSpecial(String position) {
        String modifier = this.specialCoords.get(position);
        if (!Objects.isNull(modifier)) {
            String[] coords = position.split(";");

            int row = Integer.parseInt(coords[0]);
            int col = Integer.parseInt(coords[1]);

            for (Node node : scrabbleGrid.getChildren()) {
                //Gets the coordinates of the node
                int nodeRow = 0;
                if (GridPane.getRowIndex(node) != null) {
                    nodeRow = GridPane.getRowIndex(node);
                }
                int nodeCol = 0;
                if (GridPane.getColumnIndex(node) != null) {
                    nodeCol = GridPane.getColumnIndex(node);
                }
                //checks if the coords are matching what we want
                if (nodeRow == row && nodeCol == col) {
                    ImageView toClear = (ImageView) node;
                    toClear.setImage(new Image("/images/" + modifier + ".png"));
                }
            }
            return true;
        }
        return false;
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

    @FXML
    void onPass(MouseEvent event) {
        if (!this.currentGame.playPassCommit()) {
            errorAlert("Must have all letters in user's hand/rack");
        }
    }

    @FXML
    void onPlay(MouseEvent event) {
        //Disable borders of tiles played.
        disableHighlights(this.highlightedTiles);

        if (checkStartPlay()) {

            boolean validPlay = this.currentGame.playCommit();

            if (validPlay) {
                firstPlay = true;

                //There are no longer highlighted tiles on the board.
                this.highlightedTiles.clear();
            } else {
                errorAlert("Sorry, letters/word placed not valid or not in dictionary");

                //Highlight tiles again because the play isn't valid
                this.highlightedTiles.forEach(tile -> {
                    tile.setEffect(this.highlightTileBorder);
                });
            }
        } else {
            errorAlert("Must add a letter on STAR(center) tile in first round");
        }
    }

    /**
     * This method disables the highlighted borders of the tiles placed on the
     * board
     */
    private void disableHighlights(List<ImageView> highlightedTiles) {
        if (!highlightedTiles.isEmpty()) {
            highlightedTiles.forEach(tile -> {
                tile.setEffect(null);
            });
        }
    }

    private boolean checkStartPlay() {
        if (firstPlay) {
            String[] startTileURL = startTile.getImage().getUrl().split("/");
            return !startTileURL[startTileURL.length - 1].equals("ST.png");
        }
        return true;
    }

    public void updateGameState() {
        playerLastScore.setText(Integer.toString(this.currentGame.getUser().getLatestScore()));
        playerTotalScore.setText(Integer.toString(this.currentGame.getUser().getPoints()));
        remainingTiles.setText(Integer.toString(this.currentGame.getBag()));

        if (this.currentGame.getBag() > 0) {
            setPlayerRack();
        }

        //Disable the highlighted borders of the previous word
        disableHighlights(this.highlightedAiTiles);

        // Get AiTileBean arrayList to place tile in gui
        for (AiTileBean aiTile : this.currentGame.getAiTilesPlaced()) {
            for (Node node : scrabbleGrid.getChildren()) {
                //Gets the coordinates of the node
                int nodeRow = 0;
                if (GridPane.getRowIndex(node) != null) {
                    nodeRow = GridPane.getRowIndex(node);
                }
                int nodeCol = 0;
                if (GridPane.getColumnIndex(node) != null) {
                    nodeCol = GridPane.getColumnIndex(node);
                }

                //checks if the coords are matching what we want
                if (nodeRow == aiTile.getAiRow() && nodeCol == aiTile.getAiCol()) {

                    //this.highlightedAiTiles.clear();
                    ImageView placedAiTile = (ImageView) node;

                    placedAiTile.setImage(new Image("/images/" + aiTile.getAiLetter() + "_tile.PNG"));

                    //Highlighting the border of the tile being placed on the board
                    placedAiTile.setEffect(this.highlightTileBorder);

                    //Adding it to the appropriate list of highlighted tiles
                    this.highlightedAiTiles.add(placedAiTile);
                }
            }
        }

        cpuLastScore.setText(Integer.toString(this.currentGame.getAiLatestScore()));
        cpuTotalScore.setText(Integer.toString(this.currentGame.getAiTotalScore()));
    }

    public void updateAIFirstMove() {
        // Get AiTileBean arrayList to place tile in gui
        for (AiTileBean aiTile : this.currentGame.getAiTilesPlaced()) {
            for (Node node : scrabbleGrid.getChildren()) {
                //Gets the coordinates of the node
                int nodeRow = 0;
                if (GridPane.getRowIndex(node) != null) {
                    nodeRow = GridPane.getRowIndex(node);
                }
                int nodeCol = 0;
                if (GridPane.getColumnIndex(node) != null) {
                    nodeCol = GridPane.getColumnIndex(node);
                }

                //checks if the coords are matching what we want
                if (nodeRow == aiTile.getAiRow() && nodeCol == aiTile.getAiCol()) {
                    ImageView placedAiTile = (ImageView) node;
                    placedAiTile.setImage(new Image("/images/" + aiTile.getAiLetter() + "_tile.PNG"));
                }
            }
        }

        cpuLastScore.setText(Integer.toString(this.currentGame.getAiLatestScore()));
        cpuTotalScore.setText(Integer.toString(this.currentGame.getAiTotalScore()));
    }

    /**
     * This method indicates the positions in the rack that the player wants
     * changed. Called by the RackTileSwapController.
     *
     * @param swappedTiles List of positions the player wants changed.
     */
    @FXML
    void onSwap(MouseEvent event) {
        if (this.currentGame.getUser().rackInsideLength() == 7) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setResources(resources);

                loader.setLocation(ScrabbleBoardController.class
                        .getResource("/fxml/RackTileSwap.fxml"));

                // Initialize Dialog box
                DialogPane swapRootLayout = (DialogPane) loader.load();
                swapRootLayout.getButtonTypes().add(ButtonType.CLOSE);

                //Put content in it
                Dialog swapGUI = new Dialog();
                swapGUI.setDialogPane(swapRootLayout);

                //Initialize the controller with a reference to this class
                RackTileSwapController rackController = loader.getController();
                rackController.setGameState(this.currentGame);
                swapGUI.show();

            } catch (IOException ex) {
                System.out.println("Error swapping the tiles in the rack.");
                Platform.exit();
            }
        } else {
            errorAlert("Must have all letters in user's hand/rack");
        }
    }

    private void setPlayerRack() {
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
     * Sets the communication socket.
     *
     * @param socket
     */
    public void setStart(Socket socket) throws IOException {
        this.socket = socket;
        this.currentGame = new GameState(socket, aiStarts, this);
        setHashMap();
        this.currentGame.setHand();

        // Review if this is correct
        if (aiStarts) {
            this.currentGame.playPassCommit();
        }
    }

    /**
     * Sets the property whoStarts so that the game board controller will know
     * who makes the first move.
     *
     * @param aiStarts
     */
    public void setFirstMove(boolean aiStarts) {
        this.aiStarts = aiStarts;

        // If AI does not start, then you will be first play
        if (aiStarts == false) {
            this.firstPlay = true;
        } else {
            this.firstPlay = false;
        }
    }

    /**
     * Displays all words played this round on scroll pane
     */
    public void displayWordsPlayed(ArrayList<String> wordsPlayed) {
        for (String wordPlayed : wordsPlayed) {
            Label wordLabel = new Label(wordPlayed.toUpperCase());
            wordLabel.setFont(new Font("Arial", 16));
            this.vboxWordsPlayed.getChildren().add(0, wordLabel);
        }
    }

    /**
     * Event handler for dragging and dropping letter tiles
     *
     * @param event The event that contains the item being dragged
     */
    @FXML
    void handleTileDrag(MouseEvent event) {
        ImageView dragged = (ImageView) event.getSource();
        this.selectedImageView = dragged;

        //Highlighting the border
        this.selectedImageView.setEffect(this.highlightTileBorder);

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
    void handleTileDropped(DragEvent event) {
        ImageView receiver = (ImageView) event.getTarget();

        boolean success = false;
        if (selectedImageView != null) {
            success = true;
            receiver.setImage(selectedImageView.getImage());

            //Highlight border until word is played
            receiver.setEffect(this.highlightTileBorder);

            // Put imageview in a list so the highlighting can be disabled when pressing play
            this.highlightedTiles.add(receiver);
            this.highlightedTiles.add(this.selectedImageView);

            //Get Id
            String idStr = this.selectedImageView.getId();
            int id = Character.getNumericValue(idStr.toCharArray()[idStr.toCharArray().length - 1]);

            //Get Row and Column
            ImageView hovered = (ImageView) event.getTarget();
            int row = 0;
            if (GridPane.getRowIndex(hovered) != null) {
                row = GridPane.getRowIndex(hovered);
            }
            int col = 0;
            if (GridPane.getColumnIndex(hovered) != null) {
                col = GridPane.getColumnIndex(hovered);
            }

            //Set game state
            this.currentGame.placeOnBoard(id, row, col);
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
    void handleTileDragOver(DragEvent event) {
        ImageView hovered = (ImageView) event.getTarget();

        int row = 0;
        if (GridPane.getRowIndex(hovered) != null) {
            row = GridPane.getRowIndex(hovered);
        }
        int col = 0;
        if (GridPane.getColumnIndex(hovered) != null) {
            col = GridPane.getColumnIndex(hovered);
        }

        if (this.currentGame.canDrop(row, col) && event.getGestureSource() != scrabbleGrid && selectedImageView.getImage() != null) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert playerTile0 != null : "fx:id=\"playerTile0\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert playerTile1 != null : "fx:id=\"playerTile1\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert playerTile2 != null : "fx:id=\"playerTile2\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert playerTile3 != null : "fx:id=\"playerTile3\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert playerTile4 != null : "fx:id=\"playerTile4\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert playerTile5 != null : "fx:id=\"playerTile5\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert playerTile6 != null : "fx:id=\"playerTile6\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert playBtn != null : "fx:id=\"playBtn\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert passBtn != null : "fx:id=\"passBtn\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert clearBtn != null : "fx:id=\"clearBtn\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert swapBtn != null : "fx:id=\"swapBtn\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert vboxWordsPlayed != null : "fx:id=\"vboxWordsPlayed\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert cpuLastScore != null : "fx:id=\"cpuLastScore\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert cpuTotalScore != null : "fx:id=\"cpuTotalScore\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert playerLastScore != null : "fx:id=\"playerLastScore\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert playerTotalScore != null : "fx:id=\"playerTotalScore\" was not injected: check your FXML file 'scrabble_board.fxml'.";
        assert remainingTiles != null : "fx:id=\"remainingTiles\" was not injected: check your FXML file 'scrabble_board.fxml'.";

        // Setting color of the highlighter
        this.highlightTileBorder = new DropShadow(15, Color.BLUE);
        this.highlightedTiles = new ArrayList<>();
        this.highlightedAiTiles = new ArrayList<>();
    }

    private void setHashMap() {
        specialCoords = new HashMap<String, String>();
        //Row 0
        specialCoords.put("0;1", "DL");
        specialCoords.put("0;4", "TW");
        specialCoords.put("0;5", "TL");
        specialCoords.put("0;7", "DW");
        specialCoords.put("0;9", "TL");
        specialCoords.put("0;10", "TW");
        specialCoords.put("0;13", "DL");
        //Row 1
        specialCoords.put("1;0", "TW");
        specialCoords.put("1;1", "DW");
        specialCoords.put("1;13", "DW");
        specialCoords.put("1;14", "TW");
        //Row 2
        specialCoords.put("2;3", "TL");
        specialCoords.put("2;11", "TL");
        //Row 3
        specialCoords.put("3;2", "TL");
        specialCoords.put("3;5", "DW");
        specialCoords.put("3;6", "DL");
        specialCoords.put("3;8", "DL");
        specialCoords.put("3;9", "DW");
        specialCoords.put("3;12", "TL");
        //Row 4
        specialCoords.put("4;1", "TL");
        specialCoords.put("4;5", "DL");
        specialCoords.put("4;9", "DL");
        specialCoords.put("4;13", "TL");
        //Row 5
        specialCoords.put("5;0", "TL");
        specialCoords.put("5;3", "DW");
        specialCoords.put("5;4", "DL");
        specialCoords.put("5;6", "TL");
        specialCoords.put("5;8", "TL");
        specialCoords.put("5;10", "DL");
        specialCoords.put("5;11", "DW");
        specialCoords.put("5;14", "TL");
        //Row 6
        specialCoords.put("6;3", "DL");
        specialCoords.put("6;11", "DL");
        //Row 7
        specialCoords.put("7;0", "DW");
        specialCoords.put("7;4", "DW");
        specialCoords.put("7;7", "ST");
        specialCoords.put("7;10", "DW");
        specialCoords.put("7;14", "DW");
        //Row 8
        specialCoords.put("8;3", "DL");
        specialCoords.put("8;11", "DL");
        //Row 9
        specialCoords.put("9;0", "TL");
        specialCoords.put("9;3", "DW");
        specialCoords.put("9;4", "DL");
        specialCoords.put("9;6", "TL");
        specialCoords.put("9;8", "TL");
        specialCoords.put("9;10", "DL");
        specialCoords.put("9;11", "DW");
        specialCoords.put("9;14", "TL");
        //Row 10
        specialCoords.put("10;1", "TL");
        specialCoords.put("10;5", "DL");
        specialCoords.put("10;9", "DL");
        specialCoords.put("10;13", "TL");
        //Row 11
        specialCoords.put("11;2", "TL");
        specialCoords.put("11;5", "DW");
        specialCoords.put("11;6", "DL");
        specialCoords.put("11;8", "DL");
        specialCoords.put("11;9", "DW");
        specialCoords.put("11;12", "TL");
        //Row 12
        specialCoords.put("12;3", "TL");
        specialCoords.put("12;11", "TL");
        //Row 13
        specialCoords.put("13;0", "TW");
        specialCoords.put("13;1", "DW");
        specialCoords.put("13;13", "DW");
        specialCoords.put("13;14", "TW");
        //Row 14
        specialCoords.put("14;1", "DL");
        specialCoords.put("14;4", "TW");
        specialCoords.put("14;5", "TL");
        specialCoords.put("14;7", "DW");
        specialCoords.put("14;9", "TL");
        specialCoords.put("14;10", "TW");
        specialCoords.put("14;13", "DL");
    }

    /**
     * Winner message popup dialog
     *
     * @param msg
     */
    public void winnerAlert(String message) throws IOException {
        //Notify who won
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("CONGRATULATIONS!!!");
        dialog.setHeaderText("GAME WINNER");
        dialog.setContentText(message);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.showAndWait();

        //Close the window and reinitialize the start up menu
        Stage closeStage = (Stage) playerTile0.getScene().getWindow();
        closeStage.close();

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(App.class.getResource("/fxml/MenuScrabbleFXML.fxml"));

        AnchorPane anchor = (AnchorPane) loader.load();
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Menu");

        MenuScrabbleFXMLController menuController = loader.getController();
        menuController.setProperties(primaryStage, this.socket);

        Scene scene = new Scene(anchor);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
