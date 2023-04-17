package io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs;

import io.github.thegatesdev.eventador.core.EventType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.ArrayDeque;
import java.util.Deque;

public class ActiveQuest {

    private final Quest quest;
    private final Player player;
    private Deque<Goal<?>> goals;
    private ActiveGoal<?> currentGoal;
    private boolean finished = false;

    public ActiveQuest(final Quest quest, Player player) {
        this.quest = quest;
        this.player = player;
        this.goals = new ArrayDeque<>(quest.goals());
        quest.onActivate(player);
    }

    public <E extends Event> boolean questEvent(E event, EventType<E> eventType) {
        @SuppressWarnings("unchecked") final ActiveGoal<E> goal = (ActiveGoal<E>) currentGoal();
        if (goal == null || !eventType.appliesTo(goal.listenedEvent())) return false;
        if (goal.doesProgress(event)) {
            if (goal.isFinished()) {
                goal.onFinish(event);
                currentGoal = null;
                if (goals.isEmpty()) finish();
            } else goal.onProgress(event);
        } else goal.onFail(event);
        return true;
    }

    private ActiveGoal<?> currentGoal() {
        if (!finished && currentGoal == null) {
            final Goal<?> newGoal = goals.poll();
            if (newGoal == null) finish();
            else {
                currentGoal = newGoal.activate();
                currentGoal.onActivate(player);
            }
        }
        return currentGoal;
    }

    private void finish() {
        finished = true;
        goals = null;
        currentGoal = null;
        quest.onFinish(player);
    }

    public boolean finished() {
        return finished;
    }

    public String questId() {
        return quest.id();
    }
}
