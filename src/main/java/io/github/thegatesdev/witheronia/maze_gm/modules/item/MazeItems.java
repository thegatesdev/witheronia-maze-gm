package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.actionable.factory.EventFactory;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.exception.ElementException;
import io.github.thegatesdev.mapletree.data.Readable;
import io.github.thegatesdev.mapletree.data.ReadableOptions;
import io.github.thegatesdev.stacker.CustomItem;
import io.github.thegatesdev.stacker.ItemGroup;
import io.github.thegatesdev.stacker.MetaBuilder;
import io.github.thegatesdev.threshold.Threshold;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.data.MazeDataTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.logging.Logger;

public class MazeItems extends ItemGroup {

    private final Logger logger;

    private final ReadableOptions itemOptions = new ReadableOptions()
            .add("material", Readable.enumeration(Material.class))
            .add("id", Readable.primitive(String.class))
            .add("name", MazeDataTypes.COLORED_STRING, null)
            .add("lore", MazeDataTypes.COLORED_STRING.list(), null)
            .add("flags", Readable.primitive(String.class).list(), null);


    private final MazeItemListeners listeners;

    public MazeItems(MazeGamemode mazeGamemode) {
        super("maze_items", mazeGamemode.key("maze_item"), true);
        logger = mazeGamemode.getLogger();
        listeners = new MazeItemListeners(mazeGamemode.listenerManager(), this, mazeGamemode.eventTypes());
    }

    public void onDataFileLoad(final DataMap data) {
        data.ifList("maze_items", elements -> elements.iterator(DataMap.class).forEachRemaining(itemData -> {
            CustomItem item;
            try {
                item = loadItem(itemData);
            } catch (ElementException e) {
                logger.warning(e.getMessage());
                return;
            }
            register(item);
        }));
    }

    @Override
    public void clear() {
        super.clear();
        listeners.clear();
    }

    private CustomItem loadItem(DataMap data) {
        final DataMap options = itemOptions.read(data);
        final String itemId = options.getString("id");
        final MetaBuilder builder = new MetaBuilder(options.get("material", Material.class));
        options.ifPrimitive("name", primitive -> builder.name(primitive.valueUnsafe()));
        options.ifPrimitive("lore", primitive -> builder.lore(primitive.<List<Component>>valueUnsafe()));
        options.ifList("flags", elements -> {
            for (final String s : elements.primitiveList(String.class)) {
                final ItemFlag flag = Threshold.enumGet(ItemFlag.class, s);
                if (flag != null) builder.flag(flag);
            }
        });
        options.ifPrimitive("reactors", primitive -> {
            for (final var reactor : primitive.<List<EventFactory<?>.ReadPerformers>>valueUnsafe())
                listeners.listen(itemId, reactor);
        });
        return new CustomItem(itemId, builder);
    }
}
