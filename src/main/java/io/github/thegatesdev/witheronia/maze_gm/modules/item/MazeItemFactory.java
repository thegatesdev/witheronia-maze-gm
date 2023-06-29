package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.actionable.EventFactories;
import io.github.thegatesdev.actionable.factory.EventFactory;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.data.DataValue;
import io.github.thegatesdev.maple.read.Readable;
import io.github.thegatesdev.maple.read.ReadableOptions;
import io.github.thegatesdev.maple.read.struct.ReadableOptionsHolder;
import io.github.thegatesdev.maple.registry.struct.Factory;
import io.github.thegatesdev.maple.registry.struct.Identifiable;
import io.github.thegatesdev.stacker.CustomItem;
import io.github.thegatesdev.stacker.MetaBuilder;
import io.github.thegatesdev.threshold.Threshold;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.Collections;
import java.util.List;

import static io.github.thegatesdev.actionable.Actionable.COLORED_STRING;

public class MazeItemFactory implements Factory<MazeItemFactory.ReadItem>, ReadableOptionsHolder {

    private final ReadableOptions itemOptions = new ReadableOptions()
            .add("material", Readable.enumeration(Material.class))
            .add("id", Readable.string())
            .addOptional("name", COLORED_STRING)
            .addOptional("lore", COLORED_STRING.list())
            .addOptional("flags", Readable.string().list());

    public MazeItemFactory(EventFactories eventFactories) {
        itemOptions.add("reactors", eventFactories.list());
    }

    @Override
    public ReadableOptions readableOptions() {
        return itemOptions;
    }

    @Override
    public ReadItem build(final DataMap data) {
        final DataMap options = itemOptions.read(data);
        final String itemId = options.getString("id");
        final MetaBuilder builder = new MetaBuilder(options.getObject("material", Material.class));
        options.ifValue("name", val -> builder.name(val.valueUnsafe()));
        options.ifValue("lore", val -> builder.lore(val.<List<Component>>valueUnsafe()));
        options.ifList("flags", elements -> elements.each(element -> {
            final ItemFlag flag = Threshold.enumGet(ItemFlag.class, element.requireOf(DataValue.class).stringValue());
            if (flag != null) builder.flag(flag);
        }));
        return new ReadItem(itemId, new CustomItem(itemId, builder), options.getUnsafe("reactors", Collections.emptyList()));
    }

    public record ReadItem(String id, CustomItem display,
                           List<EventFactory<?>.ReadPerformers> listeners) implements Identifiable {
    }
}
