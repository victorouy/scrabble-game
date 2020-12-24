package com.scrabblegame.scrabbleserver;

/**
 *
 * @author Stephen He
 * @author Victor Ouy
 * @author Lucas
 */
public class TileBean {
    private char letter;
    
    public TileBean(char letter){
        this.letter = letter;
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }
}
