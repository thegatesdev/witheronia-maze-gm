package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.actionable.registry.Registries;
import io.github.thegatesdev.eventador.listener.struct.ClassListener;
import io.github.thegatesdev.maple.data.DataList;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.data.DataValue;
import io.github.thegatesdev.maple.read.Readable;
import io.github.thegatesdev.maple.read.ReadableOptions;
import io.github.thegatesdev.maple.read.struct.DataType;
import io.github.thegatesdev.stacker.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;

import java.util.List;

import static io.github.thegatesdev.actionable.Actionable.COLORED_STRING;
import static io.github.thegatesdev.maple.read.Readable.*;

public class MazeItemReader {

    private static final ReadableOptions options = new ReadableOptions();
    private static final DataType<DataValue<ReadListener>> ITEM_REACTOR = wrap("item_reactor", Registries.REACTORS, (element, value) -> {
        var dataKey = element.requireOf(DataMap.class).getString("dataKey", null);
        return value.then(ReadListener.class, classListener -> new ReadListener(classListener, dataKey));
    });

    static {
        options.add("material", enumeration(Material.class));
        options.add("id", string());
        options.addOptional("name", COLORED_STRING);
        options.addOptional("lore", COLORED_STRING.list());
        options.addOptional("flags", Readable.enumeration(ItemFlag.class).list());
        options.addOptional("reactors", ITEM_REACTOR.list());
    }

    public static ReadItem read(DataMap input) {
        var data = options.read(input);

        var id = data.getString("id");
        var builder = new ItemBuilder(data.getUnsafe("material"));

        data.ifValue("name", value -> builder.name(value.valueUnsafe()));
        data.ifList("lore", list -> builder.lore(list.valueListUnsafe()));
        data.ifList("flags", list -> builder.flag(list.valueListUnsafe()));

        return new ReadItem(id, builder, data.getList("reactors", new DataList()).valueListUnsafe());
    }

    public static ReadableOptions options() {
        return options;
    }

    public record ReadItem(String itemKey,
                           ItemBuilder display,
                           List<ReadListener> listeners) {
    }

    public record ReadListener(ClassListener<? extends Event> listener, String dataKey) {
    }
}
