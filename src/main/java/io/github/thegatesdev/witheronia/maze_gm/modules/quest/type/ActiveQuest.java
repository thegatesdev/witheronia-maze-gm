package io.github.thegatesdev.witheronia.maze_gm.modules.quest.type;

import io.github.thegatesdev.mapletree.registry.Identifiable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public final class ActiveQuest<O> implements Identifiable {
    private final Quest<O> quest;
    private final O origin;
    private final Player player;

    private int progression = 0;
    private boolean finished = false;

    public ActiveQuest(Quest<O> quest, O origin, Player player) {
        this.quest = quest;
        this.origin = origin;
        this.player = player;
    }

    public ActiveQuest<O> setProgression(final int progression) {
        this.progression = progression;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <E extends Event> void goalEvent(E event, Class<E> eventClass) {
        if (finished) return;
        final Goal<E, O> goal = (Goal<E, O>) currentGoal();
        if (!eventClass.isAssignableFrom(goal.currentEvent())) return;
        if (goal.completesGoal(event, origin)) progress();
    }

    private void progress() {
        if (finished) return;
        if (++progression >= quest.goals()) {
            finished = true;
            quest.finish(player, origin);
        } else currentGoal().onAccept(player, origin);
    }

    private Goal<?, O> currentGoal() {
        return quest.goal(progression);
    }

    public boolean finished() {
        return finished;
    }

    @Override
    public String id() {
        return quest.id();
    }
}
