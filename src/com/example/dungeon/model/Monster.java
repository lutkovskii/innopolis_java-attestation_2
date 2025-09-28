package com.example.dungeon.model;

import java.io.Serializable;

public class Monster extends Entity {
    private static final long serialVersionUID = 1L;

    private int level;

    public Monster(String name, int level, int hp) {
        super(name, hp);
        this.level = level;
    }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}
