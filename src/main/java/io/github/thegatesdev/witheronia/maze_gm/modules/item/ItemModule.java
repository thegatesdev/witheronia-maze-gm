package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.stacker.item.ItemGroup;
import io.github.thegatesdev.threshold.pluginmodule.ModuleManager;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;

public class ItemModule extends PluginModule<MazeGamemode> {
    private final ItemGroup itemGroup = plugin.itemManager().addGroup("maze");

    public ItemModule(ModuleManager<MazeGamemode> moduleManager) {
        super("items", moduleManager);
    }


    public ItemGroup itemGroup() {
        return itemGroup;
    }
}
