package io.github.thegatesdev.witheronia.maze_gm.registry;

import io.github.thegatesdev.eventador.event.ListenerManager;
import io.github.thegatesdev.eventador.event.util.MappedReactors;
import io.github.thegatesdev.eventador.factory.ReactorFactory;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.mapletree.data.Readable;
import io.github.thegatesdev.mapletree.data.ReadableOptions;
import io.github.thegatesdev.mapletree.data.ReadableOptionsHolder;
import io.github.thegatesdev.skiller.CustomItem;
import io.github.thegatesdev.skiller.ItemGroup;
import io.github.thegatesdev.skiller.MetaBuilder;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class MazeItems extends ItemGroup implements ReadableOptionsHolder, ListenerManager.EventListener {
    private final MappedReactors<ItemStack, String> reactors;
    private final ReadableOptions itemOptions = new ReadableOptions()
            .add("material", Readable.enumeration(Material.class))
            .add("id", Readable.primitive(String.class))
            .add("name", MazeDataTypes.COLORED_STRING, null)
            .add("lore", MazeDataTypes.COLORED_STRING.list(), null);

    public MazeItems(MazeGamemode mazeGamemode) {
        super("maze_items", mazeGamemode.key("maze_item"), true);
        reactors = new MappedReactors<>(mazeGamemode.getEventManager(), mazeGamemode.getMazeEvents().itemStackEvents, this::itemId);
        itemOptions.add("reactors", mazeGamemode.getMazeEvents().list());
    }

    public void readItem(final DataMap dataMap) {
        final DataMap options = itemOptions.read(dataMap);

        final String itemId = options.getString("id");
        final MetaBuilder builder = new MetaBuilder(options.get("material", Material.class));
        options.ifPrimitive("name", primitive -> builder.name(primitive.valueUnsafe()));
        options.ifPrimitive("lore", primitive -> builder.addLore(primitive.<List<Component>>valueUnsafe()));
        options.ifPrimitive("reactors", primitive -> {
            for (final ReactorFactory<?>.ReadReactor reactor : primitive.<List<ReactorFactory<?>.ReadReactor>>valueUnsafe())
                this.reactors.addReactor(itemId, reactor.eventClass(), reactor);
        });

        register(new CustomItem(itemId, builder));
    }

    @Override
    public <E extends Event> boolean callEvent(@NotNull final E e, final Class<E> aClass) {
        return reactors.callEvent(e, aClass);
    }

    public Set<Class<? extends Event>> listenedEvents() {
        return reactors.listenedEvents();
    }

    @Override
    public ReadableOptions getReadableOptions() {
        return itemOptions;
    }

    @Override
    public void clear() {
        super.clear();
        reactors.clear();
    }
}
