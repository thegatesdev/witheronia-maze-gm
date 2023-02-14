package io.github.thegatesdev.witheronia.maze_gm.quest;

import io.github.thegatesdev.mapletree.registry.Identifiable;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;

public class Quest implements Identifiable {

    private final String id;

    private Set<String> requiredQuests;

    private final List<Goal<?>> goals = new ArrayList<>();
    private TreeMap<UUID, Integer> playerProgression = new TreeMap<>();

    private Consumer<Player> completeAction, acceptAction;


    public Quest(final String id) {
        this.id = id;
    }

    public Quest addGoal(Goal<?> goal) {
        goals.add(goal);
        return this;
    }

    public Quest onAccept(final Consumer<Player> acceptAction) {
        this.acceptAction = acceptAction;
        return this;
    }

    public Quest onComplete(final Consumer<Player> completeAction) {
        this.completeAction = completeAction;
        return this;
    }

    public Quest requiresQuest(String... quests) {
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

    public TreeMap<UUID, Integer> getPlayerProgression() {
        return playerProgression;
    }


    public void acceptPlayer(Player player) {
        if (playerProgression.computeIfAbsent(player.getUniqueId(), uuid -> 0) == 0 && acceptAction != null)
            acceptAction.accept(player);
    }

    public Goal<?> currentGoal(UUID playerId) {
        final Integer progression = playerProgression.get(playerId);
        if (progression == null) return null;
        return goals.get(progression);
    }

    public boolean progressPlayer(Player player) {
        Integer progression = playerProgression.computeIfPresent(player.getUniqueId(), (uuid, integer) -> ++integer);
        if (progression == null) return false;
        if (progression >= goals.size()) {
            completePlayer(player);
            return true;
        }
        goals.get(progression).accept(player);
        return false;
    }

    private void completePlayer(Player player) {
        playerProgression.remove(player.getUniqueId());
        if (completeAction != null) completeAction.accept(player);
    }


    @Override
    public String id() {
        return id;
    }
}