package com.scrabblegame.scrabbleserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Stephen He
 * @author Victor Ouy
 * @author Lucas
 */
public class ServerGameState {

    private HashMap<Character, Integer> charToValue;
    private HashMap<Byte, Character> byteToChar;
    private HashMap<String, String> modifiers;
    private ScrabbleAI scrabbleAi;
    private int clientPts;
    private int clientLatestPts;
    // Public since it is shared with ScrabbleAI
    public ArrayList<TileBean> bag;
    public TileBean[][] board;
    public HashMap<Character, Byte> charToByte;

    public ServerGameState() {
        bag = new ArrayList<TileBean>();
        fillBag();
        setCharToValue();
        setHashMaps();
        clientPts = 0;
        clientLatestPts = 0;
        board = new TileBean[15][15];
        scrabbleAi = new ScrabbleAI(this);
        scrabbleAi.setRack();
    }
    
    public ArrayList<TileBean> getBag() {
        return this.bag;
    }
    
    public byte[] aiMoves() {
        return scrabbleAi.playWord();
    }

    /**
     * Adds a tileByte
     *
     * @param byteLetter Byte
     */
    public void addTileByte(byte byteLetter) {
        bag.add(new TileBean(this.byteToChar.get(byteLetter)));
    }
    
    /**
     * Draws a random tile bean from the bag
     *
     * @return The byte representation
     */
    public TileBean getTileBag() {
        int randomIndex = ThreadLocalRandom.current().nextInt(0, bag.size());
        TileBean randomTile = bag.get(randomIndex);
        bag.remove(randomIndex);
        return randomTile;
    }

    /**
     * Draws a random tile from the bag in byte
     *
     * @return The byte representation
     */
    private byte getRandomTileByte() {
        int randomIndex = ThreadLocalRandom.current().nextInt(0, bag.size());
        byte randomTile = this.charToByte.get(bag.get(randomIndex).getLetter());
        bag.remove(randomIndex);
        return randomTile;
    }

    /**
     * Calculates the points of the word placed by ai
     * 
     * @param word
     * @return ai pts played
     */
    public int calcAiWord(String word, ArrayList<Integer> row, ArrayList<Integer> col, boolean appendFront) {
        int pts = 0;
        
        for (int i = 0; i < word.length(); i++) {
            pts += charToValue.get(word.charAt(i));
        }
        return pts;
    }
    
    /**
     * Sets the byte[] of points and letters returned
     * 
     * @param receivedBuff
     * @return byte[] of updated games state to client
     */
    public byte[] sendUpdateGameState(byte[] receivedBuff) {
        // Client swapped letters and updated scores
        byte[] sendBuffSwap = new byte[receivedBuff.length];
        sendBuffSwap[0] = 1;

        sendBuffSwap[1] = (byte) scrabbleAi.getLatestPts();

        sendBuffSwap[2] = (byte) clientLatestPts;

        // Amount of letters to send back
        int numLettersSwap = receivedBuff[1];
        sendBuffSwap[4] = (byte) numLettersSwap;

        boolean bagEmpty = false;
        // Adds swapped tile to bag and retrieves random one
        for (int i = 5; i < 23; i++) {
            if (i < (numLettersSwap + 5)) {
                //Only add back to bag if it's a swap
                if (receivedBuff[0] == 1) {
                    addTileByte(receivedBuff[i - 3]);
                }
                if (bag.size() <= 0) {
                    bagEmpty = true;
                    break;
                }
                byte byteToPlace = getRandomTileByte();
                sendBuffSwap[i] = byteToPlace;
            } else {
                sendBuffSwap[i] = 0;
            }
        }
        
        if (bagEmpty == true) {
            sendBuffSwap[0] = 2;
            for (int i = 3; i < 23; i++) {
                sendBuffSwap[i] = 0;
            }
            return sendBuffSwap;
        }

        // Current size of bag (do this at end, after bag got updated above
        sendBuffSwap[3] = (byte) this.bag.size();
        
        //Reset the points curr Points of AI and User
        //We do this because if the user passes, we want to reset the points to 0
        this.clientLatestPts = 0;
        this.scrabbleAi.resetLatestPts();
        
        return sendBuffSwap;
    }

