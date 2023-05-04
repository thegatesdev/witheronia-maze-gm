package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.eventador.core.EventTypes;
import io.github.thegatesdev.eventador.listener.ListenerManager;
import io.github.thegatesdev.eventador.util.EventData;
import io.github.thegatesdev.eventador.util.MappedListeners;
import io.github.thegatesdev.stacker.ItemGroup;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class MazeItemListener extends MappedListeners<ItemStack, String> {

    private final ItemGroup mazeItemGroup;

    public MazeItemListener(ListenerManager listenerManager, EventTypes eventTypes, ItemGroup mazeItemGroup) {
        super(listenerManager, new EventData<ItemStack>(eventTypes)
                .add(PlayerInteractEvent.class, PlayerInteractEvent::getItem)
                .add(PlayerSwapHandItemsEvent.class, PlayerSwapHandItemsEvent::getMainHandItem)
                .add(PlayerSwapHandItemsEvent.class, PlayerSwapHandItemsEvent::getOffHandItem)
                .add(PlayerDropItemEvent.class, e -> e.getItemDrop().getItemStack())
                .add(PlayerItemConsumeEvent.class, PlayerItemConsumeEvent::getItem)
                .add(PlayerItemHeldEvent.class, e -> e.getPlayer().getInventory().getItem(e.getNewSlot()))
                .add(PlayerItemHeldEvent.class, e -> e.getPlayer().getInventory().getItem(e.getPreviousSlot()))
                .add(PlayerItemBreakEvent.class, PlayerItemBreakEvent::getBrokenItem)
                .add(PlayerItemDamageEvent.class, PlayerItemDamageEvent::getItem)
                .add(PlayerAttemptPickupItemEvent.class, e -> e.getItem().getItemStack()));
        this.mazeItemGroup = mazeItemGroup;
    }

    @Override
    protected String getKey(ItemStack itemStack) {
        return mazeItemGroup.itemId(itemStack);
    }
}
