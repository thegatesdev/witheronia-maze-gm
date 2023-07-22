package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.stacker.item.ItemGroup;
import io.github.thegatesdev.threshold.pluginmodule.ModuleManager;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.DataListeners;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;

public class MazeItemModule extends PluginModule<MazeGamemode> {
    private final ItemGroup mazeItems = plugin.itemManager().addGroup("maze");
    private final DataListeners<ItemStack, String> listeners = new DataListeners<>(plugin.listeners(), plugin.itemManager()::itemId);

    {
        listeners.data(PlayerInteractEvent.class, PlayerInteractEvent::getItem, "clicked_item");
        listeners.data(PlayerDropItemEvent.class, e -> e.getItemDrop().getItemStack(), "dropped_item");
        listeners.data(PlayerItemBreakEvent.class, PlayerItemBreakEvent::getBrokenItem, "broken_item");
        listeners.data(PlayerAttemptPickupItemEvent.class, e -> e.getItem().getItemStack(), "grabbing_item");
    }

    public MazeItemModule(ModuleManager<MazeGamemode> moduleManager) {
        super("items", moduleManager);
    }

    @Override
    protected void onInitialize() {
        plugin.EVENT_LOAD_DATAFILE.bind(this::onDataFileLoad);
    }

    @Override
    protected void onDisable() {
        listeners.enabled(false);
    }

    @Override
    protected void onEnable() {
        listeners.enabled(true);
    }

    @Override
    protected void onUnload() {
        mazeItems.clearItems();
        listeners.reset();
    }


    private void onDataFileLoad(MazeGamemode.LoadDataFileInfo info) {
        info.data().ifList("maze_items", list ->
            list.eachMap(data ->
                registerRead(MazeItemReader.read(data))));
    }

    private void registerRead(MazeItemReader.ReadItem item) {
        mazeItems.add(item.itemKey(), item.display());
        for (var listener : item.listeners())
            listeners.listen(item.itemKey(), listener.listener(), listener.dataKey());
    }
}
