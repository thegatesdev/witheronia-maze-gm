package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.eventador.util.MappedListeners;
import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.exception.ElementException;
import io.github.thegatesdev.stacker.ItemGroup;
import io.github.thegatesdev.threshold.pluginmodule.ModuleManager;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MazeItemModule extends PluginModule<MazeGamemode> {

    private final MazeItemFactory mazeItemFactory = new MazeItemFactory(plugin.mazeReactors());
    private final ItemGroup mazeItemGroup = new ItemGroup("maze_items", plugin.key("maze_item"), true);
    private final MappedListeners<ItemStack, String> mazeItemListeners = new MazeItemListener(plugin.listenerManager(), plugin.eventTypes(), mazeItemGroup);

    private final List<MazeItemFactory.MazeItem> loadedItems = new ArrayList<>();

    public MazeItemModule(ModuleManager<MazeGamemode> moduleManager) {
        super("items", moduleManager);
    }

    // -- MODULE


    @Override
    protected void onInitialize() {
        plugin.EVENT_LOAD_DATAFILE.bind(this::onDataFileLoad);
    }

    @Override
    protected void onUnload() {
        mazeItemListeners.clear();
        mazeItemGroup.clear();
        loadedItems.clear();
    }

    @Override
    protected void onLoad() {
        pollLoaded();
    }

    // -- LOADING

    private void pollLoaded() {
        loadedItems.forEach(this::register);
        logger.info("Successfully loaded " + loadedItems.size() + " items");
        loadedItems.clear();
    }

    private void onDataFileLoad(MazeGamemode.LoadDataFileInfo info) {
        info.data().ifList("maze_items", list -> {
            for (DataElement el : list) {
                if (el.isMap()) try {
                    loadedItems.add(mazeItemFactory.build(el.asMap()));
                } catch (ElementException e) {
                    plugin.logError(e);
                }
            }
        });
    }

    // -- ITEMS

    public synchronized void register(MazeItemFactory.MazeItem item) {
        mazeItemGroup.register(item.display());
        for (var listener : item.listeners()) mazeItemListeners.listen(item.id(), listener);
    }

    // -- GET / SET

    public ItemGroup mazeItemGroup() {
        return mazeItemGroup;
    }
}
