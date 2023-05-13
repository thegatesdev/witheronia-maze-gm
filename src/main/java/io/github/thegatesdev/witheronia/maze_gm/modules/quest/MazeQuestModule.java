package io.github.thegatesdev.witheronia.maze_gm.modules.quest;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.PlayerResultingCommandExecutor;
import dev.jorel.commandapi.executors.ResultingCommandExecutor;
import io.github.thegatesdev.eventador.core.EventSet;
import io.github.thegatesdev.eventador.core.EventType;
import io.github.thegatesdev.eventador.listener.dyn.DynamicListener;
import io.github.thegatesdev.eventador.util.EventData;
import io.github.thegatesdev.threshold.pluginmodule.ModuleManager;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.data.PlayerQuestData;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.data.QuestData;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.ActiveQuest;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.Quest;
import io.github.thegatesdev.witheronia.maze_gm.util.DisplayUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;

public class MazeQuestModule extends PluginModule<MazeGamemode> {

    private final QuestData questData = new QuestData();

    private final EventData<Player> playerQuestEvents = new EventData<Player>(plugin.eventTypes())
            .add(PlayerEvent.class, PlayerEvent::getPlayer)
            .add(EntityEvent.class, event -> event.getEntity() instanceof Player player ? player : null)
            .add(BlockBreakEvent.class, BlockBreakEvent::getPlayer);
    private final EventSet eventSet = EventSet.mimic(questData.questEvents());

    private final DynamicListener listener = DynamicListener.of(eventSet, this::onPlayerQuestEvent);

    public MazeQuestModule(ModuleManager<MazeGamemode> moduleManager) {
        super("quests", moduleManager);
    }

    // -- MODULE

    @Override
    protected void onInitialize() {
        plugin.witheroniaCommand().add(command());
    }

    @Override
    protected void onEnable() {
        plugin.listenerManager().listen(listener);
    }

    @Override
    protected void onDisable() {
        plugin.listenerManager().stop(listener);
    }

    // -- QUEST

    private <E extends Event> boolean onPlayerQuestEvent(E event, EventType<E> eventType) {
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

    // COMMAND

    private LiteralArgument command() {
        return (LiteralArgument) new LiteralArgument("quest")
                .then(argActivate());
    }

    private LiteralArgument argActivate() {
        return (LiteralArgument) new LiteralArgument("activate")
                .then(new StringArgument("quest_id")
                        .executesPlayer((PlayerResultingCommandExecutor) (sender, args) ->
                                handleActivateQuestCommand(sender, sender, args.getUnchecked("quest_id"), false))
                        .then(new LiteralArgument("force")
                                .executesPlayer((PlayerResultingCommandExecutor) (sender, args) ->
                                        handleActivateQuestCommand(sender, sender, args.getUnchecked("quest_id"), true)))
                        .then(new PlayerArgument("for_player")
                                .executes((ResultingCommandExecutor) (sender, args) ->
                                        handleActivateQuestCommand(sender, args.getUnchecked("for_player"), args.getUnchecked("quest_id"), false))
                                .then(new LiteralArgument("force")
                                        .executes((ResultingCommandExecutor) (sender, args) ->
                                                handleActivateQuestCommand(sender, args.getUnchecked("for_player"), args.getUnchecked("quest_id"), true))))
                );
    }

    private int handleActivateQuestCommand(CommandSender sender, Player forPlayer, String questId, boolean force) {
        assertEnabled();
        Quest quest = questData.getQuest(questId);
        if (quest == null) {
            sender.sendMessage(Component.text("Could not find quest " + questId + "!", DisplayUtil.FAIL_STYLE));
            return 0;
        }
        PlayerQuestData playerData = questData.getOrCreatePlayer(forPlayer.getUniqueId());
        if (!force && !playerData.canActivate(quest)) {
            sender.sendMessage(Component.text("Quest requirements not fulfilled for " + questId + "!", DisplayUtil.FAIL_STYLE));
            return 0;
        }
        playerData.activate(quest, forPlayer);
        return 1;
    }

    // -- GET / SET

    public QuestData questData() {
        return questData;
    }
}
