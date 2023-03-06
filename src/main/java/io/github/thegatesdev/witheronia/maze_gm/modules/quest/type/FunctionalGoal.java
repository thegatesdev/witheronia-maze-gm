package io.github.thegatesdev.witheronia.maze_gm.modules.quest.type;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class FunctionalGoal<E extends Event, O> implements Goal<E, O> {

    private final Class<E> eventClass;
    private final BiPredicate<E, O> doesComplete;

    private BiConsumer<Player, O> acceptAction;
    private BiConsumer<E, O> completeAction, failAction;

    public FunctionalGoal(Class<E> eventClass, BiPredicate<E, O> doesComplete) {
        this.eventClass = eventClass;
        this.doesComplete = doesComplete;
    }

    public FunctionalGoal<E, O> onAccept(final BiConsumer<Player, O> acceptAction) {
        this.acceptAction = acceptAction;
        return this;
    }

    public FunctionalGoal<E, O> onComplete(final BiConsumer<E, O> completeAction) {
        this.completeAction = completeAction;
        return this;
    }

    public FunctionalGoal<E, O> onFail(final BiConsumer<E, O> failAction) {
        this.failAction = failAction;
        return this;
    }

    public boolean completesGoal(E event, O origin) {
        if (doesComplete.test(event, origin)) {
            completeAction.accept(event, origin);
            return true;
        }
        failAction.accept(event, origin);
        return false;
    }

    public void accept(Player player, O origin) {
        if (acceptAction != null) acceptAction.accept(player, origin);
    }

    public Class<E> eventClass() {
        return eventClass;
    }
}
