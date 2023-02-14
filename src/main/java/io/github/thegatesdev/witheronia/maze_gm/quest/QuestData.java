package io.github.thegatesdev.witheronia.maze_gm.quest;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.*;

public class QuestData {

    private final Map<String, Quest> questPool = new HashMap<>();

    private final Map<UUID, PlayerEntry> playerQuestData = new HashMap<>();

    private final Map<UUID, QuestLine> questEntities = new HashMap<>();
    private final Map<Chunk, Map<Location, QuestLine>> questBlocks = new HashMap<>();

    public QuestData addQuest(Quest quest) {
        questPool.putIfAbsent(quest.id(), quest);
        return this;
    }

    public Quest getQuest(String id) {
        return questPool.get(id);
    }

    public Set<String> getQuestIds() {
        return questPool.keySet();
    }


    public QuestLine getEntityQuests(UUID entityUUID) {
        return questEntities.get(entityUUID);
    }

    public QuestLine getEntityQuests(Entity entity) {
        return getEntityQuests(entity.getUniqueId());
    }

    public void addEntityQuest(UUID entity, String questId) {
        if (!questPool.containsKey(questId)) throw new RuntimeException("This quest does not exist");
        questEntities.computeIfAbsent(entity, uuid -> new QuestLine()).quests.add(questId);
    }

    public void populateEntity(UUID entity, QuestLine questLine) {
        questEntities.put(entity, questLine);
    }


    public QuestLine getBlockQuests(Location location) {
        final Map<Location, QuestLine> chunkLocations = questBlocks.get(location.getChunk());
        return chunkLocations == null ? null : chunkLocations.get(location);
    }

    public void addBockQuest(Location location, String questId) {
        if (!questPool.containsKey(questId)) throw new RuntimeException("This quest does not exist");
        questBlocks.computeIfAbsent(location.getChunk(), chunk -> new HashMap<>()).computeIfAbsent(location, location1 -> new QuestLine()).quests.add(questId);
    }


    public PlayerEntry getPlayer(UUID playerUUID) {
        return playerQuestData.computeIfAbsent(playerUUID, uuid -> new PlayerEntry(new HashSet<>(), new HashSet<>()));
    }

    public void populatePlayer(UUID playerUUID, PlayerEntry playerEntry) {
        playerQuestData.put(playerUUID, playerEntry);
    }


    public record PlayerEntry(Set<String> finished, Set<String> active) {
    }

    public record QuestLine(List<String> quests, Map<UUID, Integer> playerProgression) {
        public QuestLine() {
            this(new ArrayList<>(1), new HashMap<>());
        }

        public String getQuest(int index) {
            if (index >= quests.size()) return null;
            return quests.get(index);
        }
    }
}
