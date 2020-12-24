package com.scrabblegame.scrabbleserver;

import java.util.ArrayList;
import me.shib.java.lib.diction.DictionService;

/**
 * The AI logic on server side
 *
 * @author Stephen He
 * @author Victor Ouy
 * @author Lucas
 */
public class ScrabbleAI {

    private int pts;
    private int latestPts;
    private TileBean[] rack;
    private ServerGameState currentGame;
    private TileBean[][] board;
    private ArrayList<TileBean> bag;
    private StringBuilder wordCombinations = new StringBuilder();
    private String rackString;
    private int[] row;
    private int[] col;
    boolean appendFront;
    private String wordCreated;
    private char letterAppend;
    private int checkChange;
    private int aiPoints;
    private byte[] aiPlay;

    public ScrabbleAI(ServerGameState game) {
        this.rack = new TileBean[7];
        this.pts = 0;
        this.latestPts = 0;
        this.currentGame = game;
        this.bag = currentGame.bag;
        this.board = currentGame.board;
        this.rackString = "";
        this.wordCreated = "";
        this.letterAppend = (char) 0;
        this.checkChange = 0;
        this.aiPoints = 0;
        this.aiPlay = aiPlay = new byte[23];
    }

    /**
     * Sets the AI rack
     */
    public void setRack() {
        for (int i = 0; i < rack.length; i++) {
            this.rack[i] = currentGame.getTileBag();
        }
    }

    /**
     * Gets the latest points of AI move
     *
     * @return the latest points
     */
    public int getLatestPts() {
        int pointTemp = this.aiPoints;
        this.aiPoints = 0;
        return pointTemp;
    }

    /**
     * Resets the latest points (we do this so that if the AI Skips, the points
     * are properly set to 0)
     */
    public void resetLatestPts() {
        this.latestPts = 0;
    }

