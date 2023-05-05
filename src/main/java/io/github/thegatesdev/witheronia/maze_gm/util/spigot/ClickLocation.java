package io.github.thegatesdev.witheronia.maze_gm.util.spigot;

import org.bukkit.event.block.Action;

public enum ClickLocation {
    BLOCK, AIR, ANY, NONE;

    public boolean compare(Action action) {
        return switch (this) {
            case NONE -> false;
            case ANY -> action != Action.PHYSICAL;
            case BLOCK -> action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK;
            case AIR -> action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR;
        };
    }
}
