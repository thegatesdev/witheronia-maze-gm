package io.github.thegatesdev.witheronia.maze_gm.registry;

import io.github.thegatesdev.mapletree.data.DataType;
import io.github.thegatesdev.mapletree.data.Readable;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;


public class MazeDataTypes {
    public static final DataType<TextComponent> COLORED_STRING = Readable.primitive("colored_text", TextComponent.class, primitive -> LegacyComponentSerializer.legacyAmpersand().deserialize(primitive.requireValue(String.class)));
}
