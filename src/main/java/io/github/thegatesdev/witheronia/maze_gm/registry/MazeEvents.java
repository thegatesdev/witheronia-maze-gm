package io.github.thegatesdev.witheronia.maze_gm.registry;

import io.github.thegatesdev.eventador.event.EventData;
import io.github.thegatesdev.eventador.event.EventManager;
import io.github.thegatesdev.eventador.registry.ReactorFactories;
import io.github.thegatesdev.eventador.util.twin.Twin;
import io.github.thegatesdev.mapletree.data.Readable;
import io.github.thegatesdev.witheronia.maze_gm.util.spigot.ClickLocation;
import io.github.thegatesdev.witheronia.maze_gm.util.spigot.ClickType;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import static io.github.thegatesdev.eventador.registry.Factories.*;

public class MazeEvents extends ReactorFactories {

    public final EventData<ItemStack> itemStackEvents;
    public final EventData<Entity> entityEvents;

    public MazeEvents(EventManager eventManager) {
        super(eventManager);
        itemStackEvents = new EventData<>(ItemStack.class, eventManager);
        entityEvents = new EventData<>(Entity.class, eventManager);
        loadEventData();
        loadReactors();
    }

    private void loadEventData() {
        itemStackEvents.add(PlayerInteractEvent.class, "used_stack", PlayerInteractEvent::getItem)
                .add(PlayerDropItemEvent.class, "dropped_stack", event -> event.getItemDrop().getItemStack())
                .add(EntityDropItemEvent.class, "dropped_stack", event -> event.getItemDrop().getItemStack())
                .add(EntityPickupItemEvent.class, "picked_up_stack", event -> event.getItem().getItemStack())
                .add(PlayerItemBreakEvent.class, "broken_stack", PlayerItemBreakEvent::getBrokenItem)
                .add(PlayerItemConsumeEvent.class, "consumed_stack", PlayerItemConsumeEvent::getItem)
                .add(PlayerItemHeldEvent.class, "new_stack", event -> event.getPlayer().getInventory().getItem(event.getNewSlot()))
                .add(PlayerItemHeldEvent.class, "old_stack", event -> event.getPlayer().getInventory().getItem(event.getPreviousSlot()))
                .add(PlayerSwapHandItemsEvent.class, "main_hand_stack", PlayerSwapHandItemsEvent::getMainHandItem)
                .add(PlayerSwapHandItemsEvent.class, "off_hand_stack", PlayerSwapHandItemsEvent::getOffHandItem);
        entityEvents.add(EntityEvent.class, "entity", EntityEvent::getEntity)
                .add(PlayerEvent.class, "entity", PlayerEvent::getPlayer);
    }

    private void loadReactors() {
        addPerformers(entityEvents, ENTITY_ACTION, ENTITY_CONDITION);

        doWithFactories(EntityDamageEvent.class, eventFactory -> eventFactory.addPerformer("combined", e -> e instanceof EntityDamageByEntityEvent, e -> Twin.of(e.getEntity(), ((EntityDamageByEntityEvent) e).getDamager()), ENTITY_ENTITY_CONDITION, ENTITY_ENTITY_ACTION));
        doWithFactories(PlayerInteractEvent.class, eventFactory -> {
            eventFactory.getReadableData()
                    .add("click_type", Readable.enumeration(ClickType.class), ClickType.BOTH)
                    .add("click_location", Readable.enumeration(ClickLocation.class), ClickLocation.BOTH);
            eventFactory.addStaticCondition((data, e) -> {
                if (!ClickType.spigot(e.getAction()).compare(data.get("click_type", ClickType.class))) return false;
                return ClickLocation.spigot(e.getAction()).compare(data.get("click_location", ClickLocation.class));
            });
        });
    }
}
