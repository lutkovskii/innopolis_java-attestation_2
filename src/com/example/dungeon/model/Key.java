// com.example.dungeon.model.Key
package com.example.dungeon.model;

public class Key extends Item {
    private static final long serialVersionUID = 1L;

    public Key(String name) {
        super(name);
    }

    @Override
    public void apply(GameState ctx) {
        if ("Пещера".equals(ctx.getCurrent().getName())) {
            if (!ctx.isTreasureDoorUnlocked()) {
                ctx.setTreasureDoorUnlocked(true);
                System.out.println("Вы использовали ключ! Дверь на север открыта.");
                ctx.getPlayer().getInventory().remove(this);
            } else {
                System.out.println("Дверь уже открыта.");
            }
        } else {
            System.out.println("Ключ звенит. Возможно, где-то есть дверь...");
        }
    }
}