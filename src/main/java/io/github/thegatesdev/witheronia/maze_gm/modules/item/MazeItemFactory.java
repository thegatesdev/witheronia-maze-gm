package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.actionable.EventFactories;
import io.github.thegatesdev.actionable.factory.EventFactory;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.mapletree.data.Factory;
import io.github.thegatesdev.mapletree.data.Readable;
import io.github.thegatesdev.mapletree.data.ReadableOptions;
import io.github.thegatesdev.mapletree.data.ReadableOptionsHolder;
import io.github.thegatesdev.mapletree.registry.Identifiable;
import io.github.thegatesdev.stacker.CustomItem;
import io.github.thegatesdev.stacker.MetaBuilder;
import io.github.thegatesdev.threshold.Threshold;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.Collections;
import java.util.List;

import static io.github.thegatesdev.actionable.Actionable.COLORED_STRING;

public class MazeItemFactory implements Factory<MazeItemFactory.MazeItem>, ReadableOptionsHolder {

    private final ReadableOptions itemOptions = new ReadableOptions()
            .add("material", Readable.enumeration(Material.class))
            .add("id", Readable.primitive(String.class))
            .add("name", COLORED_STRING, null)
            .add("lore", COLORED_STRING.list(), null)
            .add("flags", Readable.primitive(String.class).list(), null);

    public MazeItemFactory(EventFactories eventFactories) {
        itemOptions.add("reactors", eventFactories);
    }

    @Override
    public ReadableOptions readableOptions() {
        return itemOptions;
    }

    @Override
    public MazeItem build(final DataMap data) {
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
        return new MazeItem(itemId, new CustomItem(itemId, builder), options.getUnsafe("reactors", Collections.emptyList()));
    }

    public record MazeItem(String id, CustomItem display,
                           List<EventFactory<?>.ReadPerformers> listeners) implements Identifiable {
    }
}
