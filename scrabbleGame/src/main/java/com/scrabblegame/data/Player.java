/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scrabblegame.data;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Stephen He
 * @author Victor Ouy
 * @author Lucas
 */
public class Player {
    private TileBean[] rack;
    private ArrayList<Integer> emptyIndexes;
    private int points;
    private int latestScore;
    
    public Player() {
        this.rack = new TileBean[7];
        this.emptyIndexes = new ArrayList<Integer>();
        for (int i = 0; i < 7; i++) {
            this.emptyIndexes.add(i);
        }
        this.points = 0;
        this.latestScore = 0;
    }
    
    /**
     * Gets the length of rack that are not null
     * 
     * @return length of rack not null
     */
    public int rackInsideLength() {
        int count = 0;
        for (TileBean tileRack : rack) {
            if (tileRack != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Adds points to the player
     *
     * Call it like this: player_obj.add(pts)
     *
     * @param add Adds points to the user
     */
    public void addPoints(int add) {
        this.points += add;
        this.latestScore = add;
    }

    /**
     * Gets the amount of points the player has Called at the end of each play
     * to update the score
     *
     * Call it like this: int pts = player_obj.getPoints()
     *
     * @return The points
     */
    public int getPoints() {
        return this.points;
    }

    public int getLatestScore() {
        return this.latestScore;
    }

    /**
     * Gets the rack of the player This method should be called at the end of
     * each round. The purpose is to update the image view to the accurate
     * version of this rack Position of tiles might also shift since we removed
     * some tiles (play or discard/swap).
     *
     * Call it like this: ArrayList<TileBean> rack = player_obj.getRack();
     *
     * @return The player's rack
     */
    public ArrayList<TileBean> getRack() {
        ArrayList<TileBean> toReturn = new ArrayList<TileBean>();
        Collections.addAll(toReturn, rack);
        return toReturn;
    }

    /**
     * Adds a tile to the rack Called when we first initialize hand, when we
     * swap, after we play.
     *
     * Call it like so: player_obj.add(tile)
     *
     * @param tile The tile to add to hand
     */
    public void addTile(TileBean tile) {
        this.rack[this.emptyIndexes.get(0)] = tile;
        emptyIndexes.remove(0);
    }

    /**
     * Removes a tile from the rack Called when we want to swap away a tile OR
     * when we play a word
     *
     * Call it like so: player_obj.removeTile(index);
     *
     * @param index The index to remove
     */
    public TileBean removeTile(int index) {
        TileBean toReturn = this.rack[index];
        this.rack[index] = null;
        this.emptyIndexes.add(index);
        return toReturn;
    }
}
