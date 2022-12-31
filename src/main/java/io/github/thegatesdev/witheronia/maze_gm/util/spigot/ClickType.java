package io.github.thegatesdev.witheronia.maze_gm.util.spigot;

import org.bukkit.event.block.Action;

public enum ClickType {
    RIGHT, LEFT, BOTH, NONE;

    public static ClickType spigot(Action action) {
        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) return RIGHT;
        else if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) return LEFT;
        else return NONE;
    }

    public boolean compare(ClickType second) {
        return this == second || this == ClickType.BOTH || second == ClickType.BOTH;
    }
}
