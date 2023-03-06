package io.github.thegatesdev.witheronia.maze_gm.modules.quest;

import io.github.thegatesdev.witheronia.maze_gm.modules.quest.type.ActiveQuest;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.type.Quest;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.type.QuestHolder;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import java.util.*;

public class QuestData {

    private final Set<Class<? extends Event>> questEvents = new HashSet<>();

    private final Map<UUID, PlayerEntry> playerQuestData = new HashMap<>();

    private final Map<UUID, QuestHolder<Entity>> questEntities = new HashMap<>();


    public Set<Class<? extends Event>> questEvents() {
        return questEvents;
    }


    public QuestHolder<Entity> get(UUID entityId) {
        return questEntities.get(entityId);
    }

    public QuestData add(Entity entity, Quest<Entity> quest) {
        if (quest == null) throw new RuntimeException("This quest does not exist!");
        questEntities.computeIfAbsent(entity.getUniqueId(), uuid -> new QuestHolder<>(entity)).addQuest(quest);
        questEvents.addAll(quest.goalEvents());
        return this;
    }

    public void populate(UUID entityId, QuestHolder<Entity> questHolder) {
        questEntities.put(entityId, questHolder);
        for (final Quest<Entity> quest : questHolder.quests()) questEvents.addAll(quest.goalEvents());
    }


    public PlayerEntry getPlayer(UUID playerUUID) {
        return playerQuestData.get(playerUUID);
    }

    public PlayerEntry getOrCreatePlayer(UUID playerUUID) {
        return playerQuestData.computeIfAbsent(playerUUID, PlayerEntry::new);
    }

    public void populatePlayer(UUID playerUUID, PlayerEntry playerEntry) {
        playerQuestData.put(playerUUID, playerEntry);
    }

    public static class PlayerEntry {
        private final UUID playerId;
        private Set<String> finished;
        private List<ActiveQuest<?>> active;
        private final Set<String> activeIds = new HashSet<>();

        public PlayerEntry(final UUID playerId) {
            this.playerId = playerId;
        }

        public UUID playerId() {
            return playerId;
        }

        public PlayerEntry setActive(final List<ActiveQuest<?>> active) {
            this.active = active;
            activeIds.clear();
            for (final ActiveQuest<?> quest : active) activeIds.add(quest.id());
            return this;
        }

        public <T> boolean activate(ActiveQuest<T> quest) {
            if (finished.contains(quest.id()) || !activeIds.add(quest.id())) return false;
            if (active == null) active = new ArrayList<>(1);
            return active.add(quest);
        }

        public void finish(ActiveQuest<?> finishedQuest) {
            if (active == null) return;
            if (activeIds.remove(finishedQuest.id()) && active.removeIf(quest -> quest == finishedQuest)) {
                if (finished == null) finished = new HashSet<>(1);
                finished.add(finishedQuest.id());
            }
        }

        public boolean isActive(String questId) {
            return activeIds.contains(questId);
        }

        public List<ActiveQuest<?>> getActive() {
            return active == null ? Collections.emptyList() : active;
        }

        public boolean isFinished(String questId) {
            return finished != null && finished.contains(questId);
        }
    }
}
