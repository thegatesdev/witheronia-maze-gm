package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.read.Options;
import io.github.thegatesdev.stacker.item.ItemSettings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import static io.github.thegatesdev.actionable.Actionable.COLORED_STRING;
import static io.github.thegatesdev.maple.read.Readable.enumeration;
import static io.github.thegatesdev.maple.read.Readable.string;

public class ItemBuilder {

    public static final Options OPTIONS = new Options()
        .add("key", string())
        .add("material", enumeration(Material.class))
        .optional("name", COLORED_STRING)
        .optional("lore", COLORED_STRING.list())
        .optional("flags", enumeration(ItemFlag.class).list());

    public static Item build(DataMap input) {
        var data = Options.read(OPTIONS, input);

        var builder = new ItemSettings(data.getUnsafe("material"));

        data.ifValue("name", value -> builder.name(value.valueUnsafe()));
        data.ifList("lore", value -> builder.lore(value.valueListUnsafe()));
        data.ifList("flags", value -> builder.flag(value.valueListUnsafe()));

        return new Item(data.getString("key"), builder);
    }

    public record Item(String key, ItemSettings settings) {
    }
}
