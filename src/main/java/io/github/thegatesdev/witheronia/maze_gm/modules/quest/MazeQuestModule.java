package io.github.thegatesdev.witheronia.maze_gm.modules.quest;

import io.github.thegatesdev.eventador.core.EventSet;
import io.github.thegatesdev.eventador.core.EventType;
import io.github.thegatesdev.eventador.listener.DynamicListener;
import io.github.thegatesdev.eventador.util.EventData;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.data.PlayerQuestData;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.data.QuestData;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.ActiveQuest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class MazeQuestModule extends PluginModule<MazeGamemode> implements DynamicListener {

    private final QuestData questData = new QuestData();

    private final EventData<Player> playerQuestEvents = new EventData<Player>(plugin.eventTypes())
            .add(PlayerEvent.class, PlayerEvent::getPlayer)
            .add(EntityEvent.class, event -> event.getEntity() instanceof Player player ? player : null)
            .add(BlockBreakEvent.class, BlockBreakEvent::getPlayer);
    private final EventSet eventSet = EventSet.mimic(plugin.eventTypes(), questData.questEvents());

    public MazeQuestModule(final MazeGamemode plugin) {
        super("quests", plugin);
    }

    @Override
    protected void onFirstLoad() {
        plugin.listenerManager().listen(this);
    }

    @Override
    public EventSet eventSet() {
        return eventSet;
    }

    @Override
    public <E extends Event> boolean callEvent(@NotNull final E e, @NotNull final EventType<E> type) {
        return handlePlayerGoals(e, type);
    }

    private <E extends Event> boolean handlePlayerGoals(E event, EventType<E> eventType) {
        playerQuestEvents.each(eventType, event, player -> {
            final PlayerQuestData playerData = questData.getPlayer(player.getUniqueId());
            if (playerData == null) return;
            for (final ActiveQuest activeQuest : playerData.activeQuests()) {
                if (activeQuest.questEvent(event, eventType)) {
                    if (activeQuest.finished()) playerData.finish(activeQuest);
                    return;
                }
            }
        });
        return false;
    }

    public QuestData questData() {
        return questData;
    }
}
