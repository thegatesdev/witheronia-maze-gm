package io.github.thegatesdev.witheronia.maze_gm.main;

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
import io.github.thegatesdev.witheronia.maze_gm.registry.MazeDataTypes;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MazeItems extends ItemGroup {
    private final MappedReactors<ItemStack, String> mazeItemReactors;
    private final ExpandableType<CustomItem> mazeItemType = new ExpandableType<>(CustomItem.class, new ReadableData()
            .add("material", Readable.enumeration(Material.class))
            .add("id", Readable.primitive(String.class)), data -> {
        final Material material = data.get("material", Material.class);
        return new CustomItem(data.getString("id"), material, new MetaBuilder(material));
    });

    public MazeItems(MazeGamemode mazeGamemode) {
        super("maze_items", mazeGamemode.key("maze_item"), true);
        mazeItemReactors = new MappedReactors<>(mazeGamemode.getEventManager(), mazeGamemode.getMazeEvents().itemStackEvents, this::itemId);
        mazeItemType
                .expand("reactors", mazeGamemode.getMazeEvents().listType(), (reactors, customItem) -> {
                    for (final ReactorFactory<?>.ReadReactor reactor : reactors)
                        mazeItemReactors.addReactor(customItem.id(), reactor.eventClass(), reactor);
                })
                .expand("name", MazeDataTypes.COLORED_STRING, (component, customItem) -> customItem.metaBuilder().name(component))
                .expand("lore", MazeDataTypes.COLORED_STRING.listType(), (components, customItem) -> customItem.metaBuilder().addLore(components));
    }

    public void read(final DataElement element) {
        register(mazeItemType.read(element));
    }

    public void remapEvents(ListenerManager listenerManager) {
        listenerManager.remap(mazeItemReactors, mazeItemReactors.listenedEvents());
    }
}
