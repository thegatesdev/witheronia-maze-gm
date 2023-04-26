package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.eventador.util.EventData;
import io.github.thegatesdev.eventador.util.MappedListeners;
import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.stacker.ItemGroup;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.Queue;

public class MazeItemModule extends PluginModule<MazeGamemode> {

    private final MazeItemFactory mazeItemFactory = new MazeItemFactory(plugin.mazeReactors());
    private final ItemGroup mazeItemGroup = new ItemGroup("maze_items", plugin.key("maze_item"), true);
    private final MappedListeners<String> mazeItemListeners = new MappedListeners<>(plugin.listenerManager(), new EventData<String>(plugin.eventTypes())
            .transform(ItemStack.class, mazeItemGroup::itemId)
            .add(PlayerInteractEvent.class, PlayerInteractEvent::getItem)
            .add(PlayerSwapHandItemsEvent.class, PlayerSwapHandItemsEvent::getMainHandItem)
            .add(PlayerSwapHandItemsEvent.class, PlayerSwapHandItemsEvent::getOffHandItem)
            .add(PlayerDropItemEvent.class, e -> e.getItemDrop().getItemStack())
            .add(PlayerItemConsumeEvent.class, PlayerItemConsumeEvent::getItem)
            .add(PlayerItemHeldEvent.class, e -> e.getPlayer().getInventory().getItem(e.getNewSlot()))
            .add(PlayerItemHeldEvent.class, e -> e.getPlayer().getInventory().getItem(e.getPreviousSlot()))
            .add(PlayerItemBreakEvent.class, PlayerItemBreakEvent::getBrokenItem)
            .add(PlayerItemDamageEvent.class, PlayerItemDamageEvent::getItem)
            .add(PlayerAttemptPickupItemEvent.class, e -> e.getItem().getItemStack())
            .close());

    private final Queue<MazeItemFactory.MazeItem> loadingItems = new ArrayDeque<>();

    // -- MODULE

    public MazeItemModule(final MazeGamemode plugin) {
        super("items", plugin);
    }

    @Override
    protected void onFirstLoad() {
        plugin.EVENT_LOAD_DATAFILE.bind(this::onDataFileLoad);
    }

    @Override
    protected void onUnload() {
        mazeItemListeners.clear();
        mazeItemGroup.clear();
        loadingItems.clear();
    }

    @Override
    protected void onLoad() {
        pollLoaded();
    }

    // -- LOADING

    private void pollLoaded() {
        var polled = loadingItems.poll();
        while (polled != null) {
            register(polled);
            polled = loadingItems.poll();
        }
    }

    private void onDataFileLoad(MazeGamemode.LoadDataFileInfo info) {
        if (!isEnabled) return;
        info.data().ifList("maze_items", list -> {
            for (DataElement el : list)
                if (el.isMap()) loadingItems.add(mazeItemFactory.build(el.asMap()));
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
