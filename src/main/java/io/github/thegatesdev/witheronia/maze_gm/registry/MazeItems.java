package io.github.thegatesdev.witheronia.maze_gm.registry;

import io.github.thegatesdev.eventador.event.ListenerManager;
import io.github.thegatesdev.eventador.event.util.MappedReactors;
import io.github.thegatesdev.eventador.factory.ReactorFactory;
import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.mapletree.data.ExpandableType;
import io.github.thegatesdev.mapletree.data.Readable;
import io.github.thegatesdev.mapletree.data.ReadableData;
import io.github.thegatesdev.skiller.CustomItem;
import io.github.thegatesdev.skiller.ItemGroup;
import io.github.thegatesdev.skiller.MetaBuilder;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MazeItems extends ItemGroup {
    private final MappedReactors<ItemStack, String> reactors;
    private final ExpandableType<CustomItem> type = new ExpandableType<>("maze_item", new ReadableData()
            .add("material", Readable.enumeration(Material.class))
            .add("id", Readable.primitive(String.class)), data -> {
        final Material material = data.get("material", Material.class);
        return new CustomItem(data.getString("id"), material, new MetaBuilder(material));
    });

    public MazeItems(MazeGamemode mazeGamemode) {
        super("maze_items", mazeGamemode.key("maze_item"), true);
        reactors = new MappedReactors<>(mazeGamemode.getEventManager(), mazeGamemode.getMazeEvents().itemStackEvents, this::itemId);
        type
                .expand("reactors", mazeGamemode.getMazeEvents().listType(), (reactors, customItem) -> {
                    for (final ReactorFactory<?>.ReadReactor reactor : reactors)
                        this.reactors.addReactor(customItem.id(), reactor.eventClass(), reactor);
                })
                .expand("name", MazeDataTypes.COLORED_STRING, (component, customItem) -> customItem.metaBuilder().name(component))
                .expand("lore", MazeDataTypes.COLORED_STRING.listType(), (components, customItem) -> customItem.metaBuilder().addLore(components));
    }

    public void read(final DataElement element) {
        register(type.read(element));
    }

    public void remapEvents(ListenerManager listenerManager) {
        listenerManager.remap(reactors, reactors.listenedEvents());
    }

    public ExpandableType<CustomItem> getType() {
        return type;
    }

    @Override
    public void clear() {
        super.clear();
        reactors.clear();
    }
}
