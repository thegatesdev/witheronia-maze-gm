package io.github.thegatesdev.witheronia.maze_gm.quest;

import io.github.thegatesdev.mapletree.registry.Identifiable;
import org.bukkit.event.Event;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Quest implements Identifiable {

    private final String id;

    private Set<String> requiredQuests;

    private final List<Goal<?>> goals = new ArrayList<>();
    private TreeMap<UUID, Integer> playerProgression = new TreeMap<>();

    private Consumer<UUID> completeAction, acceptAction;


    public Quest(final String id) {
        this.id = id;
    }

    public <E extends Event> Quest addGoal(Class<E> eventClass, Predicate<E> canProceed, Consumer<UUID> onAccept,
                                           Consumer<E> onFinish) {
        return addGoal(new Goal<>(eventClass, canProceed, onAccept, onFinish));
    }

    public Quest addGoal(Goal<?> goal) {
        goals.add(goal);
        return this;
    }

    public Quest onAccept(final Consumer<UUID> acceptAction) {
        this.acceptAction = acceptAction;
        return this;
    }

    public Quest onComplete(final Consumer<UUID> completeAction) {
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


    public void acceptPlayer(UUID playerId) {
        if (playerProgression.computeIfAbsent(playerId, uuid -> 0) == 0 && acceptAction != null)
            acceptAction.accept(playerId);
    }

    public Goal<?> currentGoal(UUID playerId) {
        final Integer progression = playerProgression.get(playerId);
        if (progression == null) return null;
        return goals.get(progression);
    }

    public boolean progressPlayer(UUID playerId) {
        Integer progression = playerProgression.computeIfPresent(playerId, (uuid, integer) -> ++integer);
        if (progression == null) return false;
        if (progression >= goals.size()) {
            completePlayer(playerId);
            return true;
        }
        goals.get(progression).accept(playerId);
        return false;
    }

    private void completePlayer(UUID playerId) {
        playerProgression.remove(playerId);
        if (completeAction != null) completeAction.accept(playerId);
    }


    public record Goal<E extends Event>(Class<E> eventClass, Predicate<E> canProceed, Consumer<UUID> onAccept,
                                        Consumer<E> onFinish) {
        public boolean tryComplete(E event) {
            if (canProceed.test(event)) {
                if (onFinish != null) onFinish.accept(event);
                return true;
            }
            return false;
        }

        private void accept(UUID playerId) {
            if (onAccept != null) onAccept.accept(playerId);
        }
    }

    @Override
    public String id() {
        return id;
    }
}