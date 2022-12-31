package io.github.thegatesdev.witheronia.maze_gm.util.spigot;

import org.bukkit.event.block.Action;

public enum ClickLocation {
    BLOCK, AIR, BOTH, NONE;

    public static ClickLocation spigot(Action action) {
        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) return BLOCK;
        else if (action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_AIR) return AIR;
        else return NONE;
    }

    public boolean compare(ClickLocation second) {
        return this == second || this == ClickLocation.BOTH || second == ClickLocation.BOTH;
    }
}
