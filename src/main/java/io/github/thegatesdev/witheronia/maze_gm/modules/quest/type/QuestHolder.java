package io.github.thegatesdev.witheronia.maze_gm.modules.quest.type;

import java.util.ArrayList;
import java.util.List;

public record QuestHolder<O>(O origin, List<Quest<O>> quests) {
    public QuestHolder(O origin) {
        this(origin, new ArrayList<>());
    }

    public QuestHolder<O> addQuest(Quest<O> quest) {
        this.quests.add(quest);
        return this;
    }
}
