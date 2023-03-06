package io.github.thegatesdev.witheronia.maze_gm.modules.quest.type;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.*;
import java.util.function.BiConsumer;

public class FunctionalQuest<O> implements Quest<O> {

    private final String id;

    private Set<String> requiredQuests;
    private final List<Goal<?, O>> goals = new ArrayList<>();

    private BiConsumer<Player, O> finishAction, acceptAction;

    private int difficulty = 0;
    private Material displayMaterial;

    private final Set<Class<? extends Event>> goalEvents = new HashSet<>();

    public FunctionalQuest(final String id) {
        this.id = id;
    }

    @SafeVarargs
    public final FunctionalQuest<O> addGoals(final Goal<?, O>... goals) {
        for (final Goal<?, O> goal : goals) {
            this.goals.add(goal);
            this.goalEvents.add(goal.eventClass());
        }
        return this;
    }

    public Goal<?, O> goal(int index) {
        return goals.get(index);
    }

    public int goals() {
        return goals.size();
    }

    public FunctionalQuest<O> onAccept(final BiConsumer<Player, O> acceptAction) {
        this.acceptAction = acceptAction;
        return this;
    }

    public FunctionalQuest<O> onFinish(final BiConsumer<Player, O> finishAction) {
        this.finishAction = finishAction;
        return this;
    }

    public void difficulty(final int difficulty) {
        this.difficulty = difficulty;
    }

    public int difficulty() {
        return difficulty;
    }

    public FunctionalQuest<O> requiresQuest(String... quests) {
        if (requiredQuests == null) requiredQuests = new HashSet<>(quests.length);
        Collections.addAll(requiredQuests, quests);
        return this;
    }

    public Set<String> requiredQuests() {
        return requiredQuests;
    }

    public FunctionalQuest<O> displayMaterial(final Material displayMaterial) {
        this.displayMaterial = displayMaterial;
        return this;
    }

    public Material displayMaterial() {
        return displayMaterial;
    }

    public void finish(Player player, O origin) {
        finishAction.accept(player, origin);
    }

    public void accept(Player player, O origin) {
        acceptAction.accept(player, origin);
    }

    public Set<Class<? extends Event>> goalEvents() {
        return goalEvents;
    }

    @Override
    public String id() {
        return id;
    }
}