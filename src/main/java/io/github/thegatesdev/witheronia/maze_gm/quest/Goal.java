package io.github.thegatesdev.witheronia.maze_gm.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.function.BiPredicate;
import java.util.function.Consumer;

public record Goal<E extends Event>(Class<E> eventClass, BiPredicate<E, Class<E>> reactor, Consumer<Player> onAccept) {

    public Goal(Class<E> eventClass, BiPredicate<E, Class<E>> reactor) {
        this(eventClass, reactor, null);
    }

    public boolean doesComplete(E event) {
        return reactor.test(event, eventClass);
    }

    public void accept(Player player) {
        if (onAccept != null) onAccept.accept(player);
    }
}