    /**
     * This method plays a word using an AI algorithm that loops through the
     * entire board and finds the best suited word to be created. It then sends
     * back the found word by storing the positions and characters
     *
     * @return byte array of created binary data to client of ai moves
     */
    public byte[] playWord() {

        // Sets the rack string used when finding combination of words
        for (int i = 0; i < rack.length; i++) {
            rackString = rackString + rack[i].getLetter();
        }
        // Updated the bag of the GameState object bag property
        this.bag = currentGame.getBag();

        // Initializes the byte array that will be send back to client
        // aiPlay[0] = 0 will be decoded by client side to know this data is ai info
        this.aiPlay = new byte[23];
        this.aiPlay[0] = 0;

        // Enters when it is the AI first move
        if (board[7][7] == null) {
            createFirstWord();
            aiPlay[1] = (byte) wordCreated.length();

            int row = 7;
            int col = 7;
            int rowAndCol = 0;
            int indexLetter = 0;

            for (int byteIndex = 2; byteIndex < 23; byteIndex++) {
                if (rowAndCol < 2) {
                    if (rowAndCol == 0) {
                        aiPlay[byteIndex] = (byte) row;
                    } else {
                        aiPlay[byteIndex] = (byte) col;
                        col++;
                    }
                    rowAndCol++;
                } else if (indexLetter < wordCreated.length()) {
                    aiPlay[byteIndex] = currentGame.charToByte.get(this.wordCreated.charAt(indexLetter));
                    rowAndCol = 0;
                    indexLetter++;;
                } else {
                    aiPlay[byteIndex] = (byte) 0;
                }
            }
        } else {
            boolean foundWord = false;
            // the AI will first finds a valid letter to place upon, it finds this 
            // ailetter by looping through the entire board. checkChange is to diversify 
            // the location in which it adds to a letter. As checkChange == 0 means the program
            // will look through the board from the top-left corner downwards, checkChange == 1
            // means the program will look through the board from the top-right corner leftwards, ETC.
            if (checkChange == 0) {
                for (int i = 0; i < 15; i++) {
                    for (int j = 0; j < 15; j++) {
                        if (board[i][j] != null) {

                            int largest = findMostSpaces(i, j);
                            // If it does not find a largest, then it will continue
                            // through the next iteration of the loop
                            if (largest == -1) {
                                continue;
                            }

                            aiMoves(i, j, largest);
                            // break to avoid iterating when already found a word
                            foundWord = true;
                            break;
                        }
                    }
                    if (foundWord == true) {
                        break;
                    }
                }
                checkChange++;
            } else if (checkChange == 1) {
                for (int i = 14; i >= 0; i--) {
                    for (int j = 0; j < 15; j++) {
                        if (board[j][i] != null) {

                            int largest = findMostSpaces(j, i);
                            // If it does not find a largest, then it will continue
                            // through the next iteration of the loop
                            if (largest == -1) {
                                continue;
                            }

                            aiMoves(j, i, largest);
                            // break to avoid iterating when already found a word
                            foundWord = true;
                            break;
                        }
                    }
                    if (foundWord == true) {
                        break;
                    }
                }

                checkChange++;
            } else if (checkChange == 2) {
                for (int i = 14; i >= 0; i--) {
                    for (int j = 14; j >= 0; j--) {
                        if (board[i][j] != null) {

                            int largest = findMostSpaces(i, j);
                            // If it does not find a largest, then it will continue
                            // through the next iteration of the loop
                            if (largest == -1) {
                                continue;
                            }

                            aiMoves(i, j, largest);
                            // break to avoid iterating when already found a word
                            foundWord = true;
                            break;
                        }
                    }
                    if (foundWord == true) {
                        break;
                    }
                }
                checkChange++;
            } else if (checkChange == 3) {
                for (int i = 0; i < 15; i++) {
                    for (int j = 14; j >= 0; j--) {
                        if (board[j][i] != null) {

                            int largest = findMostSpaces(j, i);
                            // If it does not find a largest, then it will continue
                            // through the next iteration of the loop
                            if (largest == -1) {
                                continue;
                            }

                            aiMoves(j, i, largest);
                            // break to avoid iterating when already found a word
                            foundWord = true;
                            break;
                        }
                    }
                    if (foundWord == true) {
                        break;
                    }
                }
                checkChange = 0;
            }
        }
        // Calculates the points of the word created
        //this.aiPoints = currentGame.calcAiWord(wordCreated, row, col, appendFront);
        if (row == null) {
            this.aiPoints = 0;
        } else {
            this.aiPoints = currentGame.calculatePoints(row, col);
            System.out.println("ai pts: " + aiPoints);
        }

        // Removes the letters on the rack that have been played and retrieves
        // new letters from bag
        for (int wordIndex = 0; wordIndex < wordCreated.length(); wordIndex++) {
            for (int rackIndex = 0; rackIndex < rack.length; rackIndex++) {
                if (rack[rackIndex].getLetter() == wordCreated.charAt(wordIndex)) {
                    if (bag.size() > 0) {
                        rack[rackIndex] = currentGame.getTileBag();
                        break;
                    }
                }
            }
        }

        // TESTS:
        System.out.println("FINAL WORD: " + wordCreated.toUpperCase());
        for (int t = 0; t < aiPlay.length; t++) {
            System.out.println(aiPlay[t]);
        }

        // Sets the fields back to default
        wordCombinations = new StringBuilder();
        this.rackString = "";
        this.wordCreated = "";
        this.letterAppend = (char) 0;

        return aiPlay;
    }

    /**
     * This method finds the most optimal direction to place letters
     *
     * @param row
     * @param col
     * @return the largest amount of space
     */
    private int findMostSpaces(int row, int col) {
        int[] spaces = new int[4];
        spaces[0] = checkTopSpaces(row, col);
        spaces[1] = checkBottomSpaces(row, col);
        spaces[2] = checkLeftSpaces(row, col);
        spaces[3] = checkRightSpaces(row, col);

        int largest = 0;
        for (int largestIndex = 1; largestIndex < spaces.length; largestIndex++) {
            if (spaces[largestIndex] > spaces[largest]) {
                largest = largestIndex;
            }
        }
        // If the largest space is 0, then there are no spaces optimal
        if (spaces[largest] == 0) {
            return -1;
        }
        return largest;
    }

    /**
     * Plays the ai moves in the proper direction
     *
     * @param i row
     * @param j column
     * @param largest replacement for top(0), bottom(1), left(2) and right(3)
     */
    private void aiMoves(int i, int j, int largest) {
        if (largest == 0) {
            aiPlaceTop(i, j);
        } else if (largest == 1) {
            aiPlaceBottom(i, j);
        } else if (largest == 2) {
            aiPlaceLeft(i, j);
        } else if (largest == 3) {
            aiPlaceRight(i, j);
        }
    }

