package io.github.thegatesdev.witheronia.maze_gm.quest;

import io.github.thegatesdev.eventador.event.EventManager;
import io.github.thegatesdev.eventador.event.ListenerManager;
import io.github.thegatesdev.eventador.event.util.EventData;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
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
                .add(PlayerInteractEntityEvent.class, "interact_entity", PlayerEvent::getPlayer);
        questLineTriggers = new EventData<>(QuestData.QuestLine.class, eventManager)
                .add(PlayerInteractEntityEvent.class, "interact_entity", event -> {
                    QuestData.QuestEntity entity = questData.getEntity(event.getRightClicked().getUniqueId());
                    if (entity == null) return null;
                    return entity.questLine();
                });
    }

    @Override
    public <E extends Event> boolean callEvent(@NotNull final E e, final Class<E> eventClass) {
        Iterable<Player> playerData = questTriggers.getData(eventClass, e);
        if (playerData == null) return false;
        Iterable<QuestData.QuestLine> questLineData = questLineTriggers.getData(eventClass, e);
        if (questLineData != null) { // The player interacted with a questLine holder
            for (final QuestData.QuestLine questLine : questLineData) {
                if (questLine == null) continue;
                for (final Player player : playerData) {
                    if (player == null) continue;
                    onQuestLineInteract(player, questLine);
                }
            }
            return true;
        } else { // The player might have completed a quest with this event.
            for (final Player player : playerData) {
                if (player == null) continue;
                UUID playerId = player.getUniqueId();
                for (final String questId : questData.getPlayer(playerId).active()) {
                    Quest quest = questData.getQuest(questId);
                    Quest.Goal<?> goal = quest.currentGoal(playerId);
                    if (goal.eventClass() != eventClass) continue;
                    @SuppressWarnings("unchecked") Quest.Goal<E> goal1 = (Quest.Goal<E>) goal;
                    if (goal1.canProceed().test(e)) {
                        goal1.finish(e);
                        quest.progressPlayer(playerId).accept(playerId);
                    }
                }
            }
        }
        return false;
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
