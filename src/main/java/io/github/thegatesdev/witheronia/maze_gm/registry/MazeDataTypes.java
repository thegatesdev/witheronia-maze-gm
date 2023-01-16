package io.github.thegatesdev.witheronia.maze_gm.registry;

import io.github.thegatesdev.eventador.main.Eventador;
import io.github.thegatesdev.mapletree.data.DataType;
import io.github.thegatesdev.mapletree.data.Readable;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.HashMap;
import java.util.Map;


public class MazeDataTypes {
    public static final Map<String, Readable<?>> MAPPED_TYPES = new HashMap<>();

    public static <T extends Readable<?>> T mapType(T readable) {
        MAPPED_TYPES.putIfAbsent(readable.id(), readable);
        return readable;
    }

    static {
        mapType(Eventador.EFFECT_TYPE);
        mapType(Eventador.VECTOR);
    }

    public static final DataType<TextComponent> COLORED_STRING = mapType(Readable.primitive("colored_text", TextComponent.class, primitive -> LegacyComponentSerializer.legacyAmpersand().deserialize(primitive.requireValue(String.class))));
}