    /**
     * Places a word to the top of a letter and assign the bytes to the array of
     * bytes to be sent back to client to decode the ai letters placed
     *
     * @param i row
     * @param j column
     */
    private void aiPlaceTop(int i, int j) {
        this.appendFront = false;

        long time = System.currentTimeMillis();
        // long end = time + 15000;
        long end = time + 10000;

        int maxLength = checkTopSpaces(i, j);

        // Create a word up down, last letter board[i][j]
        letterAppend = board[i][j].getLetter();

        while (System.currentTimeMillis() < end) {
            wordCombinationAppend(0, maxLength, true);
        }

        // If no word found to be create, set all bytes to 0
        if (wordCreated.equals("")) {
            for (int byteIndex = 1; byteIndex < aiPlay.length; byteIndex++) {
                aiPlay[byteIndex] = (byte) 0;
            }
            for (int rackIndex = 0; rackIndex < rack.length; rackIndex++) {
                bag.add(rack[rackIndex]);
                rack[rackIndex] = currentGame.getTileBag();
            }
            System.out.println("No word created");
            return;
        }

        // Sets the word created by ai on to the board
        int row = i;
        int col = j;
        int wordCount = 2;
        for (int x = 0; x < (wordCreated.length() - 1); x++) {
            this.board[row - 1][col] = new TileBean(wordCreated.charAt(wordCreated.length() - wordCount));
            row--;
            wordCount++;
        }

        aiPlay[1] = (byte) (wordCreated.length() - 1);

        // Assigns to the aiPlay byte[] the positions and chars of the created
        // word by AI
        int rowCheck = i - 1;
        int colCheck = j;
        int rowAndCol = 0;
        int wordCountTemp = 2;
        this.row = new int[wordCreated.length() - 1];
        this.col = new int[wordCreated.length() - 1];
        this.row[0] = i;
        this.col[0] = j;
        int index = 0;
        for (int byteIndex = 2; byteIndex < 23; byteIndex++) {
            if (byteIndex < (((wordCreated.length() - 1) * 3) + 2)) {
                if (rowAndCol < 2) {
                    if (rowAndCol == 0) {
                        aiPlay[byteIndex] = (byte) rowCheck;
                        this.row[index] = rowCheck;
                        rowCheck--;
                    } else {
                        this.col[index] = colCheck;
                        index++;
                        aiPlay[byteIndex] = (byte) colCheck;
                    }
                    rowAndCol++;
                } else {
                    aiPlay[byteIndex] = currentGame.charToByte.get(this.wordCreated.charAt(wordCreated.length() - wordCountTemp));
                    rowAndCol = 0;
                    wordCountTemp++;
                }
            } else {
                aiPlay[byteIndex] = (byte) 0;
            }
        }

        //Rearrange row and col to calculate points properly
        int[] tempRow = this.row;
        int[] tempCol = this.col;
        this.row = new int[this.row.length];
        this.col = new int[this.col.length];
        int indexer = 0;
        for (int tempIndex = tempRow.length - 1; tempIndex >= 0; tempIndex--) {
            this.row[indexer] = tempRow[tempIndex];
            this.col[indexer] = tempCol[tempIndex];
            indexer++;
        }
    }

