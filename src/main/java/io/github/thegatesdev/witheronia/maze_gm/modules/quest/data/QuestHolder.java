package io.github.thegatesdev.witheronia.maze_gm.modules.quest.data;

import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.quest.Quest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record QuestHolder<O>(O origin, List<Quest<O>> quests) {
    public QuestHolder(O origin) {
        this(origin, new ArrayList<>());
    }

    @SafeVarargs
    public final QuestHolder<O> addQuests(Quest<O>... toAdd) {
        this.quests.addAll(Arrays.asList(toAdd));
        return this;
    }
}
