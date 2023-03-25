package io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs;

import org.bukkit.event.Event;

public interface Goal<E extends Event> {
    ActiveGoal<E> activate();
}