    /**
     * Places a word to the bottom of a letter and assign the bytes to the array
     * of bytes to be sent back to client to decode the ai letters placed
     *
     * @param i row
     * @param j column
     */
    private void aiPlaceBottom(int i, int j) {
        this.appendFront = true;

        long time = System.currentTimeMillis();
        // long end = time + 15000;
        long end = time + 10000;

        int maxLength = checkBottomSpaces(i, j);

        // Create a word up down, last letter board[i][j]
        letterAppend = board[i][j].getLetter();

        while (System.currentTimeMillis() < end) {
            wordCombinationAppend(0, maxLength, false);
        }

        // If no word found to be create, set all bytes to 0
        if (wordCreated.equals("")) {
            for (int byteIndex = 1; byteIndex < aiPlay.length; byteIndex++) {
                aiPlay[byteIndex] = (byte) 0;
            }
            for (int rackIndex = 0; rackIndex < rack.length; rackIndex++) {
                bag.add(rack[rackIndex]);
                rack[rackIndex] = currentGame.getTileBag();
            }
            return;
        }

        // Sets the word created by ai on to the board
        int row = i;
        int col = j;
        int wordCount = 1;
        for (int x = 0; x < (wordCreated.length() - 1); x++) {
            this.board[row + 1][col] = new TileBean(wordCreated.charAt(wordCount));
            row++;
            wordCount++;
        }

        aiPlay[1] = (byte) (wordCreated.length() - 1);

        // Assigns to the aiPlay byte[] the positions and chars of the created
        // word by AI
        int rowCheck = i + 1;
        int colCheck = j;
        int rowAndCol = 0;
        int indexLetter = 1;
        this.row = new int[wordCreated.length() - 1];
        this.col = new int[wordCreated.length() - 1];
        this.row[0] = i;
        this.col[0] = j;
        int index = 0;
        for (int byteIndex = 2; byteIndex < 23; byteIndex++) {
            if (byteIndex < (((wordCreated.length() - 1) * 3) + 2)) {
                if (rowAndCol < 2) {
                    if (rowAndCol == 0) {
                        aiPlay[byteIndex] = (byte) rowCheck;
                        this.row[index] = rowCheck;
                        rowCheck++;
                    } else {
                        this.col[index] = colCheck;
                        index++;
                        aiPlay[byteIndex] = (byte) colCheck;
                    }
                    rowAndCol++;
                } else {
                    aiPlay[byteIndex] = currentGame.charToByte.get(this.wordCreated.charAt(indexLetter));
                    rowAndCol = 0;
                    indexLetter++;
                }
            } else {
                aiPlay[byteIndex] = (byte) 0;
            }
        }
    }

    /**
     * Places a word to the left of a letter and assign the bytes to the array
     * of bytes to be sent back to client to decode the ai letters placed
     *
     * @param i row
     * @param j column
     */
    private void aiPlaceLeft(int i, int j) {
        this.appendFront = false;

        long time = System.currentTimeMillis();
        // long end = time + 15000;
        long end = time + 10000;

        int maxLength = checkLeftSpaces(i, j);

        // Create a word up down, last letter board[i][j]
        letterAppend = board[i][j].getLetter();

        while (System.currentTimeMillis() < end) {
            wordCombinationAppend(0, maxLength, true);
        }

        // If no word found to be create, set all bytes to 0
        if (wordCreated.equals("")) {
            for (int byteIndex = 1; byteIndex < aiPlay.length; byteIndex++) {
                aiPlay[byteIndex] = (byte) 0;
            }
            for (int rackIndex = 0; rackIndex < rack.length; rackIndex++) {
                bag.add(rack[rackIndex]);
                rack[rackIndex] = currentGame.getTileBag();
            }
            return;
        }

        // Sets the word created by ai on to the board
        int row = i;
        int col = j;
        int wordCount = 2;
        for (int x = 0; x < (wordCreated.length() - 1); x++) {
            this.board[row][col - 1] = new TileBean(wordCreated.charAt(wordCreated.length() - wordCount));
            col--;
            wordCount++;
        }

        aiPlay[1] = (byte) (wordCreated.length() - 1);

        // Assigns to the aiPlay byte[] the positions and chars of the created
        // word by AI
        int rowCheck = i;
        int colCheck = j - 1;
        int rowAndCol = 0;
        int wordCountTemp = 2;
        this.row = new int[wordCreated.length() - 1];
        this.col = new int[wordCreated.length() - 1];
        this.row[0] = i;
        this.col[0] = j;
        int index = 0;
        for (int byteIndex = 2; byteIndex < 23; byteIndex++) {
            if (byteIndex < (((wordCreated.length() - 1) * 3) + 2)) {
                if (rowAndCol < 2) {
                    if (rowAndCol == 0) {
                        aiPlay[byteIndex] = (byte) rowCheck;
                        this.row[index] = rowCheck;
                    } else {
                        aiPlay[byteIndex] = (byte) colCheck;
                        this.col[index] = colCheck;
                        index++;
                        colCheck--;
                    }
                    rowAndCol++;
                } else {
                    aiPlay[byteIndex] = currentGame.charToByte.get(this.wordCreated.charAt(wordCreated.length() - wordCountTemp));
                    rowAndCol = 0;
                    wordCountTemp++;
                }
            } else {
                aiPlay[byteIndex] = (byte) 0;
            }
        }

        //Rearrange row and col to calculate points properly
        int[] tempRow = this.row;
        int[] tempCol = this.col;
        this.row = new int[this.row.length];
        this.col = new int[this.col.length];
        int indexer = 0;
        for (int tempIndex = tempRow.length - 1; tempIndex >= 0; tempIndex--) {
            this.row[indexer] = tempRow[tempIndex];
            this.col[indexer] = tempCol[tempIndex];
            indexer++;
        }
    }

