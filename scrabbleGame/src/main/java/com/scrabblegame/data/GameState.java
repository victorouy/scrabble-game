/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrabblegame.data;

import com.scrabblegame.data.TileBean;
import com.scrabblegame.scrabble_game.ScrabbleBoardController;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.shib.java.lib.diction.DictionService;

/**
 *
 * @author Stephen He
 * @author Victor Ouy
 * @author Lucas
 */
public class GameState {

    private ClientLogic connection;
    private Player user;
    private int aiTotalScore;
    private int aiLatestScore;
    //Contains location of pieces in board but not commited
    private ArrayList<String> tempInBoard;
    private ArrayList<TileBean> inPlay;
    private TileBean[][] board;
    private ArrayList<AiTileBean> aiTilesPlaced;
    private int bag;
    private ScrabbleBoardController controller;
    private ArrayList<String> wordsPlayed;

    /**
     * Constructor for GameState to initialize objects
     *
     * @param socket
     * @param aiStarts
     * @throws IOException
     */
    public GameState(Socket socket, boolean aiStarts, ScrabbleBoardController controller) throws IOException {
        this.connection = new ClientLogic(socket, this);
        this.inPlay = new ArrayList<TileBean>();
        this.user = new Player();
        this.board = new TileBean[15][15];
        this.tempInBoard = new ArrayList<String>();
        this.aiTotalScore = 0;
        this.aiLatestScore = 0;
        this.aiTilesPlaced = new ArrayList<AiTileBean>();
        this.controller = controller;
        this.wordsPlayed = new ArrayList<String>();
    }

    public Player getUser() {
        return user;
    }

    public ArrayList<AiTileBean> getAiTilesPlaced() {
        return this.aiTilesPlaced;
    }

    public int getAiLatestScore() {
        return aiLatestScore;
    }

    public int getAiTotalScore() {
        return aiTotalScore;
    }

    public int getBag() {
        return bag;
    }

    /**
     * Getter to retrieve the temporary pieces on board
     *
     * @return an array list of the positions
     */
    public ArrayList<String> getTempInBoard() {
        return tempInBoard;
    }

    /**
     * This method sets the initial hand of the user
     */
    public void setHand() {
        this.connection.settingLetters();
    }

    /**
     * Call this method when the user places a tile on the board. In this
     * situation he did not commit yet so we store it in temp arrays
     *
     * Call it like this: game_obj = placeOnBoard(index, row, col);
     *
     * @param rackIndex The index of the tile in the player's hands
     * @param row The row in which the item is being placed
     * @param col The column in which the item is being placed
     */
    public void placeOnBoard(int rackIndex, int row, int col) {
        TileBean placed = this.user.removeTile(rackIndex);
        this.inPlay.add(placed);
        this.tempInBoard.add(row + ";" + col);
        board[row][col] = placed;
    }

    public ArrayList<TileBean> getRack() {
        return user.getRack();
    }

    /**
     * Method resets the board to how it was at the beginning of the turn
     */
    public void revertBoard() {
        //Put the pieces back in his rack
        for (TileBean item : inPlay) {
            user.addTile(item);
        }
        //Reset the place holder
        inPlay = new ArrayList<TileBean>();

        //Remove from board
        for (String item : tempInBoard) {
            String[] arr = item.split(";");
            int row = Integer.parseInt(arr[0]);
            int col = Integer.parseInt(arr[1]);

            board[row][col] = null;
        }
        //Reset the temp board
        tempInBoard = new ArrayList<String>();

    }

    /**
     * Checks if spot on board is already occupied If it is, returns false
     *
     * @param row The int value of row
     * @param col The int value of column
     * @return If we can drop
     */
    public boolean canDrop(int row, int col) {
        return board[row][col] == null;
    }

    /**
     * When click pass by user to skip turn. validates if there the user placed
     * anything on the board
     *
     * @return true if user placed nothing on board, false otherwise
     */
    public boolean playPassCommit() {
        //If tempInBoard is empty and inPlay is empty, it is a pass.
        //A pass is just a Play but with nothing inside
        if (tempInBoard.isEmpty() && inPlay.isEmpty()) {
            clientPlay();
            return true;
        } else {
            return false;
        }
    }

