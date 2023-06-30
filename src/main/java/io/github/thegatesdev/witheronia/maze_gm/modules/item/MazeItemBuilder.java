package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.actionable.EventFactories;
import io.github.thegatesdev.actionable.factory.EventFactory;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.data.DataValue;
import io.github.thegatesdev.maple.read.Readable;
import io.github.thegatesdev.maple.read.ReadableOptions;
import io.github.thegatesdev.maple.read.struct.DataType;
import io.github.thegatesdev.maple.registry.struct.Identifiable;
import io.github.thegatesdev.stacker.CustomItem;
import io.github.thegatesdev.stacker.MetaBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.List;

import static io.github.thegatesdev.actionable.Actionable.COLORED_STRING;

public class MazeItemBuilder {
    public static DataType<DataValue<ReadItem>> createDataType(EventFactories eventFactories) {
        return Readable.map("maze_item", options -> DataValue.of(build(options)), options(eventFactories))
                .info(info -> info
                        .description("A item in the maze gamemode")
                );
    }

    private static ReadableOptions options(EventFactories eventFactories) {
        return new ReadableOptions()
                .add("material", Readable.enumeration(Material.class))
                .add("id", Readable.string())
                .addOptional("name", COLORED_STRING)
                .addOptional("lore", COLORED_STRING.list())
                .addOptional("flags", Readable.enumeration(ItemFlag.class))
                .addOptional("reactors", eventFactories.list());
    }

    private static ReadItem build(DataMap options) {
        final String itemId = options.getString("id");
        final MetaBuilder builder = new MetaBuilder(options.getObject("material", Material.class));
        options.ifValue("name", val -> builder.name(val.valueUnsafe()));
        options.ifList("lore", list -> list.eachValueOf(Component.class, builder::lore));
        options.ifList("flags", list -> list.eachValueOf(ItemFlag.class, builder::flag));
        return new ReadItem(
                itemId,
                new CustomItem(itemId, builder),
                options.getList("reactors").valueListUnsafe()
        );
    }

    public record ReadItem(String id, CustomItem display,
                           List<EventFactory<?>.ReadPerformers> listeners) implements Identifiable {
    }
}
