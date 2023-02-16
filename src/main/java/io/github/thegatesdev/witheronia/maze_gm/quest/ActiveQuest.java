package io.github.thegatesdev.witheronia.maze_gm.quest;

import org.bukkit.entity.Player;

public record ActiveQuest<T>(Quest<T> quest, T origin) {

    public boolean progress(Player player) {
        return quest.progress(player, origin);
    }

    public void accept(Player player) {
        quest.accept(player, origin);
    }
}
