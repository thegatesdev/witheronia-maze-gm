package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.eventador.EventData;
import io.github.thegatesdev.eventador.MappedListeners;
import io.github.thegatesdev.eventador.core.EventTypes;
import io.github.thegatesdev.eventador.listener.ListenerManager;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class MazeItemListeners extends MappedListeners<String> {
    public MazeItemListeners(final ListenerManager listenerManager, MazeItems mazeItems, EventTypes eventTypes) {
        super(listenerManager, new EventData<String>(eventTypes)
                .transform(ItemStack.class, mazeItems::itemId)
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
    }
}
