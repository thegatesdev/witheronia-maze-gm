package io.github.thegatesdev.witheronia.maze_gm.modules.quest.data;

import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.quest.ActiveQuest;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.quest.Quest;

import java.util.*;

public class PlayerQuestData {

    private final UUID playerId;
    private final List<ActiveQuest<?>> activeQuests;
    private final Set<String> activeIds;
    private final Set<String> finished;

    public PlayerQuestData(final UUID id, final List<ActiveQuest<?>> quests, final Set<String> activeIds, final Set<String> finished) {
        playerId = id;
        activeQuests = quests;
        this.activeIds = activeIds;
        this.finished = finished;
    }

    public PlayerQuestData(final UUID id) {
        this(id, new ArrayList<>(), new HashSet<>(), new HashSet<>());
    }

    public final <O> boolean shouldActivate(Quest<O> quest) {
        return !finished.contains(quest.id()) && !activeIds.contains(quest.id()) && (quest.requiredQuests() == null || finished.containsAll(quest.requiredQuests()));
    }

    public final <O> void activate(ActiveQuest<O> toAdd) {
        if (activeIds.add(toAdd.questId())) activeQuests.add(toAdd);
    }

    public final <O> void finish(ActiveQuest<O> toFinish) {
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

    public List<ActiveQuest<?>> activeQuests() {
        return activeQuests;
    }
}
