package io.github.thegatesdev.witheronia.maze_gm.modules.quest;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import io.github.thegatesdev.eventador.event.EventListener;
import io.github.thegatesdev.eventador.event.util.EventData;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.type.ActiveQuest;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.type.QuestHolder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class QuestListener implements EventListener {
    private final QuestData questData;

    private final QuestGui questGui;
    private final EventData<Player> playerGoalEvents;

    public QuestListener(final QuestData questData, QuestGui questGui, final EventData<Player> playerGoalEvents) {
        this.questData = questData;
        this.questGui = questGui;
        this.playerGoalEvents = playerGoalEvents;
    }

    private <E extends Event> boolean handleEventGoals(E event, Class<E> eventClass) {
        for (final Player player : playerGoalEvents.get(eventClass, event)) {
            UUID playerId = player.getUniqueId();
            QuestData.PlayerEntry playerQuests = questData.getPlayer(playerId);
            if (playerQuests == null) continue;
            for (final ActiveQuest<?> activeQuest : playerQuests.getActive()) {
                activeQuest.goalEvent(event, eventClass);
                if (activeQuest.finished()) playerQuests.finish(activeQuest);
            }
        }
        return false;
    }

    private <O> void handleEventQuests(Player player, O origin, QuestHolder<O> holder) {
        ChestGui gui = questGui.create(questData.getOrCreatePlayer(player.getUniqueId()), origin, holder.quests(), "Quests");
        gui.show(player);
    }

    @Override
    public <E extends Event> boolean onEvent(@NotNull final E event, final Class<E> eventClass) {
        if (handleEventGoals(event, eventClass)) return false;

        if (event instanceof PlayerInteractAtEntityEvent playerInteractAtEntityEvent) {
            Entity clicked = playerInteractAtEntityEvent.getRightClicked();
            QuestHolder<Entity> holder = questData.getEntity(clicked.getUniqueId());
            if (holder != null) handleEventQuests(playerInteractAtEntityEvent.getPlayer(), clicked, holder);
        }

        return false;
    }

    @Override
    public Set<Class<? extends Event>> eventSet() {
        return null;
    }
}
