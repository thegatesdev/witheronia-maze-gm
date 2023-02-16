package io.github.thegatesdev.witheronia.maze_gm.quest;

import io.github.thegatesdev.eventador.event.EventManager;
import io.github.thegatesdev.eventador.event.ListenerManager;
import io.github.thegatesdev.eventador.event.util.EventData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GoalListener implements ListenerManager.EventListener {
    private final QuestData questData;
    private final EventData<Player> playerGoalEvents;

    public GoalListener(final QuestData questData, final EventManager eventManager) {
        this.questData = questData;
        playerGoalEvents = new EventData<>(Player.class, eventManager)
                .add(PlayerEvent.class, "player", PlayerEvent::getPlayer);
    }

    @Override
    public <E extends Event> boolean callEvent(@NotNull final E event, final Class<E> eventClass) {
        for (final Player player : playerGoalEvents.getData(eventClass, event)) {
            UUID playerId = player.getUniqueId();
            QuestData.PlayerEntry playerQuests = questData.getPlayer(playerId);
            if (playerQuests == null) continue;
            for (final ActiveQuest<?> activeQuest : playerQuests.getActive()) {
                if (doesCompleteGoal(playerId, activeQuest, event, eventClass) && activeQuest.progress(player))
                    playerQuests.setFinished(activeQuest.quest().id());
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private <T, E extends Event> boolean doesCompleteGoal(UUID playerId, ActiveQuest<T> activeQuest, E event, Class<E> eventClass) {
        final Goal<?, T> goal = activeQuest.quest().currentGoal(playerId);
        return eventClass.isAssignableFrom(goal.getEventClass()) && ((Goal<E, T>) goal).didComplete(event, activeQuest.origin());
    }
}
