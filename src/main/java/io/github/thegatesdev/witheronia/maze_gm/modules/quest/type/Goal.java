package io.github.thegatesdev.witheronia.maze_gm.modules.quest.type;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public interface Goal<E extends Event, O> {
    boolean completesGoal(E event, O origin);

    void accept(Player player, O origin);

    Class<E> eventClass();
}
