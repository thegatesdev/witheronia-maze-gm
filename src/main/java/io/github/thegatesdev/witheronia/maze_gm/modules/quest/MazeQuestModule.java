package io.github.thegatesdev.witheronia.maze_gm.modules.quest;

import io.github.thegatesdev.eventador.event.EventListener;
import io.github.thegatesdev.eventador.event.util.EventData;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class MazeQuestModule extends PluginModule<MazeGamemode> implements EventListener {

    private final QuestData questData = new QuestData();
    private final QuestListener questListener;

    public MazeQuestModule(final MazeGamemode plugin) {
        super("quests", plugin);
        questListener = new QuestListener(questData, new QuestGui(), EventData.of(plugin.eventManager(), PlayerEvent.class, PlayerEvent::getPlayer));
        plugin.listenerManager().add(this);
    }

    public QuestData questData() {
        return questData;
    }


    @Override
    public <E extends Event> boolean onEvent(@NotNull final E e, final Class<E> aClass) {
        if (!isEnabled) return false;
        return questListener.onEvent(e, aClass);
    }

    @Override
    public Set<Class<? extends Event>> eventSet() {
        return questListener.eventSet();
    }
}
