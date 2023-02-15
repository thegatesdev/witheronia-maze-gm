package io.github.thegatesdev.witheronia.maze_gm.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Goal<E extends Event> {

    private final Class<E> eventClass;
    private final Predicate<E> doesComplete;

    private Consumer<Player> acceptAction;

    public Goal(Class<E> eventClass, Predicate<E> doesComplete) {
        this.eventClass = eventClass;
        this.doesComplete = doesComplete;
    }

    public Goal<E> onAccept(final Consumer<Player> acceptAction) {
        this.acceptAction = acceptAction;
        return this;
    }

    public boolean doesComplete(E event) {
        return doesComplete.test(event);
    }

    public void accept(Player player) {
        if (acceptAction != null) acceptAction.accept(player);
    }

    public Class<E> getEventClass() {
        return eventClass;
    }
}