    /**
     * Places a word to the right of a letter and assign the bytes to the array
     * of bytes to be sent back to client to decode the ai letters placed
     *
     * @param i row
     * @param j column
     */
    private void aiPlaceRight(int i, int j) {
        this.appendFront = true;

        long time = System.currentTimeMillis();
        // long end = time + 15000;
        long end = time + 10000;

        int maxLength = checkRightSpaces(i, j);

        // Create a word up down, last letter board[i][j]
        letterAppend = board[i][j].getLetter();

        while (System.currentTimeMillis() < end) {
            wordCombinationAppend(0, maxLength, false);
        }

        // If no word found to be create, set all bytes to 0
        if (wordCreated.equals("")) {
            for (int byteIndex = 1; byteIndex < aiPlay.length; byteIndex++) {
                aiPlay[byteIndex] = (byte) 0;
            }
            for (int rackIndex = 0; rackIndex < rack.length; rackIndex++) {
                bag.add(rack[rackIndex]);
                rack[rackIndex] = currentGame.getTileBag();
            }
            return;
        }

        // Sets the word created by ai on to the board
        int row = i;
        int col = j;
        int wordCount = 1;
        for (int x = 0; x < (wordCreated.length() - 1); x++) {
            this.board[row][col + 1] = new TileBean(wordCreated.charAt(wordCount));
            col++;
            wordCount++;
        }

        aiPlay[1] = (byte) (wordCreated.length() - 1);

        // Assigns to the aiPlay byte[] the positions and chars of the created
        // word by AI
        int rowCheck = i;
        int colCheck = j + 1;
        int rowAndCol = 0;
        int indexLetter = 1;
        this.row = new int[wordCreated.length() - 1];
        this.col = new int[wordCreated.length() - 1];
        this.row[0] = i;
        this.col[0] = j;
        int index = 0;
        for (int byteIndex = 2; byteIndex < 23; byteIndex++) {
            if (byteIndex < (((wordCreated.length() - 1) * 3) + 2)) {
                if (rowAndCol < 2) {
                    if (rowAndCol == 0) {
                        aiPlay[byteIndex] = (byte) rowCheck;
                        this.row[index] = rowCheck;
                    } else {
                        aiPlay[byteIndex] = (byte) colCheck;
                        this.col[index] = colCheck;
                        index++;
                        colCheck++;
                    }
                    rowAndCol++;
                } else {
                    aiPlay[byteIndex] = currentGame.charToByte.get(this.wordCreated.charAt(indexLetter));
                    rowAndCol = 0;
                    indexLetter++;
                }
            } else {
                aiPlay[byteIndex] = (byte) 0;
            }
        }
    }

    /**
     * Finds a combination of words in which you append a constant letter to the
     * front or end of the word
     *
     * @param start
     * @param maxLength
     * @param boolean placeFront
     */
    private void wordCombinationAppend(int start, int maxLength, boolean placeFront) {
        for (int i = start; i < rackString.length(); ++i) {
            wordCombinations.append(rackString.charAt(i));
            String tempWordCreated = wordCombinations.toString();
            if (placeFront) {
                tempWordCreated = tempWordCreated + letterAppend;
            }
            else {
                tempWordCreated = letterAppend + tempWordCreated;
            }
            DictionService dictService = new DictionService();

            if (dictService.getDictionWord(tempWordCreated) != null) {
                if (tempWordCreated.length() <= maxLength && wordCreated.length() < tempWordCreated.length()) {
                    wordCreated = tempWordCreated;
                }
            }
            if (i < rackString.length()) {
                wordCombinationAppend(i + 1, maxLength, placeFront);
            }
            wordCombinations.setLength(wordCombinations.length() - 1);
        }
    }

