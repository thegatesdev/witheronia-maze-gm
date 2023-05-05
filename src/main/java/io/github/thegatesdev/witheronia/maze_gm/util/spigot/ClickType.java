package io.github.thegatesdev.witheronia.maze_gm.util.spigot;

import org.bukkit.event.block.Action;

public enum ClickType {
    RIGHT, LEFT, ANY, NONE;

    public boolean compare(Action action) {
        return switch (this) {
            case NONE -> false;
            case ANY -> action != Action.PHYSICAL;
            case RIGHT -> action.isRightClick();
            case LEFT -> action.isLeftClick();
        };
    }
}
