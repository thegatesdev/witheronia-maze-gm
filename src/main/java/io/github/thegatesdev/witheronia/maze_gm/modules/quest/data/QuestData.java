package io.github.thegatesdev.witheronia.maze_gm.modules.quest.data;

import io.github.thegatesdev.eventador.core.EventType;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.Quest;

import java.util.*;

public class QuestData {

    private final Map<String, Quest> questMap = new HashMap<>();
    private final Map<UUID, PlayerQuestData> players = new LinkedHashMap<>();

    private final Set<EventType<?>> questEvents = new HashSet<>();

    public PlayerQuestData getOrCreatePlayer(UUID playerId) {
        return players.computeIfAbsent(playerId, PlayerQuestData::new);
    }

    public PlayerQuestData getPlayer(UUID playerId) {
        return players.get(playerId);
    }


    public Quest getQuest(String id) {
        return questMap.get(id);
    }

    public QuestData addQuest(Quest quest) {
        questMap.putIfAbsent(quest.id(), quest);
        questEvents.addAll(quest.listenedEvents());
        return this;
    }

    public String[] questKeys() {
        return questMap.keySet().toArray(new String[0]);
    }

    public Set<EventType<?>> questEvents() {
        return questEvents;
    }
}
