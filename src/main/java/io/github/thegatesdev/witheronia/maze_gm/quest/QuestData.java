package io.github.thegatesdev.witheronia.maze_gm.quest;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import java.util.*;

public class QuestData {

    private final Set<Class<? extends Event>> questEvents = new HashSet<>();

    private final Map<UUID, PlayerEntry> playerQuestData = new HashMap<>();

    private final Map<UUID, List<Quest<Entity>>> questEntities = new HashMap<>();


    public Set<Class<? extends Event>> getQuestEvents() {
        return questEvents;
    }


    public List<Quest<Entity>> getEntityQuests(UUID entityId) {
        return questEntities.get(entityId);
    }

    public QuestData addEntityQuest(UUID entityId, Quest<Entity> quest) {
        questEntities.computeIfAbsent(entityId, uuid -> new ArrayList<>()).add(quest);
        questEvents.addAll(quest.getGoalEvents());
        return this;
    }

    public void populateEntity(UUID entityId, List<Quest<Entity>> quests) {
        questEntities.put(entityId, quests);
        for (final Quest<Entity> quest : quests) {
            questEvents.addAll(quest.getGoalEvents());
        }
    }


    public PlayerEntry getPlayer(UUID playerUUID) {
        return playerQuestData.get(playerUUID);
    }

    public PlayerEntry getOrCreatePlayer(UUID playerUUID) {
        return playerQuestData.computeIfAbsent(playerUUID, uuid -> new PlayerEntry());
    }

    public void populatePlayer(UUID playerUUID, PlayerEntry playerEntry) {
        playerQuestData.put(playerUUID, playerEntry);
    }


    public static class PlayerEntry {
        private final Set<String> finished;
        private final List<ActiveQuest<?>> active;

        private PlayerEntry(final Set<String> finished, final List<ActiveQuest<?>> active) {
            this.finished = finished;
            this.active = active;
        }

        private PlayerEntry() {
            this(new HashSet<>(), new ArrayList<>());
        }

        public void setActive(ActiveQuest<?> quest) {
            if (finished.contains(quest.quest().id())) throw new RuntimeException("Cannot activate finished quest!");
            active.add(quest);
        }

        public void setFinished(String questId) {
            active.removeIf(quest -> quest.quest().id().equals(questId));
            finished.add(questId);
        }

        public List<ActiveQuest<?>> getActive() {
            return active;
        }

        public Set<String> getFinished() {
            return finished;
        }
    }
}
