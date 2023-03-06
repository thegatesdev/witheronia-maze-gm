package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.actionable.factory.ReactorFactory;
import io.github.thegatesdev.eventador.event.EventListener;
import io.github.thegatesdev.eventador.event.util.EventData;
import io.github.thegatesdev.eventador.event.util.MappedReactors;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.exception.ElementException;
import io.github.thegatesdev.mapletree.data.Readable;
import io.github.thegatesdev.mapletree.data.ReadableOptions;
import io.github.thegatesdev.stacker.CustomItem;
import io.github.thegatesdev.stacker.ItemGroup;
import io.github.thegatesdev.stacker.MetaBuilder;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.data.MazeDataTypes;
import io.github.thegatesdev.witheronia.maze_gm.util.DataFileLoader;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class MazeItemModule extends PluginModule<MazeGamemode> implements EventListener, DataFileLoader {

    private final Logger logger;

    private final ReadableOptions itemOptions = new ReadableOptions()
            .add("material", Readable.enumeration(Material.class))
            .add("id", Readable.primitive(String.class))
            .add("name", MazeDataTypes.COLORED_STRING, null)
            .add("lore", MazeDataTypes.COLORED_STRING.list(), null);

    private final ItemGroup itemGroup = new ItemGroup("maze_items", plugin.key("maze_item"), true);
    private final EventData<ItemStack> itemEvents;
    private final MappedReactors<ItemStack, String> itemReactors;

    public MazeItemModule(final MazeGamemode plugin) {
        super("items", plugin);
        itemReactors = new MappedReactors<>(plugin.eventManager(), itemEvents, itemGroup::itemId);
        plugin.addDataFileLoaders(this);
        plugin.listenerManager().add(this);
        plugin.stacker().itemManager().addGroup(itemGroup);
        this.logger = plugin.getLogger();
    }

    {
        itemEvents = new EventData<ItemStack>(plugin.eventManager()).add(PlayerInteractEvent.class, "used_stack", PlayerInteractEvent::getItem)
                .add(PlayerDropItemEvent.class, "dropped_stack", event -> event.getItemDrop().getItemStack())
                .add(EntityDropItemEvent.class, "dropped_stack", event -> event.getItemDrop().getItemStack())
                .add(EntityPickupItemEvent.class, "picked_up_stack", event -> event.getItem().getItemStack())
                .add(PlayerItemBreakEvent.class, "broken_stack", PlayerItemBreakEvent::getBrokenItem)
                .add(PlayerItemConsumeEvent.class, "consumed_stack", PlayerItemConsumeEvent::getItem)
                .add(PlayerItemHeldEvent.class, "new_stack", event -> event.getPlayer().getInventory().getItem(event.getNewSlot()))
                .add(PlayerItemHeldEvent.class, "old_stack", event -> event.getPlayer().getInventory().getItem(event.getPreviousSlot()))
                .add(PlayerSwapHandItemsEvent.class, "main_hand_stack", PlayerSwapHandItemsEvent::getMainHandItem)
                .add(PlayerSwapHandItemsEvent.class, "off_hand_stack", PlayerSwapHandItemsEvent::getOffHandItem);
    }

    // -- GET / SET

    public ItemGroup itemGroup() {
        return itemGroup;
    }


    // -- MODULE

    @Override
    public void onDataFileLoad(final DataMap data) {
        data.ifList("maze_items", elements -> elements.iterator(DataMap.class).forEachRemaining(itemData -> {
            CustomItem item;
            try {
                item = loadItem(itemData);
            } catch (ElementException e) {
                logger.warning("An item failed to load;");
                logger.warning(e.getMessage());
                return;
            }
            itemGroup.register(item);
        }));
    }

    @Override
    protected void onUnload() {
        itemGroup.clear();
    }

    // -- FUNCTIONALITY

    private CustomItem loadItem(DataMap data) {
        final DataMap options = itemOptions.read(data);
        final String itemId = options.getString("id");
        final MetaBuilder builder = new MetaBuilder(options.get("material", Material.class));
        options.ifPrimitive("name", primitive -> builder.name(primitive.valueUnsafe()));
        options.ifPrimitive("lore", primitive -> builder.lore(primitive.<List<Component>>valueUnsafe()));
        options.ifPrimitive("reactors", primitive -> {
            for (final ReactorFactory<?>.ReadReactor reactor : primitive.<List<ReactorFactory<?>.ReadReactor>>valueUnsafe())
                itemReactors.map(itemId, reactor.eventClass(), reactor);
        });
        return new CustomItem(itemId, builder);
    }

    @Override
    public <E extends Event> boolean onEvent(@NotNull final E e, final Class<E> eventClass) {
        if (!isEnabled) return false;
        return itemReactors.onEvent(e, eventClass);
    }

    @Override
    public Set<Class<? extends Event>> eventSet() {
        return itemReactors.eventSet();
    }
}
