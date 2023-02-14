package io.github.thegatesdev.witheronia.maze_gm.quest;

import io.github.thegatesdev.eventador.event.EventManager;
import io.github.thegatesdev.eventador.event.ListenerManager;
import io.github.thegatesdev.eventador.event.util.EventData;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class QuestHandler implements ListenerManager.EventListener {

    private final QuestData questData;

    private final EventData<Player> playerEvents;
    private final EventData<QuestData.QuestLine> questLineTriggers;

    public QuestHandler(final QuestData data, EventManager eventManager) {
        this.questData = data;

        playerEvents = new EventData<>(Player.class, eventManager).add(PlayerEvent.class, "player", PlayerEvent::getPlayer);
        questLineTriggers = new EventData<>(QuestData.QuestLine.class, eventManager)
                .add(PlayerInteractAtEntityEvent.class, "right_click_entity", event -> event.getHand() == EquipmentSlot.HAND,
                        event -> questData.getEntityQuests(event.getRightClicked()))
                .add(PlayerInteractEvent.class, "right_click_block", event -> event.hasBlock() && event.getHand() == EquipmentSlot.HAND,
                        event -> questData.getBlockQuests(Objects.requireNonNull(event.getClickedBlock()).getLocation()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Event> boolean callEvent(@NotNull final E e, final Class<E> eventClass) {
        final Iterable<Player> players = playerEvents.getData(eventClass, e);
        for (final Player player : players) { // The player might have completed a quest with this event.
            UUID playerId = player.getUniqueId();
            QuestData.PlayerEntry playerData = questData.getPlayer(playerId);
            for (final String questId : playerData.active()) {
                Quest quest = questData.getQuest(questId);
                Goal<?> goal = quest.currentGoal(playerId);
                if (!goal.eventClass().isAssignableFrom(eventClass)) continue;
                if (!((Goal<E>) goal).doesComplete(e)) continue;
                // Completed goal
                if (quest.progressPlayer(player)) { // Completed quest
                    playerData.active().remove(questId);
                    playerData.finished().add(questId);
                }
                return true;
            }
        }
        boolean cancel = false;
        for (final QuestData.QuestLine questLine : questLineTriggers.getData(eventClass, e)) {
            for (final Player player : players) {
                onQuestLineInteract(player, questLine);
                cancel = true;
            }
        }
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
        Set<String> required = newQuest.getRequiredQuests();
        if (required == null || playerData.finished().containsAll(required)) {
            newQuest.acceptPlayer(player);
            playerData.active().add(newQuestId);
        }
    }

    public Set<Class<? extends Event>> eventSet() {
        Set<Class<? extends Event>> eventSet = new HashSet<>();
        eventSet.addAll(playerEvents.eventSet());
        eventSet.addAll(questLineTriggers.eventSet());
        return eventSet;
    }
}
