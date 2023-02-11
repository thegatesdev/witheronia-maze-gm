package io.github.thegatesdev.witheronia.maze_gm.quest;

import java.util.*;

public class QuestData {

    private final Map<String, Quest> questPool = new HashMap<>();

    private final Map<UUID, PlayerEntry> playerQuestData = new HashMap<>();


    private final Map<UUID, QuestEntity> questEntities = new HashMap<>();


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
        QuestEntity entity = questEntities.get(entityUUID);
        if (entity == null) return null;
        return entity.questLine;
    }

    public void addEntityQuest(UUID entity, String questId) {
        if (!questPool.containsKey(questId)) throw new RuntimeException("This quest does not exist");
        questEntities.computeIfAbsent(entity, QuestEntity::new).questLine.quests.add(questId);
    }

    public void populateEntity(UUID entity, QuestEntity questEntity) {
        questEntities.put(entity, questEntity);
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

    public record QuestEntity(UUID uuid, QuestLine questLine) {
        public QuestEntity(UUID uuid) {
            this(uuid, new QuestLine());
        }
    }
}
