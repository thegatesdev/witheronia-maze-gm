package io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs;

import io.github.thegatesdev.eventador.core.EventType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public interface ActiveGoal<E extends Event> {
    boolean doesProgress(E event);

    boolean isFinished();

    EventType<E> listenedEvent();


    default void onFinish(E event) {
    }

    default void onProgress(E event) {
    }

    default void onFail(E event) {
    }

    default void onActivate(Player player) {
    }
}