    /**
     * Places the tiles where the client played Calculate the points and store it
     *
     * @param receiveBuff The return byte[] from the client
     */
    public void processClientPlay(byte[] receiveBuff) {
        //We assume the characters are already in order. It was sorted and validated in client side
        int[] row = new int[receiveBuff[1]];
        int[] col = new int[receiveBuff[1]];
        char[] characters = new char[receiveBuff[1]];

        int rowAndCol = 0;
        int indexLetter = 0;

        for (int i = 2; i < (receiveBuff[1] * 3) + 2; i++) {
            if (rowAndCol < 2) {
                //First set the coordinates
                if (rowAndCol == 0) {
                    row[indexLetter] = receiveBuff[i];
                } else {
                    col[indexLetter] = receiveBuff[i];
                }
                rowAndCol++;
            } else {
                //Set the char value and reset coordinates counter to 0
                characters[indexLetter] = this.byteToChar.get(receiveBuff[i]);
                rowAndCol = 0;
                //Increment index to next character
                indexLetter++;;
            }

        }
        placeOnBoard(row, col, characters);
        clientLatestPts = calculatePoints(row, col);
        clientPts += clientLatestPts;
    }

    /**
     * Calculate the points of a play
     *
     * @param row Row positions of word
     * @param col Column position of word
     */
    public int calculatePoints(int[] row, int[] col) {
        int pts = 0;
        if (checkStraightLine(row)) {            
            pts += calculatePointsHorizontal(row, col);
            for (int i = 0; i < row.length; i++) {
                pts += calculatePointsVertical(row[i], col[i]);
            }
        } else if (checkStraightLine(col)) {
            pts = calculatePointsVertical(row, col);
            for (int i = 0; i < row.length; i++) {
                pts += calculatePointsHorizontal(row[i], col[i]);
            }
        }
        return pts;
    }

    /**
     * Calculate the points of a word Horizontally
     *
     * @param row Row positions of the word
     * @param col Column positions of the word
     * @return The points
     */
    private int calculatePointsHorizontal(int[] row, int[] col) {
        int pts = 0;
        int wordMultiplier = 1;

        for (int i = 0; i < row.length; i++) {
            wordMultiplier = wordMultiplier * getWordModifier(row[i] + ";" + col[i]);
            int letterMultiplier = 1 * getLetterModifier(row[i] + ";" + col[i]);
            pts += charToValue.get(board[row[i]][col[i]].getLetter()) * letterMultiplier;
            
        }

        //We now calculate the points inbetween that was not part of the client's play
        int currCol = col[0];
        int endCol = col[col.length - 1];

        while (currCol < endCol) {
            currCol++;
            boolean toAdd = true;
            //Go through our existing columns
            for (int column : col) {

                //We check to see if that column ever appears in our array.
                //If it does, that means the player placed it, so we already calculated its points
                //If not, we must add its points to our word, but we do not calculate it's modifiers
                if (currCol == column) {
                    toAdd = false;
                }
            }

            if (toAdd) {
                pts += this.charToValue.get(board[row[0]][currCol].getLetter());
            }

        }

        //Now we calculate the points from all the letters next to it (left/right)
        int singleRow = row[0];

        //First we check left (decreasing row)
        int currentCol = col[0];

        boolean keepChecking = true;
        while (keepChecking) {
            currentCol--;
            if (currentCol >= 0 && board[singleRow][currentCol] != null) {
                pts += this.charToValue.get(board[singleRow][currentCol].getLetter());
            } else {
                keepChecking = false;
            }
        }

        //Now we check right
        currentCol = col[col.length - 1];
        keepChecking = true;
        while (keepChecking) {
            currentCol++;
            if (currentCol <= 14 && board[singleRow][currentCol] != null) {
                pts += this.charToValue.get(board[singleRow][currentCol].getLetter());
            } else {
                keepChecking = false;
            }
        }
        return pts * wordMultiplier;
    }

