package io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs;

import io.github.thegatesdev.eventador.core.EventType;
import io.github.thegatesdev.maple.data.Keyed;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class Quest implements Keyed {

    private final String id;
    private final List<Goal<?>> goals = new ArrayList<>();
    private final Set<EventType<?>> listenedEvents = new HashSet<>();

    private Consumer<Player> onActivate, onFinish;
    private Set<String> requiredQuests;


    public Quest(final String id) {
        this.id = id;
    }


    public ActiveQuest createActive(Player player) {
        return new ActiveQuest(this, player);
    }

    public List<Goal<?>> goals() {
        return goals;
    }

    public Quest addGoals(Goal<?>... goals) {
        for (final Goal<?> goal : goals) {
            this.goals.add(goal);
            this.listenedEvents.add(goal.listenedEvent());
        }
        return this;
    }


    public Quest onActivate(final Consumer<Player> onActivate) {
        this.onActivate = onActivate;
        return this;
    }

    public Quest onFinish(final Consumer<Player> onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    void onActivate(Player player) {
        if (onActivate != null) onActivate.accept(player);
    }

    void onFinish(Player player) {
        if (onFinish != null) onFinish.accept(player);
    }

    public Set<String> requiredQuests() {
        return requiredQuests;
    }

    public Set<EventType<?>> listenedEvents() {
        return listenedEvents;
    }

    @Override
    public String key() {
        return id;
    }
}
