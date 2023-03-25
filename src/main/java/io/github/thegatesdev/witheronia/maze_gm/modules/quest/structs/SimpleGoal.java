package io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class SimpleGoal<E extends Event> implements Goal<E> {
    private final Class<E> eventClass;
    private final Predicate<E> eventPredicate;
    private EventCallback<E> onProgress, onFinish, onFail;
    private Consumer<Player> onActivate;
    private int maxProgress = 1;

    public SimpleGoal(final Class<E> eventClass, final Predicate<E> eventPredicate) {
        this.eventClass = eventClass;
        this.eventPredicate = eventPredicate;
    }

    public SimpleGoal<E> maxProgress(final int maxProgress) {
        this.maxProgress = maxProgress;
        return this;
    }

    public SimpleGoal<E> onProgress(final EventCallback<E> callback) {
        this.onProgress = callback;
        return this;
    }

    public SimpleGoal<E> onFinish(final EventCallback<E> callback) {
        this.onFinish = callback;
        return this;
    }

    public SimpleGoal<E> onFail(final EventCallback<E> callback) {
        this.onFail = callback;
        return this;
    }

    public SimpleGoal<E> onActivate(final Consumer<Player> onActivate) {
        this.onActivate = onActivate;
        return this;
    }

    @Override
    public ActiveGoal<E> activate() {
        return new ActiveGoal<>() {
            private int progress = 0;

            @Override
            public boolean doesProgress(final E event) {
                if (eventPredicate.test(event)) {
                    progress++;
                    return true;
                }
                return false;
            }

            @Override
            public boolean isFinished() {
                return progress >= maxProgress;
            }

            @Override
            public Class<E> listenedEvent() {
                return eventClass;
            }

            @Override
            public void onProgress(final E event) {
                if (onProgress != null) onProgress.call(event, progress, maxProgress);
            }

            @Override
            public void onFail(final E event) {
                if (onFail != null) onFail.call(event, progress, maxProgress);
            }

            @Override
            public void onFinish(final E event) {
                if (onFinish != null) onFinish.call(event, progress, maxProgress);
            }

            @Override
            public void onActivate(final Player player) {
                if (onActivate != null) onActivate.accept(player);
            }
        };
    }

    @FunctionalInterface
    public interface EventCallback<E> {
        void call(E event, int progress, int maxProgress);
    }
}
