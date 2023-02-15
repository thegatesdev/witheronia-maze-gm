package io.github.thegatesdev.witheronia.maze_gm.quest;

import org.bukkit.event.Event;

import java.util.*;

public class QuestData {

    private final Map<String, Quest> questPool = new HashMap<>();
    private final Set<Class<? extends Event>> questEvents = new HashSet<>();

    private final Map<UUID, PlayerEntry> playerQuestData = new HashMap<>();

    private final Map<UUID, List<String>> questEntities = new HashMap<>();


    public QuestData addQuest(Quest quest) {
        questPool.putIfAbsent(quest.id(), quest);
        questEvents.addAll(quest.getGoalEvents());
        return this;
    }

    public QuestData addQuest(Quest... quests) {
        for (final Quest quest : quests) addQuest(quest);
        return this;
    }

    public Quest getQuest(String id) {
        final Quest quest = questPool.get(id);
        if (quest == null) throw new RuntimeException("Unknown quest '%s'".formatted(id));
        return quest;
    }

    public Set<String> getQuestIds() {
        return questPool.keySet();
    }

    public Set<Class<? extends Event>> getQuestEvents() {
        return questEvents;
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


    public class PlayerEntry {
        private final Set<String> finished;
        private final List<Quest> active;

        private PlayerEntry(final Set<String> finished, final List<Quest> active) {
            this.finished = finished;
            this.active = active;
        }

        private PlayerEntry() {
            this(new HashSet<>(), new ArrayList<>());
        }

        public void setActive(String questId) {
            if (finished.contains(questId)) throw new RuntimeException("Cannot activate finished quest!");
            active.add(getQuest(questId));
        }

        public void setFinished(String questId) {
            active.remove(getQuest(questId));
            finished.add(questId);
        }

        public List<Quest> getActive() {
            return active;
        }

        public Set<String> getFinished() {
            return finished;
        }
    }
}
