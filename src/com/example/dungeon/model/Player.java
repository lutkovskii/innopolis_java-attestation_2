package com.example.dungeon.model;

import java.io.Serializable;
import java.util.*;

public class Player extends Entity {
    private static final long serialVersionUID = 1L;

    private int attack;
    private final List<Item> inventory = new ArrayList<>();

    public Player(String name, int hp, int attack) {
        super(name, hp);
        this.attack = attack;
    }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }
    public List<Item> getInventory() { return inventory; }
}