    /**
     * When click play validates and plays the word placed, then sends to server
     *
     * @return true if user placed a valid word(s), false otherwise
     */
    public boolean playCommit() {
        if (!inPlay.isEmpty() && !tempInBoard.isEmpty()) {
            boolean rowConcurrent = checkConcurrentRow();
            boolean colConcurrent = checkConcurrentCol();
            if (rowConcurrent || colConcurrent) {
                if (tempInBoard.size() == 1) {
                    if (checkSingleLetter(this.tempInBoard.get(0).split(";"))) {
                        if (checkWordRowAround() && checkWordColumnAround()) {
                            clientPlay();
                            return true;
                        }
                    }
                } else if (rowConcurrent && checkWordAttachedRow()) {
                    if (checkWordHorizontal() && checkWordRowAround()) {
                        clientPlay();
                        return true;
                    }
                } else if (colConcurrent && checkWordAttachedCol()) {
                    if (checkWordVertical() && checkWordColumnAround()) {
                        clientPlay();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * This method checks if a word is attached to a letter by each row
     * 
     * @return true if attached a letter by row
     */
    private boolean checkWordAttachedRow() {
        if (bag == 84) {
            return true;
        }

        for (int i = 0; i < tempInBoard.size(); i++) {
            String[] splitPos = this.tempInBoard.get(i).split(";");
            int currRow = Integer.parseInt(splitPos[0]);
            int currCol = Integer.parseInt(splitPos[1]);

            int tempRow = currRow - 1;
            if (tempRow >= 0) {
                if (board[tempRow][currCol] != null) {
                    return true;
                }
            }
            tempRow = currRow + 1;
            if (tempRow <= 14) {
                if (board[tempRow][currCol] != null) {
                    return true;
                }
            }

            int tempCol = currCol - 1;
            if (!checkIfLocalLetter(currRow, tempCol) || i == 0) {
                if (tempCol >= 0) {
                    if (board[currRow][tempCol] != null) {
                        return true;
                    }
                }
            }
            tempCol = currCol + 1;
            if (!checkIfLocalLetter(currRow, tempCol) || i == (tempInBoard.size() - 1)) {
                if (tempCol <= 14) {
                    if (board[currRow][tempCol] != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if the letter placed is attached to a letter already attached to the board
     * 
     * @param row
     * @param col
     * @return true if the letter is already placed on the board
     */
    private boolean checkIfLocalLetter(int row, int col) {
        for (int i = 0; i < tempInBoard.size(); i++) {
            String[] splitPos = this.tempInBoard.get(i).split(";");
            int currRow = Integer.parseInt(splitPos[0]);
            int currCol = Integer.parseInt(splitPos[1]);
            
            if (row == currRow && col == currCol) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks if a word is attached to a letter by each column
     * 
     * @return true if attached a letter by column
     */
    private boolean checkWordAttachedCol() {
        if (bag == 84) {
            return true;
        }

        for (int i = 0; i < tempInBoard.size(); i++) {
            String[] splitPos = this.tempInBoard.get(i).split(";");
            int currRow = Integer.parseInt(splitPos[0]);
            int currCol = Integer.parseInt(splitPos[1]);

            int tempRow = currRow - 1;
            if (!checkIfLocalLetter(tempRow, currCol) || i == 0) {
                if (tempRow >= 0) {
                    if (board[tempRow][currCol] != null) {
                        return true;
                    }
                }
            }
            tempRow = currRow + 1;
            if (!checkIfLocalLetter(tempRow, currCol) || i == (tempInBoard.size() - 1)) {
                if (tempRow <= 14) {
                    if (board[tempRow][currCol] != null) {
                        return true;
                    }
                }
            }

            int tempCol = currCol - 1;
            if (tempCol >= 0) {
                if (board[currRow][tempCol] != null) {
                    return true;
                }
            }
            tempCol = currCol + 1;
            if (tempCol <= 14) {
                if (board[currRow][tempCol] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sends data to client logic to send as binary data to server
     */
    private void clientPlay() {
        // Displays all words played this round
        this.controller.displayWordsPlayed(wordsPlayed);
        this.wordsPlayed = new ArrayList<String>();

        char[] letters = new char[tempInBoard.size()];
        int[] rowPos = new int[tempInBoard.size()];
        int[] colPos = new int[tempInBoard.size()];

        for (int i = 0; i < letters.length; i++) {
            String[] splitPos = this.tempInBoard.get(i).split(";");
            int currRow = Integer.parseInt(splitPos[0]);
            int currCol = Integer.parseInt(splitPos[1]);
            rowPos[i] = currRow;
            colPos[i] = currCol;
            letters[i] = board[currRow][currCol].getLetter();
        }
        this.connection.playLetters(letters, rowPos, colPos);

        //Reset inPlay and temp board
        inPlay = new ArrayList<TileBean>();
        tempInBoard = new ArrayList<String>();
    }

    private boolean checkSingleLetter(String[] splitPos) {
        int currRow = Integer.parseInt(splitPos[0]);
        int currCol = Integer.parseInt(splitPos[1]);

        int tempRow = currRow - 1;
        if (tempRow >= 0) {
            if (board[tempRow][currCol] != null) {
                return true;
            }
        }
        tempRow = currRow + 1;
        if (tempRow <= 14) {
            if (board[tempRow][currCol] != null) {
                return true;
            }
        }

        int tempCol = currCol - 1;
        if (tempCol >= 0) {
            if (board[currRow][tempCol] != null) {
                return true;
            }
        }
        tempCol = currCol + 1;
        if (tempCol <= 14) {
            if (board[currRow][tempCol] != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks a horizontal word placed and its letters attached to it to check
     * if they are valid words
     *
     * @return true if words are valid in dictionary, false otherwise
     */
    private boolean checkWordRowAround() {
        int countLetters = 0;

        while (countLetters < tempInBoard.size()) {
            String[] splitPos = this.tempInBoard.get(countLetters).split(";");
            int currRow = Integer.parseInt(splitPos[0]);
            int currCol = Integer.parseInt(splitPos[1]);
            int constantRow = currRow;
            String word = board[currRow][currCol].getLetter() + "";

            boolean wordAttached = false;

            boolean hasAbove = true;
            while (hasAbove) {
                //Decrement by one since we are checking to the top
                currRow--;
                //Make sure that we don't go overbounds on the top side
                if (currRow >= 0) {
                    //If there is a tile placed there
                    if (board[currRow][currCol] != null) {
                        //Append it to the front of the word
                        word = board[currRow][currCol].getLetter() + word;
                        wordAttached = true;
                    } else {
                        //If there is no more tiles, we stop checking
                        hasAbove = false;
                    }
                } else {
                    //If we reached the border, stop searching
                    hasAbove = false;
                }
            }

            boolean hasBelow = true;
            currRow = constantRow;
            while (hasBelow) {
                //Increment by one since we are checking below
                currRow++;
                if (currRow <= 14) {
                    //If there is a tile placed there
                    if (board[currRow][currCol] != null) {
                        //append it to the end
                        word = word + board[currRow][currCol].getLetter();
                        wordAttached = true;
                    } else {
                        //If there is no more tiles, we should stop checking
                        hasBelow = false;
                    }
                } else {
                    //If we reached the border, we stop searching
                    hasBelow = false;
                }
            }

            // Only checks if word is in dictionary if a word is attached to the letter
            if (wordAttached) {
                if (!checkWordDictionary(word)) {
                    return false;
                }
            }
            countLetters++;
        }
        return true;
    }

    /**
     * Checks a vertical word placed and its letters attached to it to check if
     * they are valid words
     *
     * @return true if words are valid in dictionary, false otherwise
     */
    private boolean checkWordColumnAround() {
        int countLetters = 0;

        while (countLetters < tempInBoard.size()) {
            String[] splitPos = this.tempInBoard.get(countLetters).split(";");
            int currRow = Integer.parseInt(splitPos[0]);
            int currCol = Integer.parseInt(splitPos[1]);
            int constantCol = currCol;
            String word = board[currRow][currCol].getLetter() + "";

            boolean wordAttached = false;

            boolean hasLeft = true;
            while (hasLeft) {
                //Decrement by one since we are checking to left
                currCol--;
                //Make sure we don't go overbounds on the left side
                if (currCol >= 0) {
                    //If there is a tile placed there
                    if (board[currRow][currCol] != null) {
                        //Append it to the front of the word
                        word = board[currRow][currCol].getLetter() + word;
                        wordAttached = true;
                    } else {
                        //If there is no more tiles, we stop checking
                        hasLeft = false;
                    }
                } else {
                    //If we reached the borded, stop searching
                    hasLeft = false;
                }
            }

            currCol = constantCol;
            boolean hasRight = true;
            while (hasRight) {
                //Increment by one since we are checking to the right
                currCol++;
                if (currCol <= 14) {
                    //If there is a tile placed there
                    if (board[currRow][currCol] != null) {
                        //Append it to the end
                        word += board[currRow][currCol].getLetter();
                        wordAttached = true;
                    } else {
                        //If there is no more tiles, we stop checking
                        hasRight = false;
                    }
                } else {
                    //If we reached the border, we stop searching
                    hasRight = false;
                }
            }

            // Only checks if word is in dictionary if a word is attached to the letter
            if (wordAttached) {
                if (!checkWordDictionary(word)) {
                    return false;
                }
            }
            countLetters++;
        }
        return true;
    }

    /**
     * Checks the horizontal intended word of the user Returns if that word is
     * in the dictionary
     *
     * @return wether or not the word is valid
     */
    private boolean checkWordHorizontal() {
        String word = "";

        String[] splitPos = this.tempInBoard.get(0).split(";");
        int constantRow = Integer.parseInt(splitPos[0]);
        int currCol = Integer.parseInt(splitPos[1]);
        splitPos = this.tempInBoard.get(tempInBoard.size() - 1).split(";");
        int endCol = Integer.parseInt(splitPos[1]);

        while (currCol <= endCol) {
            word += board[constantRow][currCol].getLetter();
            currCol++;
        }

        //Now we look Left to see if there are any characters (since the word is horizontal)
        //Get the smallest col value
        splitPos = this.tempInBoard.get(0).split(";");
        currCol = Integer.parseInt(splitPos[1]);

        boolean hasLeft = true;

        while (hasLeft) {
            //Decrement by one since we are checking to left
            currCol--;
            //Make sure we don't go overbounds on the left side
            if (currCol >= 0) {
                //If there is a tile placed there
                if (board[constantRow][currCol] != null) {
                    //Append it to the front of the word
                    word = board[constantRow][currCol].getLetter() + word;
                } else {
                    //If there is no more tiles, we stop checking
                    hasLeft = false;
                }
            } else {
                //If we reached the borded, stop searching
                hasLeft = false;
            }
        }

        //Now we look right to see if there are any characters (Since word is horizontal)
        //Get the largest col value
        splitPos = this.tempInBoard.get(this.tempInBoard.size() - 1).split(";");
        //Only need to update currCol since row should still be the same
        currCol = Integer.parseInt(splitPos[1]);

        boolean hasRight = true;
        while (hasRight) {
            //Increment by one since we are checking to the right
            currCol++;
            if (currCol <= 14) {
                //If there is a tile placed there
                if (board[constantRow][currCol] != null) {
                    //Append it to the end
                    word += board[constantRow][currCol].getLetter();
                } else {
                    //If there is no more tiles, we stop checking
                    hasRight = false;
                }
            } else {
                //If we reached the border, we stop searching
                hasRight = false;
            }
        }

        return checkWordDictionary(word);
    }

    /**
     * Checks the vertical intended word of the user Returns if that word is in
     * the dictionary
     *
     * @return
     */
    private boolean checkWordVertical() {

        String word = "";

        String[] splitPos = this.tempInBoard.get(0).split(";");
        int currRow = Integer.parseInt(splitPos[0]);
        int constantCol = Integer.parseInt(splitPos[1]);
        splitPos = this.tempInBoard.get(tempInBoard.size() - 1).split(";");
        int endRow = Integer.parseInt(splitPos[0]);

        while (currRow <= endRow) {
            word += board[currRow][constantCol].getLetter();
            currRow++;
        }

        //Now we look Above to see if there are any characters (since the word is vertical)
        //Get the smallest row value
        splitPos = this.tempInBoard.get(0).split(";");
        currRow = Integer.parseInt(splitPos[0]);
        constantCol = Integer.parseInt(splitPos[1]);

        boolean hasAbove = true;

        while (hasAbove) {
            //Decrement by one since we are checking to the top
            currRow--;
            //Make sure that we don't go overbounds on the top side
            if (currRow >= 0) {
                //If there is a tile placed there
                if (board[currRow][constantCol] != null) {
                    //Append it to the front of the word
                    word = board[currRow][constantCol].getLetter() + word;
                } else {
                    //If there is no more tiles, we stop checking
                    hasAbove = false;
                }
            } else {
                //If we reached the border, stop searching
                hasAbove = false;
            }
        }

        //Now we look Below to see if there are any characters (Since word is horizontal)
        //Get the largest row value
        splitPos = this.tempInBoard.get(this.tempInBoard.size() - 1).split(";");
        //Only need to update currRow since col should still be the same
        currRow = Integer.parseInt(splitPos[0]);

        boolean hasBelow = true;
        while (hasBelow) {
            //Increment by one since we are checking below
            currRow++;
            if (currRow <= 14) {
                //If there is a tile placed there
                if (board[currRow][constantCol] != null) {
                    //append it to the end
                    word += board[currRow][constantCol].getLetter();
                } else {
                    //If there is no more tiles, we should stop checking
                    hasBelow = false;
                }
            } else {
                //If we reached the border, we stop searching
                hasBelow = false;
            }
        }

        return checkWordDictionary(word);
    }

    /**
     * Checks to see if the word exists
     *
     * @param word the word
     * @return exist or not
     */
    private boolean checkWordDictionary(String word) {
        System.out.println("Word: " + word);
        DictionService dictService = new DictionService();

        if (dictService.getDictionWord(word) != null) {
            this.wordsPlayed.add(word);
            return true;
        } else {
            this.wordsPlayed = new ArrayList<String>();
            return false;
        }
    }

    /**
     * Checks if the letters placed are in a concurrent row
     *
     * @return true if concurrent, false otherwise
     */
    private boolean checkConcurrentRow() {
        List<Integer> columns = new ArrayList<Integer>();

        String expRow = "";
        for (String item : tempInBoard) {
            String[] arr = item.split(";");
            columns.add(Integer.parseInt(arr[1]));

            if (expRow.equals("")) {
                expRow = arr[0];
            } else if (!expRow.equals(arr[0])) {
                return false;
            }
        }
        Collections.sort(columns);
        tempInBoard = new ArrayList<String>();
        for (int col : columns) {
            tempInBoard.add(expRow + ";" + col);
        }
        return checkNoSpaceInBetweenRow();
    }

    /**
     * Given that the board starts off like this:
     *
     *
     * _____h______ _____i______
     *
     * Let's say we place pieces like this in our turn
     *
     * _____h______ ____tip_____
     *
     *
     * 't' and 'p' are in concurrent row, but now we have to make sure that
     * there are no empty spaces between them
     *
     * Move is valid if there are NO empty spaces in between
     *
     * @return true if move was valid
     */
    private boolean checkNoSpaceInBetweenRow() {
        String[] letter = tempInBoard.get(0).split(";");
        int row = Integer.parseInt(letter[0]);
        int col = Integer.parseInt(letter[1]);
        letter = tempInBoard.get(tempInBoard.size() - 1).split(";");
        int endCol = Integer.parseInt(letter[1]);

        while (col < endCol) {
            col++;
            if (board[row][col] == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the letters placed are in a concurrent column
     *
     * @return true if concurrent, false otherwise
     */
    private boolean checkConcurrentCol() {
        List<Integer> rows = new ArrayList<Integer>();

        String expCol = "";
        for (String item : tempInBoard) {
            String[] arr = item.split(";");
            rows.add(Integer.parseInt(arr[0]));

            if (expCol.equals("")) {
                expCol = arr[1];
            } else if (!expCol.equals(arr[1])) {
                return false;
            }
        }
        Collections.sort(rows);
        tempInBoard = new ArrayList<String>();
        for (int row : rows) {
            tempInBoard.add(row + ";" + expCol);
        }

        return checkNoSpaceInBetweenCol();
    }

    /**
     * Same thing as checkNoSpaceInBetweenRow() but this time it's with the
     * columns
     *
     * @return true if move was valid
     */
    private boolean checkNoSpaceInBetweenCol() {
        String[] letter = tempInBoard.get(0).split(";");
        int row = Integer.parseInt(letter[0]);
        int col = Integer.parseInt(letter[1]);
        letter = tempInBoard.get(tempInBoard.size() - 1).split(";");
        int endRow = Integer.parseInt(letter[0]);

        while (row < endRow) {
            row++;
            if (board[row][col] == null) {
                return false;
            }
        } 
        return true;
    }

    /**
     * This is called on client logic to initialize aiTilesPlaced of ai rows and
     * column and chars when it receives data from server
     *
     * @param rowsAi
     * @param colsAi
     * @param charsAi
     */
    public void setReturnAiPlay(int[] rowsAi, int[] colsAi, char[] charsAi) {
        String wordAI = "";
        for (int i = 0; i < rowsAi.length; i++) {
            aiTilesPlaced.add(new AiTileBean(charsAi[i], rowsAi[i], colsAi[i]));
            this.board[rowsAi[i]][colsAi[i]] = new TileBean(charsAi[i], -1);
            wordAI = charsAi[i] + wordAI;
        }

        // Displays all words played this round by AI
        wordsPlayed.add(wordAI);
        this.controller.displayWordsPlayed(wordsPlayed);
        this.wordsPlayed = new ArrayList<String>();

        this.controller.updateAIFirstMove();
    }

    /**
     * This is called on client logic when it received the AIscore, clientscore,
     * and receivedTiles back from server
     *
     * @param aiScore
     * @param clientScore
     * @param receivedBack
     */
    public void setScoresAndUserTilesPostRound(int aiScore, int clientScore, char[] receivedBack, int bag) {
        this.aiLatestScore = aiScore;
        this.aiTotalScore += aiScore;
        this.user.addPoints(clientScore);
        this.bag = bag;

        for (char tileChar : receivedBack) {
            this.user.addTile(new TileBean(tileChar, -1));
        }

        this.controller.updateGameState();
        this.aiTilesPlaced = new ArrayList<AiTileBean>();
    }

    /**
     * This is called to commit and transfer the data that the swap will use
     * client logic to send data to server
     *
     * @param rackPositions
     */
    public void swapCommit(ArrayList<Integer> rackPositions) {
        char[] swapLetters = new char[rackPositions.size()];
        int count = 0;
        for (int index : rackPositions) {
            swapLetters[count] = this.user.removeTile(index).getLetter();
            count++;
        }
        this.connection.swapLettersServer(swapLetters);
    }

    /**
     * Checks to see who has won
     */
    public void endGame() throws IOException {
        if (this.aiTotalScore > this.user.getPoints()) {
            this.controller.winnerAlert("AI HAS WON!");
        } else if (this.aiTotalScore < this.user.getPoints()) {
            this.controller.winnerAlert("YOU HAVE WON!");
        } else {
            this.controller.winnerAlert("USER AND AI HAVE TIED!");
        }
    }
}
