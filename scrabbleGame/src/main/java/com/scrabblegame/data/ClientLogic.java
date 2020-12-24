package com.scrabblegame.data;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 *
 * @author Stephen He
 * @author Victor Ouy
 * @author Lucas
 */
public class ClientLogic {

    private final Socket socket;
    private final DataInputStream in;
    private final OutputStream out;
    private HashMap<Character, Byte> charToByte;
    private HashMap<Byte, Character> byteToChar;
    private GameState currentGame;

    /**
     * This creates a connection to the server Please deal with checking valid
     * server/port before calling the constructor
     *
     * @param socket The socket to connect
     * @throws IOException if anything goes wrong
     */
    public ClientLogic(Socket socket, GameState currGame) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(this.socket.getInputStream());
        this.out = socket.getOutputStream();
        setHashMaps();
        this.currentGame = currGame;
    }
    
    /**
     * This method talks to server to request starting hand of user
     */
    public void settingLetters() {
        byte[] byteArr = new byte[23];
        byteArr[0] = 2;
        byteArr[1] = 7;
        
        for (int i = 2; i < byteArr.length; i++) {
            byteArr[i] = 0;
        }
        try {
            out.write(byteArr);
            in.readFully(byteArr);
            decodeServerUpdateAll(byteArr);
        } 
        catch (IOException e) {
            System.out.println("Error when data communication with server");
        }
    }

    /**
     * Sends bytes data to server side representing info, # letters players,
     * position of letters, and the letters played. Receives back score of user
     * round
     *
     * @param lettersPlayed
     * @param rowPos
     * @param colPos
     * @return int of score of round played; calculated on server side
     */
    public void playLetters(char[] lettersPlayed, int[] rowPos, int[] colPos) {
        //ENCODE DATA
        byte[] byteArr = new byte[23];
        byteArr[0] = 0;
        byteArr[1] = (byte) lettersPlayed.length;

        int rowAndCol = 0;
        int indexLetters = 0;

        for (int i = 2; i < 23; i++) {
            // lettersPlayed.length * 3) + 2 are the length of indexes that must be filled
            // with data, the rest is 0
            if (i < ((lettersPlayed.length * 3) + 2)) {
                if (rowAndCol < 2) {
                    //First set the coordinates
                    if (rowAndCol == 0) {
                        byteArr[i] = (byte) rowPos[indexLetters];
                    } else {
                        byteArr[i] = (byte) colPos[indexLetters];
                    }
                    rowAndCol++;
                } else {
                    //Set the char value and reset coordinates counter to 0
                    byteArr[i] = this.charToByte.get(lettersPlayed[indexLetters]);
                    rowAndCol = 0;
                    //Increment index to next character
                    indexLetters++;
                }
            } else {
                //Fill everything else with 0
                byteArr[i] = 0;
            }
        }

        //SEND AND RECEIVE
        try {
            out.write(byteArr);
            
            //We expect 2 retturns from server
            in.readFully(byteArr);
            decodeReturnBytes(byteArr);

            in.readFully(byteArr);
            decodeReturnBytes(byteArr);
        } catch (IOException e) {
            System.out.println("Error when data communication with server");
        }
    }

    public void swapLettersServer(char[] lettersSwapped) {
        byte[] byteArr = new byte[23];
        byteArr[0] = 1;
        byteArr[1] = (byte) lettersSwapped.length;
        
        for (int i = 2; i < 23; i++) {
            if (i < (lettersSwapped.length + 2)) {
                byteArr[i] = this.charToByte.get(lettersSwapped[i - 2]);
            }
            else {
                byteArr[i] = 0;
            }
        }
        
        try {
            out.write(byteArr);
            
            // We expect 2 retturns from server 
            in.readFully(byteArr);
            decodeReturnBytes(byteArr);

            in.readFully(byteArr);
            decodeReturnBytes(byteArr);
        } 
        catch (IOException e) {
            System.out.println("Error when data communication with server");
        }
    }
    
    /**
     * Calls the appropriate private method to decode the byte array
     *
     * @param byteArr The byte array we need to decode
     */
    private void decodeReturnBytes(byte[] byteArr) throws IOException {
        //Depending on the initial byte, we will be doing something different
        switch (byteArr[0]) {
            case 0:
                decodeServerPlay(byteArr);
                break;
            case 1:
                decodeServerUpdateAll(byteArr);
                break;
            case 2:
                decodeServerUpdateAll(byteArr);
                this.currentGame.endGame();
                break;
        }
    }

    /**
     * Decodes the byte array where the server sends us information to update
     * our board after it plays. Calls method in GameState after decoding to
     * update.
     *
     * @param byteArr the byte array received from server
     */
    private void decodeServerPlay(byte[] byteArr) {
        //Get the amount of characters played by server
        int played = (int) byteArr[1];
        int readUpto = (played * 3) + 2;

        int[] rowsAi = new int[played];
        int[] colsAi = new int[played];
        char[] charsAi = new char[played];

        int rowAndCol = 0;
        int indexLetters = 0;

        for (int i = 2; i < readUpto; i++) {
            if (rowAndCol < 2) {
                //First set the coordinates
                if (rowAndCol == 0) {
                    rowsAi[indexLetters] = byteArr[i];
                } else {
                    colsAi[indexLetters] = byteArr[i];
                }
                rowAndCol++;
            } else {
                //Set the char value and reset coordinates counter to 0
                charsAi[indexLetters] = this.byteToChar.get(byteArr[i]);
                rowAndCol = 0;
                //Increment index to next character
                indexLetters++;;
            }
        }

        this.currentGame.setReturnAiPlay(rowsAi, colsAi, charsAi);
    }

    /**
     * Decodes the byte array where the server sends us information to update
     * our game Calls method in GameState after decoding to update.
     *
     * @param byteArr the byte array received from server
     */
    private void decodeServerUpdateAll(byte[] byteArr) {
        int aiScoreIncrease = (int) byteArr[1];
        int clientScoreIncrease = (int) byteArr[2];
        int bag = (int) byteArr[3];

        int receivedBackCount = (int) byteArr[4];
        char[] receivedBack = new char[receivedBackCount];

        for (int i = 5; i < 5 + receivedBackCount; i++) {
            receivedBack[i - 5] = this.byteToChar.get(byteArr[i]);
        }

        this.currentGame.setScoresAndUserTilesPostRound(aiScoreIncrease, clientScoreIncrease,
                receivedBack, bag);
    }

    /**
     * Closes the socket Should be called when game is over
     *
     * Call it like this: this.conn.closeConnection();
     */
    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException exception) {
            System.exit(0);
        }
    }

    /**
     * Sets the Character to Byte dictionary for our byte array later Sets the
     * Byte to Character dictionary for our char array later
     */
    private void setHashMaps() {
        // For encoding send
        this.charToByte = new HashMap<Character, Byte>();
        byte byteValue = 0;
        this.charToByte.put(' ', byteValue);
        char charKey = 'a';
        byteValue++;
        while (charKey <= 'z') {
            charToByte.put(charKey, byteValue);
            charKey++;
            byteValue++;
        }

        // For decoding receive
        this.byteToChar = new HashMap<Byte, Character>();
        byte byteKey = 0;
        this.byteToChar.put(byteKey, ' ');
        char charValue = 'a';
        byteKey++;
        while (charValue <= 'z') {
            byteToChar.put(byteKey, charValue);
            charValue++;
            byteKey++;
        }
    }
}
