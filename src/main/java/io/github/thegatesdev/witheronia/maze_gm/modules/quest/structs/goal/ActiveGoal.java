package io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.goal;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class ActiveGoal<E extends Event, O> {
    private final Goal<E, O> goal;
    private final O origin;

    private int progression = 0;
    private boolean finished = false;

    public ActiveGoal(final Goal<E, O> goal, O origin, Player player) {
        this.goal = goal;
        this.origin = origin;
        if (player != null) goal.onAccept(player, this);
    }

    public void goalEvent(E event) {
        if (finished) return;
        if (goal.doesProgress(event, this)) {
            if (++progression >= goal.progressNeeded()) {
                goal.onFinish(event, this);
                finished = true;
            } else goal.onProgress(event, this);
        } else goal.onFail(event, this);
    }

    public O origin() {
        return origin;
    }

    public int progress() {
        return progression;
    }

    public int progressNeeded() {
        return goal.progressNeeded();
    }

    public boolean finished() {
        return finished;
    }

    public Class<E> listenedEvent() {
        return goal.listenedEvent();
    }
}
