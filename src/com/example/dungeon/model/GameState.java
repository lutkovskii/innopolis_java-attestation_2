// com.example.dungeon.model.GameState
package com.example.dungeon.model;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private Player player;
    private Room current;
    private int score;
    private boolean treasureDoorUnlocked = false; // новое поле

    public Player getPlayer() { return player; }
    public void setPlayer(Player p) { this.player = p; }
    public Room getCurrent() { return current; }
    public void setCurrent(Room r) { this.current = r; }
    public int getScore() { return score; }
    public void addScore(int d) { this.score += d; }

    public boolean isTreasureDoorUnlocked() { return treasureDoorUnlocked; }
    public void setTreasureDoorUnlocked(boolean unlocked) { this.treasureDoorUnlocked = unlocked; }
}