    /**
     * Calculate the points of a word vertically
     *
     * @param row Row positions of the word
     * @param col Column positions of the word
     * @return The points
     */
    private int calculatePointsVertical(int[] row, int[] col) {
        int pts = 0;
        int wordMultiplier = 1;

        for (int i = 0; i < row.length; i++) {
            wordMultiplier = wordMultiplier * getWordModifier(row[i] + ";" + col[i]);
            int letterMultiplier = 1 * getLetterModifier(row[i] + ";" + col[i]);

            pts += charToValue.get(board[row[i]][col[i]].getLetter()) * letterMultiplier;
        }

        //We now calculate the points inbetween that was not part of the client's play
        int currRow = row[0];
        int endRow = row[row.length - 1];

        while (currRow < endRow) {
            currRow++;
            //Go through our existing rows
            boolean toAdd = true;
            for (int checkRow : row) {
                //We check to see if that row ever appears in our array.
                //If it does, that means the player placed it, so we already calculated its points
                //If not, we must add its points to our word, but we do not calculate it's modifiers
                if (currRow == checkRow) {
                    toAdd = false;
                }
            }
            if (toAdd) {
                pts += this.charToValue.get(board[currRow][col[0]].getLetter());
            }

        }

        //Now we calculate the points from all the letters next to it
        int singleCol = col[0];

        //First check top
        int currentRow = row[0];

        boolean keepChecking = true;
        while (keepChecking) {
            currentRow--;
            if (currentRow >= 0 && board[currentRow][singleCol] != null) {
                pts += this.charToValue.get(board[currentRow][singleCol].getLetter());
            } else {
                keepChecking = false;
            }
        }

        //Now we check bottom
        currentRow = row[row.length - 1];
        keepChecking = true;
        while (keepChecking) {
            currentRow++;
            if (currentRow <= 14 && board[currentRow][singleCol] != null) {
                System.out.println("currRow: "+currRow);
                System.out.println("letter: "+board[currentRow][singleCol].getLetter());
                pts += this.charToValue.get(board[currentRow][singleCol].getLetter());
            } else {
                keepChecking = false;
            }
        }
        
        return pts * wordMultiplier;
    }

    /**
     * Overloaded method This is called to calculate points of subsequent words
     * (side effect words) from a certain character
     *
     * @param row of character
     * @param col of character
     * @return points
     */
    private int calculatePointsHorizontal(int row, int col) {
        int pts = 0;
        int wordMultiplier = 1;

        //Look left first
        int currCol = col;
        boolean keepLooking = true;
        while (keepLooking) {
            currCol--;
            if (currCol >= 0 && board[row][currCol] != null) {
                pts += this.charToValue.get(board[row][currCol].getLetter());
            } else {
                keepLooking = false;
            }
        }

        //Look right after
        currCol = col;
        keepLooking = true;
        while (keepLooking) {
            currCol++;
            if (currCol <= 14 && board[row][currCol] != null) {
                pts += this.charToValue.get(board[row][currCol].getLetter());
            } else {
                keepLooking = false;
            }
        }

        //Only if pts!=0, do we recount the value of the played letter. (it makes a word so we should count it again)
        //It's modifiers apply
        if (pts != 0) {
            wordMultiplier = wordMultiplier * getWordModifier(row + ";" + col);
            int letterMultiplier = 1 * getLetterModifier(row + ";" + col);
            pts += charToValue.get(board[row][col].getLetter()) * letterMultiplier;
        }

        return pts * wordMultiplier;
    }

    /**
     * Overloaded method This is called to calculate points of subsequent words
     * (side effect words) from a certain character
     *
     * @param row of character
     * @param col of character
     * @return points
     */
    private int calculatePointsVertical(int row, int col) {
        int pts = 0;
        int wordMultiplier = 1;

        //Look top first
        int currRow = row;
        boolean keepLooking = true;
        while (keepLooking) {
            currRow--;
            if (currRow >= 0 && board[currRow][col] != null) {
                pts += this.charToValue.get(board[currRow][col].getLetter());
            } else {
                keepLooking = false;
            }
        }

        //Look bottom after
        currRow = row;
        keepLooking = true;
        while (keepLooking) {
            currRow++;
            if (currRow <= 14 && board[currRow][col] != null) {
                pts += this.charToValue.get(board[currRow][col].getLetter());
            } else {
                keepLooking = false;
            }
        }
        //Only if pts!=0, do we recount the value of the played letter. (it makes a word so we should count it again)
        //It's modifiers apply
        if (pts != 0) {
            wordMultiplier = wordMultiplier * getWordModifier(row + ";" + col);
            int letterMultiplier = 1 * getLetterModifier(row + ";" + col);
            pts += charToValue.get(board[row][col].getLetter()) * letterMultiplier;
        }

        return pts * wordMultiplier;
    }

