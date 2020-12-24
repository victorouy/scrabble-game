/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrabblegame.data;

import javafx.scene.image.ImageView;


/**
 *
 * @author Stephen He
 * @author Victor Ouy
 * @author Lucas
 */
public class TileBean {
    private char letter;
    //Only the server should make use of this parameter. Client can just set this to -1.
    private int value;
    
    public TileBean(char letter, int value){
        this.letter = letter;
    }

    public char getLetter() {
        return letter;
    }

    public int getValue() {
        return value;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
