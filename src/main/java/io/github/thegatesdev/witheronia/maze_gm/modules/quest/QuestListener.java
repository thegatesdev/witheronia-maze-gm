package io.github.thegatesdev.witheronia.maze_gm.modules.quest;

import io.github.thegatesdev.eventador.EventData;
import io.github.thegatesdev.eventador.EventManager;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.data.PlayerQuestData;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.data.QuestData;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.quest.ActiveQuest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class QuestListener {

    private final QuestData questData;
    private final EventData<Player> playerQuestEvents;

    public QuestListener(final QuestData data, EventManager eventManager) {
        questData = data;
        playerQuestEvents = new EventData<Player>(eventManager)
                //.add(PlayerEvent.class, PlayerEvent::getPlayer)
                .add(BlockBreakEvent.class, BlockBreakEvent::getPlayer);
    }

    public <E extends Event> boolean handleQuestEvent(@NotNull final E event, final Class<E> eventClass) {
        return handlePlayerGoals(event, eventClass);
    }

    private <E extends Event> boolean handlePlayerGoals(E event, Class<E> eventClass) {
        for (final Player player : playerQuestEvents.get(eventClass, event)) {
            final PlayerQuestData playerData = questData.getPlayer(player.getUniqueId());
            if (playerData == null) continue;
            for (final ActiveQuest<?> activeQuest : playerData.activeQuests()) {
                activeQuest.questEvent(eventClass, event);
                if (activeQuest.finished()) {
                    playerData.finish(activeQuest);
                    return true;
                }
            }
        }
        return false;
    }

    public Set<Class<? extends Event>> listenedEvents() {
        return playerQuestEvents.eventSet();
    }
}
