package io.github.thegatesdev.witheronia.maze_gm.modules.quest.data;

import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.Quest;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class QuestData {

    private final Map<String, Quest> questMap = new HashMap<>();
    private final Map<UUID, PlayerQuestData> players = new LinkedHashMap<>();


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
        return this;
    }

    public String[] questKeys() {
        return questMap.keySet().toArray(new String[0]);
    }
}
