package io.github.thegatesdev.witheronia.maze_gm.modules.quest.archived.structs;

import io.github.thegatesdev.eventador.core.EventType;
import org.bukkit.event.Event;

public interface Goal<E extends Event> {
    ActiveGoal<E> activate();

    EventType<E> listenedEvent();
}