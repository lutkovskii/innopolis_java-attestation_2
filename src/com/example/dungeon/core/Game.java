package com.example.dungeon.core;

import com.example.dungeon.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class Game {
    private final GameState state = new GameState();
    private final Map<String, Command> commands = new LinkedHashMap<>();

    static {
        WorldInfo.touch("Game");
    }

    public Game() {
        registerCommands();
        bootstrapWorld();
    }

    private void registerCommands() {
        commands.put("help", (ctx, a) -> System.out.println("Команды: " + String.join(", ", commands.keySet())));
        commands.put("gc-stats", (ctx, a) -> {
            Runtime rt = Runtime.getRuntime();
            long free = rt.freeMemory(), total = rt.totalMemory(), used = total - free;
            System.out.println("Память: used=" + used + " free=" + free + " total=" + total);
        });
        commands.put("look", (ctx, a) -> System.out.println(ctx.getCurrent().describe()));

        // TODO-1: move <direction>
        commands.put("move", (ctx, args) -> {
            if (args.isEmpty()) {
                throw new InvalidCommandException("Укажите направление: north, south, east, west");
            }
            String dir = args.get(0).toLowerCase(Locale.ROOT);
            Room current = ctx.getCurrent();
            Room next = current.getNeighbors().get(dir);
            if (next == null) {
                throw new InvalidCommandException("Нельзя пойти на " + dir);
            }
            ctx.setCurrent(next);
            System.out.println("Вы перешли в: " + next.getName());
            System.out.println(next.describe());
        });

        // TODO-2: take <item name>
        commands.put("take", (ctx, args) -> {
            if (args.isEmpty()) {
                throw new InvalidCommandException("Укажите название предмета");
            }
            String itemName = String.join(" ", args);
            Room room = ctx.getCurrent();
            List<Item> items = room.getItems();
            Item taken = null;
            for (Item item : items) {
                if (item.getName().equalsIgnoreCase(itemName)) {
                    taken = item;
                    break;
                }
            }
            if (taken == null) {
                throw new InvalidCommandException("Предмет '" + itemName + "' не найден в комнате");
            }
            items.remove(taken);
            ctx.getPlayer().getInventory().add(taken);
            System.out.println("Взято: " + taken.getName());
        });

        // TODO-3: inventory — Stream API + группировка по типу
        commands.put("inventory", (ctx, a) -> {
            List<Item> inv = ctx.getPlayer().getInventory();
            if (inv.isEmpty()) {
                System.out.println("Инвентарь пуст.");
                return;
            }

            Map<String, List<Item>> grouped = inv.stream()
                    .collect(Collectors.groupingBy(item -> item.getClass().getSimpleName()));

            grouped.forEach((type, items) -> {
                String names = items.stream()
                        .map(Item::getName)
                        .collect(Collectors.joining(", "));
                System.out.println("- " + type + " (" + items.size() + "): " + names);
            });
        });

        // TODO-4: use <item name>
        commands.put("use", (ctx, args) -> {
            if (args.isEmpty()) {
                throw new InvalidCommandException("Укажите предмет для использования");
            }
            String itemName = String.join(" ", args);
            Player player = ctx.getPlayer();
            List<Item> inventory = player.getInventory();
            Item toUse = null;
            for (Item item : inventory) {
                if (item.getName().equalsIgnoreCase(itemName)) {
                    toUse = item;
                    break;
                }
            }
            if (toUse == null) {
                throw new InvalidCommandException("У вас нет предмета '" + itemName + "'");
            }
            toUse.apply(ctx); // полиморфизм: вызов apply() конкретного подкласса
        });

        // TODO-5: fight
        commands.put("fight", (ctx, a) -> {
            Room room = ctx.getCurrent();
            Monster monster = room.getMonster();
            if (monster == null) {
                System.out.println("В комнате нет монстра.");
                return;
            }

            Player player = ctx.getPlayer();
            int playerDmg = player.getAttack();
            monster.setHp(monster.getHp() - playerDmg);
            System.out.println("Вы бьёте " + monster.getName() + " на " + playerDmg + ". HP монстра: " + monster.getHp());

            if (monster.getHp() <= 0) {
                System.out.println("Монстр побеждён!");
                room.setMonster(null);
                return;
            }

            // Монстр атакует
            int monsterDmg = 1; // можно расширить модель Monster позже
            player.setHp(player.getHp() - monsterDmg);
            System.out.println("Монстр отвечает на " + monsterDmg + ". Ваше HP: " + player.getHp());

            if (player.getHp() <= 0) {
                System.out.println("Вы погибли! Игра окончена.");
                System.exit(0);
            }
        });

        // Существующие команды — без изменений
        commands.put("save", (ctx, a) -> SaveLoad.save(ctx));
        commands.put("load", (ctx, a) -> SaveLoad.load(ctx));
        commands.put("scores", (ctx, a) -> SaveLoad.printScores());
        commands.put("exit", (ctx, a) -> {
            System.out.println("Пока!");
            System.exit(0);
        });
    }

    private void bootstrapWorld() {
        Player hero = new Player("Герой", 20, 5);
        state.setPlayer(hero);

        Room square = new Room("Площадь", "Каменная площадь с фонтаном.");
        Room forest = new Room("Лес", "Шелест листвы и птичий щебет.");
        Room cave = new Room("Пещера", "Темно и сыро.");
        square.getNeighbors().put("north", forest);
        forest.getNeighbors().put("south", square);
        forest.getNeighbors().put("east", cave);
        cave.getNeighbors().put("west", forest);

        forest.getItems().add(new Potion("Малое зелье", 5));
        forest.setMonster(new Monster("Волк", 1, 8));

        state.setCurrent(square);
    }

    public void run() {
        System.out.println("DungeonMini (TEMPLATE). 'help' — команды.");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.print("> ");
                String line = in.readLine();
                if (line == null) break;
                line = line.trim();
                if (line.isEmpty()) continue;
                List<String> parts = Arrays.asList(line.split("\\s+"));
                String cmd = parts.getFirst().toLowerCase(Locale.ROOT);
                List<String> args = parts.subList(1, parts.size());
                Command c = commands.get(cmd);
                try {
                    if (c == null) throw new InvalidCommandException("Неизвестная команда: " + cmd);
                    c.execute(state, args);
                    state.addScore(1);
                } catch (InvalidCommandException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Непредвиденная ошибка: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка ввода/вывода: " + e.getMessage());
        }
    }
}