package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.actionable.builder.ReactorBuilder;
import io.github.thegatesdev.actionable.registry.Registries;
import io.github.thegatesdev.maple.data.DataList;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.read.Readable;
import io.github.thegatesdev.maple.read.ReadableOptions;
import io.github.thegatesdev.stacker.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.List;

import static io.github.thegatesdev.actionable.Actionable.COLORED_STRING;
import static io.github.thegatesdev.maple.read.Readable.enumeration;
import static io.github.thegatesdev.maple.read.Readable.string;

public class MazeItemReader {

    private static final ReadableOptions options = new ReadableOptions();

    static {
        options.add("material", enumeration(Material.class));
        options.add("id", string());
        options.addOptional("name", COLORED_STRING);
        options.addOptional("lore", COLORED_STRING.list());
        options.addOptional("flags", Readable.enumeration(ItemFlag.class).list());
        options.addOptional("reactors", Registries.REACTORS.list());
    }

    public static MazeItem read(DataMap input) {
        var data = options.read(input);

        var id = data.getString("id");
        var builder = new ItemBuilder(data.getUnsafe("material"));

        data.ifValue("name", value -> builder.name(value.valueUnsafe()));
        data.ifList("lore", list -> builder.lore(list.valueListUnsafe()));
        data.ifList("flags", list -> builder.flag(list.valueListUnsafe()));
        List<ReactorBuilder<?>.Reactor> reactors = data.getList("reactors", new DataList()).valueListUnsafe();

        return new MazeItem(id, builder, reactors);
    }

    public static ReadableOptions options() {
        return options;
    }

    public record MazeItem(String itemKey, ItemBuilder display, List<ReactorBuilder<?>.Reactor> reactors) {
    }
}
