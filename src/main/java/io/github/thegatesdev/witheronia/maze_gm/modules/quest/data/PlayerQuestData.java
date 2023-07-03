package io.github.thegatesdev.witheronia.maze_gm.modules.quest.data;

import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.ActiveQuest;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.Quest;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerQuestData {

    private final UUID playerId;
    private final List<ActiveQuest> activeQuests = new ArrayList<>();
    private final Set<String> finished = new HashSet<>();

    private final Set<String> activeIds = new HashSet<>();

    public PlayerQuestData(final UUID id) {
        playerId = id;
    }

    public final <O> void activate(Quest quest, Player player) {
        if (activeIds.add(quest.key())) activeQuests.add(quest.createActive(player));
    }

    public boolean canActivate(Quest quest) {
        return quest.requiredQuests() == null || hasFinished(quest.requiredQuests());
    }

    public final <O> void finish(ActiveQuest toFinish) {
        final String questId = toFinish.questId();
        if (activeIds.remove(questId)) {
            activeQuests.remove(toFinish);
            finished.add(questId);
        }
    }

    public boolean hasFinished(String questId) {
        return finished.contains(questId);
    }

    public boolean hasFinished(Set<String> other) {
        return finished.containsAll(other);
    }

    public boolean isActive(String questId) {
        return activeIds.contains(questId);
    }


    public UUID playerId() {
        return playerId;
    }

    public List<ActiveQuest> activeQuests() {
        return activeQuests;
    }
}
