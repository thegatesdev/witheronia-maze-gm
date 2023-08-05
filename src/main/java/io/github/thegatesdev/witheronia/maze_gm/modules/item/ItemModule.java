package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.maple.read.struct.DataType;
import io.github.thegatesdev.stacker.item.ItemGroup;
import io.github.thegatesdev.threshold.pluginmodule.ModuleManager;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;

public class ItemModule extends PluginModule<MazeGamemode> {
    private final ItemGroup itemGroup = plugin.itemManager().addGroup("maze");
    private final DataType<ItemDataType.ReadItem> dataType = new ItemDataType();

    public ItemModule(ModuleManager<MazeGamemode> moduleManager) {
        super("items", moduleManager);
    }

    @Override
    protected void onInitialize() {
        plugin.EVENT_LOAD_DATAFILE.bind(this::onDataFileLoad);
    }


    private void onDataFileLoad(MazeGamemode.LoadDataFileInfo info) {
        info.data().ifList("maze_items", list -> list.each(element -> {
            var item = dataType.read(element);
            itemGroup.add(item.key(), item.builder());
        }));
    }


    public ItemGroup itemGroup() {
        return itemGroup;
    }
}
