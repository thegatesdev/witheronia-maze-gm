package io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.goal;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public interface Goal<E extends Event, O> {

    static <E extends Event, O> FunctionalGoal<E, O> build(Class<E> eventClass, FunctionalGoal.GoalPredicate<E, O> goalPredicate) {
        return new FunctionalGoal<>(eventClass, goalPredicate);
    }

    boolean doesProgress(E event, ActiveGoal<E, O> goal);


    default void onAccept(Player player, ActiveGoal<E, O> goal) {
    }

    default void onProgress(E event, ActiveGoal<E, O> goal) {
    }

    default void onFinish(E event, ActiveGoal<E, O> goal) {
    }

    default void onFail(E event, ActiveGoal<E, O> goal) {
    }


    default int progressNeeded() {
        return 1;
    }

    Class<E> listenedEvent();
}
