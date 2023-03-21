package io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.quest;

import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.goal.Goal;

import java.util.*;

public class FunctionalQuest<O> implements Quest<O> {

    private final String id;
    private final List<Goal<?, O>> goals = new ArrayList<>();
    private Set<String> requiredQuests;

    FunctionalQuest(final String id) {
        this.id = id;
    }


    public FunctionalQuest<O> requiresQuest(String... ids) {
        if (requiredQuests == null) requiredQuests = new HashSet<>(ids.length);
        Collections.addAll(requiredQuests, ids);
        return this;
    }

    @SafeVarargs
    public final FunctionalQuest<O> goals(Goal<?, O>... goals) {
        this.goals.addAll(Arrays.asList(goals));
        return this;
    }


    @Override
    public Set<String> requiredQuests() {
        return requiredQuests;
    }

    @Override
    public List<Goal<?, O>> goals() {
        return goals;
    }

    @Override
    public String id() {
        return id;
    }
}
