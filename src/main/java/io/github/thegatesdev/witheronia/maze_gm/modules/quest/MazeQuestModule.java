package io.github.thegatesdev.witheronia.maze_gm.modules.quest;

import io.github.thegatesdev.eventador.EventListener;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.data.QuestData;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.Quest;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.SimpleGoal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class MazeQuestModule extends PluginModule<MazeGamemode> implements EventListener {

    private final QuestData questData = new QuestData();
    private final QuestListener questListener = new QuestListener(questData, plugin.eventManager());

    public MazeQuestModule(final MazeGamemode plugin) {
        super("quests", plugin);
        plugin.listenerManager().add(this);
    }

    {
        questData.addQuest(new Quest("test")
                .onActivate((player) -> player.sendMessage(Component.text("Quest activated!", TextColor.color(50, 180, 180))))
                .onFinish(player -> player.sendMessage(Component.text("Quest goals completed!", TextColor.color(0, 130, 160))))
                .addGoals(
                        new SimpleGoal<>(BlockBreakEvent.class, event -> event.getBlock().getType() == Material.STONE).maxProgress(20)
                                .onProgress((event, progress, maxProgress) -> event.getPlayer().sendMessage(Component.text("%s stone to go".formatted(maxProgress - progress), TextColor.color(120, 255, 120))))
                                .onFail((event, progress, maxProgress) -> event.getPlayer().sendMessage(Component.text("You need to break %s more stone, not %s!".formatted(maxProgress - progress, event.getBlock().getType().name().toLowerCase()), TextColor.color(200, 50, 60))))
                                .onFinish((event, progress, maxProgress) -> event.getPlayer().sendMessage(Component.text("Well done!", TextColor.color(25, 220, 25))))
                ));
    }


    @Override
    public <E extends Event> boolean callEvent(@NotNull final E event, final Class<E> eventClass) {
        if (!isEnabled) return false;
        return questListener.handleQuestEvent(event, eventClass);
    }


    public QuestData questData() {
        return questData;
    }

    @Override
    public Set<Class<? extends Event>> eventSet() {
        return questListener.listenedEvents();
    }
}
