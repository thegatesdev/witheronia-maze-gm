package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.actionable.builder.ReactorBuilder;
import io.github.thegatesdev.stacker.item.ItemGroup;
import io.github.thegatesdev.threshold.pluginmodule.ModuleManager;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;

public class MazeItemModule extends PluginModule<MazeGamemode> {
    private final ItemGroup mazeItems = plugin.itemManager().addGroup("maze");
    private final MazeItemListener listener = new MazeItemListener(plugin.itemManager(), plugin.listeners());

    public MazeItemModule(ModuleManager<MazeGamemode> moduleManager) {
        super("items", moduleManager);
    }

    @Override
    protected void onInitialize() {
        plugin.EVENT_LOAD_DATAFILE.bind(this::onDataFileLoad);
    }

    @Override
    protected void onDisable() {
        listener.enabled(false);
    }

    @Override
    protected void onEnable() {
        listener.enabled(true);
    }

    @Override
    protected void onUnload() {
        mazeItems.clearItems();
        listener.clear();
    }


    public void registerItem(MazeItemReader.MazeItem item) {
        mazeItems.overwrite(item.itemKey(), item.display());
        for (ReactorBuilder<?>.Reactor reactor : item.reactors()) listener.listen(item.itemKey(), reactor);
    }

    private void onDataFileLoad(MazeGamemode.LoadDataFileInfo info) {
        info.data().ifList("maze_items", list ->
            list.eachMap(data ->
                registerItem(MazeItemReader.read(data))));
    }
}