    /**
     * Finds the number of valid spaces given a tile position
     *
     * @param row
     * @param col
     * @return valid spaces to the top of a tile
     */
    private int checkTopSpaces(int row, int col) {
        int spaceCount = 0;
        if (row >= 1 && row <= 13 && board[row + 1][col] == null) {
            for (int i = row - 1; i >= 0; i--) {
                if ((col - 1) < 0 || (col + 1) > 14 || i < 0) {
                    return spaceCount;
                }
                if (board[i][col] == null) {
                    if (board[i][col - 1] != null || board[i][col + 1] != null) {
                        spaceCount = 0;
                        break;
                    }
                    if (i > 0) {
                        if (board[i][col] != null) {
                            spaceCount--;
                            break;
                        }
                    }
                    spaceCount++;
                } else {
                    break;
                }
            }
        }
        return spaceCount;
    }

    /**
     * Finds the number of valid spaces given a tile position
     *
     * @param row
     * @param col
     * @return valid spaces to the bottom of a tile
     */
    private int checkBottomSpaces(int row, int col) {
        int spaceCount = 0;
        if (row >= 1 && row <= 13 && board[row - 1][col] == null) {
            for (int i = row + 1; i <= 14; i++) {
                if ((col - 1) < 0 || (col + 1) > 14 || i > 14) {
                    return spaceCount;
                }
                if (board[i][col] == null) {
                    if (board[i][col - 1] != null || board[i][col + 1] != null) {
                        spaceCount = 0;
                        break;
                    }
                    if (i < 14) {
                        if (board[i][col] != null) {
                            spaceCount--;
                            break;
                        }
                    }
                    spaceCount++;
                } else {
                    break;
                }
            }
        }
        return spaceCount;
    }

    /**
     * Finds the number of valid spaces given a tile position
     *
     * @param row
     * @param col
     * @return valid spaces to the left of a tile
     */
    private int checkLeftSpaces(int row, int col) {
        int spaceCount = 0;
        if (col >= 1 && col <= 13 && board[row][col + 1] == null) {
            for (int i = col - 1; i >= 0; i--) {
                if ((row - 1) < 0 || (row + 1) > 14 || i < 0) {
                    return spaceCount;
                }
                if (board[row - 1][i] != null || board[row + 1][i] != null) {
                    spaceCount = 0;
                    break;
                }
                if (board[row][i] == null) {
                    if (i > 0) {
                        if (board[row][i] != null) {
                            spaceCount--;
                            break;
                        }
                    }
                    spaceCount++;
                } else {
                    break;
                }
            }
        }
        return spaceCount;
    }

    /**
     * Finds the number of valid spaces given a tile position
     *
     * @param row
     * @param col
     * @return valid spaces to the right of a tile
     */
    private int checkRightSpaces(int row, int col) {
        int spaceCount = 0;
        if (col >= 1 && col <= 13 && board[row][col - 1] == null) {
            for (int i = col + 1; i <= 14; i++) {
                if ((row - 1) < 0 || (row + 1) > 14 || i > 14) {
                    return spaceCount;
                }
                if (board[row - 1][i] != null || board[row + 1][i] != null) {
                    spaceCount = 0;
                    return spaceCount;
                }
                if (board[row][i] == null) {
                    if (i < 14) {
                        if (board[row][i] != null) {
                            spaceCount--;
                            return spaceCount;
                        }
                    }
                    spaceCount++;
                } else {
                    return spaceCount;
                }
            }
        }
        return spaceCount;
    }

    /**
     * Created the first word of an AI
     */
    private void createFirstWord() {
        long time = System.currentTimeMillis();
//        long end = time + 15000;
        long end = time + 10000;
        while (System.currentTimeMillis() < end) {
            firstWordCombination(0);
        }
        int row = 7;
        int col = 7;
        for (int i = 0; i < wordCreated.length(); i++) {
            this.board[row][col] = new TileBean(wordCreated.charAt(i));
            col++;
        }
    }

    /**
     * Finds the combination of ai first word placed (on star)
     *
     * @param start
     */
    private void firstWordCombination(int start) {
        for (int i = start; i < rackString.length(); ++i) {
            wordCombinations.append(rackString.charAt(i));
            String tempWordCreated = wordCombinations.toString();
            DictionService dictService = new DictionService();
            if (dictService.getDictionWord(tempWordCreated) != null) {
                if (wordCreated.length() < tempWordCreated.length()) {
                    wordCreated = tempWordCreated;
                }
            }
            if (i < rackString.length()) {
                firstWordCombination(i + 1);
            }
            wordCombinations.setLength(wordCombinations.length() - 1);
        }
    }
}
