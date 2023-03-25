package io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs;

import io.github.thegatesdev.mapletree.registry.Identifiable;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class Quest implements Identifiable {

    private final String id;
    private final List<Goal<?>> goals = new ArrayList<>();

    private Consumer<Player> onActivate, onFinish;
    private Set<String> requiredQuests;


    public Quest(final String id) {
        this.id = id;
    }


    public ActiveQuest activate(Player player) {
        return new ActiveQuest(this, player);
    }

    public List<Goal<?>> goals() {
        return goals;
    }

    public Quest addGoals(Goal<?>... goals) {
        Collections.addAll(this.goals, goals);
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

    @Override
    public String id() {
        return id;
    }
}
