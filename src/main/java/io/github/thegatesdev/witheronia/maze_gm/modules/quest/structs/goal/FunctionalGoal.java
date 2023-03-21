package io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.goal;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.function.BiConsumer;

public class FunctionalGoal<E extends Event, O> implements Goal<E, O> {
    private final Class<E> eventClass;
    private final GoalPredicate<E, O> goalPredicate;
    private BiConsumer<Player, ActiveGoal<E, O>> onAccept;
    private GoalEventCallback<E, O> onProgress, onFinish, onFail;
    private int amount = 1;

    FunctionalGoal(final Class<E> eventClass, final GoalPredicate<E, O> predicate) {
        this.eventClass = eventClass;
        goalPredicate = predicate;
    }

    @Override
    public boolean doesProgress(final E event, ActiveGoal<E, O> goal) {
        return goalPredicate.test(event, goal);
    }

    @Override
    public void onAccept(final Player player, ActiveGoal<E, O> goal) {
        if (onAccept != null) onAccept.accept(player, goal);
    }

    @Override
    public void onFail(final E event, ActiveGoal<E, O> goal) {
        if (onFail != null) onFail.call(event, goal);
    }

    @Override
    public void onFinish(final E event, ActiveGoal<E, O> goal) {
        if (onFinish != null) onFinish.call(event, goal);
    }

    @Override
    public void onProgress(final E event, ActiveGoal<E, O> goal) {
        if (onProgress != null) onProgress.call(event, goal);
    }

    public FunctionalGoal<E, O> onAccept(final BiConsumer<Player, ActiveGoal<E, O>> onAccept) {
        this.onAccept = onAccept;
        return this;
    }

    public FunctionalGoal<E, O> onFail(final GoalEventCallback<E, O> onFail) {
        this.onFail = onFail;
        return this;
    }

    public FunctionalGoal<E, O> onFinish(final GoalEventCallback<E, O> onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    public FunctionalGoal<E, O> onProgress(final GoalEventCallback<E, O> onProgress) {
        this.onProgress = onProgress;
        return this;
    }

    public FunctionalGoal<E, O> times(final int progressNeeded) {
        this.amount = progressNeeded;
        return this;
    }

    @Override
    public int progressNeeded() {
        return amount;
    }

    @Override
    public Class<E> listenedEvent() {
        return eventClass;
    }

    @FunctionalInterface
    public interface GoalEventCallback<E extends Event, O> {
        void call(E event, ActiveGoal<E, O> goal);
    }

    @FunctionalInterface
    public interface GoalPredicate<E extends Event, O> {
        boolean test(E event, ActiveGoal<E, O> goal);
    }
}
