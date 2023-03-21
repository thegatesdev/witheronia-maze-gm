package io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.quest;

import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.goal.ActiveGoal;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.goal.Goal;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayDeque;
import java.util.Deque;

public class ActiveQuest<O> {

    private final Quest<O> quest;
    private final Player player;
    private final O origin;
    private Deque<Goal<?, O>> goals;
    private ActiveGoal<?, O> currentGoal;
    private boolean finished = false;

    public ActiveQuest(final Quest<O> quest, Player player, O origin) {
        this.quest = quest;
        this.player = player;
        this.origin = origin;
        this.goals = new ArrayDeque<>(quest.goals());
    }

    public <E extends Event> void questEvent(Class<E> eventClass, E event) {
        @SuppressWarnings("unchecked") final ActiveGoal<E, O> goal = (ActiveGoal<E, O>) currentGoal();
        if (goal == null || !goal.listenedEvent().isAssignableFrom(eventClass)) return;
        goal.goalEvent(event);
        if (goal.finished()) {
            currentGoal = null;
            if (goals.isEmpty()) finish();
        }
    }

    private ActiveGoal<?, O> currentGoal() {
        if (!finished && currentGoal == null) {
            final Goal<?, O> newGoal = goals.poll();
            if (newGoal == null) finish();
            else currentGoal = new ActiveGoal<>(newGoal, origin, player);
        }
        return currentGoal;
    }

    private void finish() {
        finished = true;
        goals = null;
        currentGoal = null;
    }

    public O origin() {
        return origin;
    }

    public boolean finished() {
        return finished;
    }

    public String questId() {
        return quest.id();
    }
}
