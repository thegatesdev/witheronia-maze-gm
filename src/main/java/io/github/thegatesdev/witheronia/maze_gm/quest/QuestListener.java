package io.github.thegatesdev.witheronia.maze_gm.quest;

import io.github.thegatesdev.threshold.FastListener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class QuestListener implements FastListener {

    private final QuestData questData;

    public QuestListener(QuestData questData) {
        this.questData = questData;
    }

    public void handleEntityInteract(PlayerInteractAtEntityEvent event) {

    }
}
