package io.github.thegatesdev.witheronia.maze_gm.modules.quest.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class QuestData {

    private final Map<UUID, PlayerQuestData> players = new LinkedHashMap<>();


    public PlayerQuestData getOrCreatePlayer(UUID playerId) {
        return players.computeIfAbsent(playerId, PlayerQuestData::new);
    }

    public PlayerQuestData getPlayer(UUID playerId) {
        return players.get(playerId);
    }
}
