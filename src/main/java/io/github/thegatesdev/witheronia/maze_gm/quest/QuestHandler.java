package io.github.thegatesdev.witheronia.maze_gm.quest;

import io.github.thegatesdev.eventador.event.EventManager;
import io.github.thegatesdev.eventador.event.ListenerManager;
import io.github.thegatesdev.eventador.event.util.EventData;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class QuestHandler implements ListenerManager.EventListener {

    private final QuestData questData;

    private final EventData<Player> questTriggers;
    private final EventData<QuestData.QuestLine> questLineTriggers;

    public QuestHandler(final QuestData data, EventManager eventManager) {
        this.questData = data;

        questTriggers = new EventData<>(Player.class, eventManager)
                .add(PlayerInteractEntityEvent.class, "right_click_entity", event -> event.getHand() == EquipmentSlot.HAND ? event.getPlayer() : null);
        questLineTriggers = new EventData<>(QuestData.QuestLine.class, eventManager)
                .add(PlayerInteractEntityEvent.class, "right_click_entity", event -> event.getHand() == EquipmentSlot.HAND ? questData.getEntityQuests(event.getRightClicked().getUniqueId()) : null);
    }

    @Override
    public <E extends Event> boolean callEvent(@NotNull final E e, final Class<E> eventClass) {
        Iterable<Player> players = questTriggers.getData(eventClass, e);
        if (players == null) return false;
        for (final Player player : players) {
            UUID playerId = player.getUniqueId();
            QuestData.PlayerEntry playerData = questData.getPlayer(playerId);
            for (final String questId : playerData.active()) {
                Quest quest = questData.getQuest(questId);
                Quest.Goal<?> goal = quest.currentGoal(playerId);
                if (eventClass.isAssignableFrom(goal.eventClass())) continue;
                //noinspection unchecked
                if (((Quest.Goal<E>) goal).tryComplete(e)) { // Completed goal
                    if (quest.progressPlayer(playerId)) { // Completed quest
                        playerData.active().remove(questId);
                        playerData.finished().add(questId);
                    }
                    return true;
                }
            }
        }
        boolean cancel = false;
        Iterable<QuestData.QuestLine> questLines = questLineTriggers.getData(eventClass, e);
        if (questLines != null) { // The player interacted with a questLine holder
            for (final QuestData.QuestLine questLine : questLines) {
                for (final Player player : players) {
                    onQuestLineInteract(player, questLine);
                    cancel = true;
                }
            }
        } // The player might have completed a quest with this event.

        return cancel;
    }

    private void onQuestLineInteract(Player player, QuestData.QuestLine questLine) {
        UUID playerId = player.getUniqueId();
        int progression = questLine.playerProgression().computeIfAbsent(playerId, u -> 0);

        String questId = questLine.getQuest(progression);
        if (questId == null) { // No more quests
            player.sendMessage(Component.text("You already finished this quest line!"));
            return;
        }
        QuestData.PlayerEntry playerData = questData.getPlayer(playerId);
        if (playerData.active().contains(questId)) { // Quest still active
            player.sendMessage(Component.text("Finish the current quest first!"));
            return;
        }
        if (playerData.finished().contains(questId)) { // Quest finished
            questLine.playerProgression().put(playerId, ++progression);
        }
        // New quest
        String newQuestId = questLine.getQuest(progression);
        if (newQuestId == null) {
            player.sendMessage(Component.text("You finished this quest line!"));
            return;
        }
        Quest newQuest = questData.getQuest(newQuestId);
        newQuest.acceptPlayer(playerId);
        playerData.active().add(newQuestId);
    }

    public Set<Class<? extends Event>> eventSet() {
        Set<Class<? extends Event>> eventSet = new HashSet<>();
        eventSet.addAll(questTriggers.eventSet());
        eventSet.addAll(questLineTriggers.eventSet());
        return eventSet;
    }
}
