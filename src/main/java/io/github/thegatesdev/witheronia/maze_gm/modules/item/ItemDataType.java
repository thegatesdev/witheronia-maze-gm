package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.read.Readable;
import io.github.thegatesdev.maple.read.ReadableOptions;
import io.github.thegatesdev.maple.read.struct.AbstractDataType;
import io.github.thegatesdev.stacker.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import static io.github.thegatesdev.actionable.Actionable.COLORED_STRING;
import static io.github.thegatesdev.maple.read.Readable.enumeration;
import static io.github.thegatesdev.maple.read.Readable.string;

public class ItemDataType extends AbstractDataType<ItemDataType.ReadItem> {

    private static final ReadableOptions options = new ReadableOptions();

    static {
        options.add("material", enumeration(Material.class));
        options.add("key", string());
        options.addOptional("name", COLORED_STRING);
        options.addOptional("lore", COLORED_STRING.list());
        options.addOptional("flags", Readable.enumeration(ItemFlag.class).list());
    }

    protected ItemDataType() {
        super("maze_item");
    }

    @Override
    public ReadItem read(DataElement input) {
        var data = options.read(input.requireOf(DataMap.class));

        var id = data.getString("id");
        var builder = new ItemBuilder(data.getUnsafe("material"));

        data.ifValue("name", value -> builder.name(value.valueUnsafe()));
        data.ifList("lore", list -> builder.lore(list.valueListUnsafe()));
        data.ifList("flags", list -> builder.flag(list.valueListUnsafe()));

        return new ReadItem(id, builder);
    }

    public record ReadItem(String key,
                           ItemBuilder builder) {
    }
}
