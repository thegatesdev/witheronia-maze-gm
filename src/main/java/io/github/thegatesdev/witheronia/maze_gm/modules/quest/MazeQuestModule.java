package io.github.thegatesdev.witheronia.maze_gm.modules.quest;

import io.github.thegatesdev.eventador.EventData;
import io.github.thegatesdev.eventador.core.EventSet;
import io.github.thegatesdev.eventador.core.EventType;
import io.github.thegatesdev.eventador.listener.DynamicListener;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.data.PlayerQuestData;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.data.QuestData;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.goal.CountingGoal;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.ActiveQuest;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.Quest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class MazeQuestModule extends PluginModule<MazeGamemode> implements DynamicListener {

    private final QuestData questData = new QuestData();

    private final EventData<Player> playerQuestEvents;

    public MazeQuestModule(final MazeGamemode plugin) {
        super("quests", plugin);
        plugin.listenerManager().listen(this);
        playerQuestEvents = new EventData<Player>(plugin.eventTypes())
                .add(PlayerEvent.class, PlayerEvent::getPlayer)
                .add(BlockBreakEvent.class, BlockBreakEvent::getPlayer);
    }

    {
        questData.addQuest(new Quest("test")
                .onActivate((player) -> player.sendMessage(Component.text("Quest activated!", TextColor.color(50, 180, 180))))
                .onFinish(player -> player.sendMessage(Component.text("Quest goals completed!", TextColor.color(0, 130, 160))))
                .addGoals(
                        new CountingGoal<>(plugin.eventTypes().get(BlockBreakEvent.class), event -> event.getBlock().getType() == Material.STONE).maxProgress(20)
                                .onProgress((event, progress, maxProgress) -> event.getPlayer().sendMessage(Component.text("%s stone to go".formatted(maxProgress - progress), TextColor.color(120, 255, 120))))
                                .onFail((event, progress, maxProgress) -> event.getPlayer().sendMessage(Component.text("You need to break %s more stone, not %s!".formatted(maxProgress - progress, event.getBlock().getType().name().toLowerCase()), TextColor.color(200, 50, 60))))
                                .onFinish((event, progress, maxProgress) -> event.getPlayer().sendMessage(Component.text("Well done!", TextColor.color(25, 220, 25))))
                ));
    }

    @Override
    public EventSet eventSet() {
        return playerQuestEvents.eventSet();
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
