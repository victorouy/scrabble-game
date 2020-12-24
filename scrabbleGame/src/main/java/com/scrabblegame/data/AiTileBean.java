/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrabblegame.data;

/**
 *
 * @author Stephen He
 * @author Victor Ouy
 * @author Lucas
 */
public class AiTileBean {
    private char aiLetter;
    private int aiRow;
    private int aiCol;
    
    public AiTileBean(char aiLetter, int aiRow, int aiCol){
        this.aiLetter = aiLetter;
        this.aiRow = aiRow;
        this.aiCol = aiCol;
    }

    public char getAiLetter() {
        return aiLetter;
    }

    public int getAiRow() {
        return aiRow;
    }
    
    public int getAiCol() {
        return aiCol;
    }

    public void setAiLetter(char aiLetter) {
        this.aiLetter = aiLetter;
    }

    public void setAiRow(int aiRow) {
        this.aiRow = aiRow;
    }
    
    public void setAiCol(int aiCol) {
        this.aiCol = aiCol;
    }
}
