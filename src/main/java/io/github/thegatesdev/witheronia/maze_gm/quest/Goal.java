package io.github.thegatesdev.witheronia.maze_gm.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class Goal<E extends Event, T> {

    private final Class<E> eventClass;
    private final Predicate<E> doesComplete;

    private BiConsumer<Player, T> acceptAction;

    public Goal(Class<E> eventClass, Predicate<E> doesComplete) {
        this.eventClass = eventClass;
        this.doesComplete = doesComplete;
    }

    public Goal<E, T> onAccept(final BiConsumer<Player, T> acceptAction) {
        this.acceptAction = acceptAction;
        return this;
    }

    public boolean doesComplete(E event) {
        return doesComplete.test(event);
    }

    public void accept(Player player, T origin) {
        if (acceptAction != null) acceptAction.accept(player, origin);
    }

    public Class<E> getEventClass() {
        return eventClass;
    }
}
