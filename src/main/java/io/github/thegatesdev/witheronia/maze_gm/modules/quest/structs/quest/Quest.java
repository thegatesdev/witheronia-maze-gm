package io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.quest;

import io.github.thegatesdev.mapletree.registry.Identifiable;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.goal.Goal;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public interface Quest<O> extends Identifiable {

    static <O> FunctionalQuest<O> build(String id) {
        return new FunctionalQuest<>(id);
    }

    static <O> ActiveQuest<O> activate(Quest<O> quest, Player player, O origin) {
        return new ActiveQuest<>(quest, player, origin);
    }

    static <O> ActiveQuest<O> activate(Quest<O> quest, Player player) {
        return activate(quest, player, null);
    }

    Set<String> requiredQuests();

    List<Goal<?, O>> goals();

    default void onActivate(Player player, ActiveQuest<O> quest) {
    }

    default void onFinish(Player player, ActiveQuest<O> quest) {
    }
}
