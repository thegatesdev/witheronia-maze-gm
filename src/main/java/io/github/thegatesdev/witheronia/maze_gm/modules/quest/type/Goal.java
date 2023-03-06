package io.github.thegatesdev.witheronia.maze_gm.modules.quest.type;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class Goal<E extends Event, O> {

    private final Class<E> eventClass;

    protected Goal(final Class<E> eventClass) {
        this.eventClass = eventClass;
    }


    public boolean completesGoal(E event, O origin) {
        if (doesComplete(event, origin)) {
            onComplete(event, origin);
            return true;
        }
        onFail(event, origin);
        return false;
    }

    public Class<E> eventClass() {
        return eventClass;
    }


    protected abstract boolean doesComplete(E event, O origin);

    public void onAccept(Player player, O origin) {
    }

    protected void onFail(E event, O origin) {
    }

    protected void onComplete(E event, O origin) {
    }
}
