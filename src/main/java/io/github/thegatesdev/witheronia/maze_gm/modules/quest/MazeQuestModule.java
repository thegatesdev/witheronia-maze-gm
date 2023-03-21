package io.github.thegatesdev.witheronia.maze_gm.modules.quest;

import io.github.thegatesdev.eventador.EventListener;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.data.QuestData;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.goal.Goal;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.quest.Quest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MazeQuestModule extends PluginModule<MazeGamemode> implements EventListener {

    private final QuestData questData = new QuestData();
    private final QuestListener questListener = new QuestListener(questData, plugin.eventManager());

    private final Map<String, Quest<?>> testQuests = new HashMap<>();

    public MazeQuestModule(final MazeGamemode plugin) {
        super("quests", plugin);
        plugin.listenerManager().add(this);
    }

    {
        addTestQuest(Quest.build("test_quest_one").goals(
                Goal.build(BlockBreakEvent.class, (event, goal) -> event.getBlock().getType() == Material.STONE).times(10)
                        .onAccept((player, goal) -> player.sendMessage(Component.text("Break 10 stone.", TextColor.color(20, 200, 0))))
                        .onProgress((event, goal) -> event.getPlayer().sendMessage(Component.text("%s stone to go!".formatted(goal.progressNeeded() - goal.progress()), TextColor.color(20, 255, 100))))
                        .onFail((event, goal) -> event.getPlayer().sendMessage(Component.text("You need to break stone, not %s!".formatted(event.getBlock().getType().name().toLowerCase()))))
                        .onFinish((event, goal) -> event.getPlayer().sendMessage(Component.text("Well done %s!".formatted(event.getPlayer().getName()), TextColor.color(20, 200, 0))))
        ));
    }

    public void addTestQuest(Quest<?> quest) {
        testQuests.put(quest.id(), quest);
    }


    @Override
    public <E extends Event> boolean callEvent(@NotNull final E event, final Class<E> eventClass) {
        if (!isEnabled) return false;
        return questListener.handleQuestEvent(event, eventClass);
    }


    public QuestData questData() {
        return questData;
    }

    public Map<String, Quest<?>> testQuests() {
        return testQuests;
    }

    @Override
    public Set<Class<? extends Event>> eventSet() {
        return questListener.listenedEvents();
    }
}