    /**
     * Call this method to see which direction the word is going
     *
     * @param pos int[] that represents Row OR Column
     * @return Whether it is straight or not (depending on Row or Column @param)
     */
    private boolean checkStraightLine(int[] pos) {
        int expectedRow = pos[0];
        for (int i = 1; i < pos.length; i++) {
            if (expectedRow != pos[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Place tiles on our board
     *
     * @param row index
     * @param col index
     * @param letter placed character
     */
    private void placeOnBoard(int[] row, int[] col, char[] letter) {
        for (int i = 0; i < letter.length; i++) {
            TileBean toPlace = new TileBean(letter[i]);

            board[row[i]][col[i]] = toPlace;
        }
    }

    /**
     * Fills up the bag at the beginning of the game
     */
    private void fillBag() {
        for (int i = 0; i < 9; i++) {
            bag.add(new TileBean('a'));
        }

        for (int i = 0; i < 2; i++) {
            bag.add(new TileBean('b'));
        }

        for (int i = 0; i < 2; i++) {
            bag.add(new TileBean('c'));
        }

        for (int i = 0; i < 4; i++) {
            bag.add(new TileBean('d'));
        }

        for (int i = 0; i < 12; i++) {
            bag.add(new TileBean('e'));
        }

        for (int i = 0; i < 2; i++) {
            bag.add(new TileBean('f'));
        }

        for (int i = 0; i < 3; i++) {
            bag.add(new TileBean('g'));
        }

        for (int i = 0; i < 2; i++) {
            bag.add(new TileBean('h'));
        }

        for (int i = 0; i < 9; i++) {
            bag.add(new TileBean('i'));
        }

        for (int i = 0; i < 1; i++) {
            bag.add(new TileBean('j'));
        }

        for (int i = 0; i < 1; i++) {
            bag.add(new TileBean('k'));
        }

        for (int i = 0; i < 4; i++) {
            bag.add(new TileBean('l'));
        }

        for (int i = 0; i < 2; i++) {
            bag.add(new TileBean('m'));
        }

        for (int i = 0; i < 6; i++) {
            bag.add(new TileBean('n'));
        }

        for (int i = 0; i < 8; i++) {
            bag.add(new TileBean('o'));
        }

        for (int i = 0; i < 2; i++) {
            bag.add(new TileBean('p'));
        }

        for (int i = 0; i < 1; i++) {
            bag.add(new TileBean('q'));
        }

        for (int i = 0; i < 6; i++) {
            bag.add(new TileBean('r'));
        }

        for (int i = 0; i < 4; i++) {
            bag.add(new TileBean('s'));
        }

        for (int i = 0; i < 6; i++) {
            bag.add(new TileBean('t'));
        }

        for (int i = 0; i < 4; i++) {
            bag.add(new TileBean('u'));
        }

        for (int i = 0; i < 2; i++) {
            bag.add(new TileBean('v'));
        }

        for (int i = 0; i < 2; i++) {
            bag.add(new TileBean('w'));
        }

        for (int i = 0; i < 1; i++) {
            bag.add(new TileBean('x'));
        }

        for (int i = 0; i < 2; i++) {
            bag.add(new TileBean('y'));
        }

        for (int i = 0; i < 1; i++) {
            bag.add(new TileBean('z'));
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
        setModifiers();
    }

    /**
     * Sets up the coordinates to the appropriate modifier
     */
    private void setModifiers() {
        modifiers = new HashMap<String, String>();
        //Row 0
        modifiers.put("0;1", "DL");
        modifiers.put("0;4", "TW");
        modifiers.put("0;5", "TL");
        modifiers.put("0;7", "DW");
        modifiers.put("0;9", "TL");
        modifiers.put("0;10", "TW");
        modifiers.put("0;13", "DL");
        //Row 1
        modifiers.put("1;0", "TW");
        modifiers.put("1;1", "DW");
        modifiers.put("1;13", "DW");
        modifiers.put("1;14", "TW");
        //Row 2
        modifiers.put("2;3", "TL");
        modifiers.put("2;11", "TL");
        //Row 3
        modifiers.put("3;2", "TL");
        modifiers.put("3;5", "DW");
        modifiers.put("3;6", "DL");
        modifiers.put("3;8", "DL");
        modifiers.put("3;9", "DW");
        modifiers.put("3;12", "TL");
        //Row 4
        modifiers.put("4;1", "TL");
        modifiers.put("4;5", "DL");
        modifiers.put("4;9", "DL");
        modifiers.put("4;13", "TL");
        //Row 5
        modifiers.put("5;0", "TL");
        modifiers.put("5;3", "DW");
        modifiers.put("5;4", "DL");
        modifiers.put("5;6", "TL");
        modifiers.put("5;8", "TL");
        modifiers.put("5;10", "DL");
        modifiers.put("5;11", "DW");
        modifiers.put("5;14", "TL");
        //Row 6
        modifiers.put("6;3", "DL");
        modifiers.put("6;11", "DL");
        //Row 7
        modifiers.put("7;0", "DW");
        modifiers.put("7;4", "DW");
        modifiers.put("7;7", "ST");
        modifiers.put("7;10", "DW");
        modifiers.put("7;14", "DW");
        //Row 8
        modifiers.put("8;3", "DL");
        modifiers.put("8;11", "DL");
        //Row 9
        modifiers.put("9;0", "TL");
        modifiers.put("9;3", "DW");
        modifiers.put("9;4", "DL");
        modifiers.put("9;6", "TL");
        modifiers.put("9;8", "TL");
        modifiers.put("9;10", "DL");
        modifiers.put("9;11", "DW");
        modifiers.put("9;14", "TL");
        //Row 10
        modifiers.put("10;1", "TL");
        modifiers.put("10;5", "DL");
        modifiers.put("10;9", "DL");
        modifiers.put("10;13", "TL");
        //Row 11
        modifiers.put("11;2", "TL");
        modifiers.put("11;5", "DW");
        modifiers.put("11;6", "DL");
        modifiers.put("11;8", "DL");
        modifiers.put("11;9", "DW");
        modifiers.put("11;12", "TL");
        //Row 12
        modifiers.put("12;3", "TL");
        modifiers.put("12;11", "TL");
        //Row 13
        modifiers.put("13;0", "TW");
        modifiers.put("13;1", "DW");
        modifiers.put("13;13", "DW");
        modifiers.put("13;14", "TW");
        //Row 14
        modifiers.put("14;1", "DL");
        modifiers.put("14;4", "TW");
        modifiers.put("14;5", "TL");
        modifiers.put("14;7", "DW");
        modifiers.put("14;9", "TL");
        modifiers.put("14;10", "TW");
        modifiers.put("14;13", "DL");
    }

    /**
     * Gets the word modifier of the tile
     *
     * @param pos Position of the tile
     * @return modifier value
     */
    private int getWordModifier(String pos) {
        String modifier = this.modifiers.get(pos);

        if (modifier == null) {
            return 1;
        }

        switch (modifier) {
            case "DW":
                return 2;
            case "TW":
                return 3;
            default:
                return 1;
        }
    }

    /**
     * Gets the Letter modifier of the tile
     *
     * @param pos Position of the tile
     * @return modifier value
     */
    private int getLetterModifier(String pos) {
        String modifier = this.modifiers.get(pos);

        if (modifier == null) {
            return 1;
        }

        switch (modifier) {
            case "DL":
                return 2;
            case "TL":
                return 3;
            default:
                return 1;
        }
    }

    /**
     * Sets up the hash map that binds character to their value
     */
    private void setCharToValue() {
        charToValue = new HashMap<Character, Integer>();

        charToValue.put(' ', 0);
        charToValue.put('a', 1);
        charToValue.put('b', 3);
        charToValue.put('c', 3);
        charToValue.put('d', 2);
        charToValue.put('e', 1);
        charToValue.put('f', 4);
        charToValue.put('g', 2);
        charToValue.put('h', 4);
        charToValue.put('i', 1);
        charToValue.put('j', 8);
        charToValue.put('k', 5);
        charToValue.put('l', 1);
        charToValue.put('m', 3);
        charToValue.put('n', 1);
        charToValue.put('o', 1);
        charToValue.put('p', 3);
        charToValue.put('q', 10);
        charToValue.put('r', 1);
        charToValue.put('s', 1);
        charToValue.put('t', 1);
        charToValue.put('u', 1);
        charToValue.put('v', 4);
        charToValue.put('w', 4);
        charToValue.put('x', 8);
        charToValue.put('y', 4);
        charToValue.put('z', 10);
    }

    /**
     * Gets a deep copy of the board. Used so that the AI can test place items
     * on board
     */
    public TileBean[][] getBoard() {
        TileBean[][] toReturn = new TileBean[board.length][];
        for (int i = 0; i < board.length; i++) {
            toReturn[i] = Arrays.copyOf(board[i], board[i].length);
        }
        return toReturn;
    }
}
