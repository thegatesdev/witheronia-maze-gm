package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;

public class MazeItemModule extends PluginModule<MazeGamemode> {

    private final MazeItems mazeItems;

    public MazeItemModule(final MazeGamemode mazeGamemode) {
        super("items", mazeGamemode);
        mazeItems = new MazeItems(mazeGamemode);
    }

    @Override
    protected void onFirstLoad() {
        plugin.onDataFileLoad(mazeItems::onDataFileLoad);
        plugin.stacker().itemManager().addGroup(mazeItems);
    }

    public MazeItems mazeItems() {
        return mazeItems;
    }
}
