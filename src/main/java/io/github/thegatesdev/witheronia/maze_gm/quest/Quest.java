package io.github.thegatesdev.witheronia.maze_gm.quest;

import io.github.thegatesdev.mapletree.registry.Identifiable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.*;
import java.util.function.Consumer;

public class Quest<T> implements Identifiable {

    private final String id;

    private Set<String> requiredQuests;

    private final List<Goal<?, T>> goals = new ArrayList<>();
    private Map<UUID, Integer> playerProgression = new HashMap<>();

    private Consumer<Player> completeAction, acceptAction;
    private int difficulty = 0;

    private final Set<Class<? extends Event>> goalEvents = new HashSet<>();

    public Quest(final String id) {
        this.id = id;
    }

    @SafeVarargs
    public final Quest<T> addGoals(final Goal<?, T>... goals) {
        for (final Goal<?, T> goal : goals) {
            this.goals.add(goal);
            this.goalEvents.add(goal.getEventClass());
        }
        return this;
    }

    public Quest<T> onAccept(final Consumer<Player> acceptAction) {
        this.acceptAction = acceptAction;
        return this;
    }

    public Quest<T> onComplete(final Consumer<Player> completeAction) {
        this.completeAction = completeAction;
        return this;
    }

    public void difficulty(final int difficulty) {
        this.difficulty = difficulty;
    }

    public int difficulty() {
        return difficulty;
    }

    public Quest<T> requiresQuest(String... quests) {
        if (requiredQuests == null) requiredQuests = new HashSet<>(quests.length);
        Collections.addAll(requiredQuests, quests);
        return this;
    }

    public Set<String> getRequiredQuests() {
        return requiredQuests;
    }


    public void populateProgression(TreeMap<UUID, Integer> playerProgression) {
        this.playerProgression = playerProgression;
    }

    public Map<UUID, Integer> getPlayerProgression() {
        return playerProgression;
    }


    public void accept(Player player, T origin) {
        if (playerProgression.computeIfAbsent(player.getUniqueId(), uuid -> 0) == 0 && acceptAction != null) {
            acceptAction.accept(player);
            goals.get(0).accept(player, origin);
        }
    }

    public Goal<?, T> currentGoal(UUID playerId) {
        final Integer progression = playerProgression.get(playerId);
        if (progression == null) return null;
        return goals.get(progression);
    }

    public boolean progress(Player player, T origin) {
        Integer progression = playerProgression.computeIfPresent(player.getUniqueId(), (uuid, integer) -> ++integer);
        if (progression == null) return false;
        if (progression >= goals.size()) {
            complete(player);
            return true;
        }
        goals.get(progression).accept(player, origin);
        return false;
    }

    private void complete(Player player) {
        playerProgression.remove(player.getUniqueId());
        if (completeAction != null) completeAction.accept(player);
    }


    public Set<Class<? extends Event>> getGoalEvents() {
        return goalEvents;
    }

    @Override
    public String id() {
        return id;
    }
}