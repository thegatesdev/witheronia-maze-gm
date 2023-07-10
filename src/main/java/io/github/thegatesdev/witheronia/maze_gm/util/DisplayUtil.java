package io.github.thegatesdev.witheronia.maze_gm.util;

import io.github.thegatesdev.maple.read.ReadableOptions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

public class DisplayUtil {
    public static final Style BLOCK_BORDER_STYLE = Style.style().color(NamedTextColor.GREEN).build();
    public static final Style TEXT_STYLE = Style.style().color(NamedTextColor.GRAY).build();
    public static final Style VAR_STYLE = Style.style().color(NamedTextColor.GOLD).build();
    public static final Style FAIL_STYLE = Style.style().color(NamedTextColor.RED).build();
    public static final Style SUCCEED_STYLE = Style.style().color(TextColor.color(158, 255, 5)).build();
    public static final Style VAR_VAL_STYLE = Style.style().color(NamedTextColor.AQUA).build();
    public static final Style EMPHASIS_STYLE = Style.style().color(NamedTextColor.BLUE).decorate(TextDecoration.ITALIC).build();

    public static Component displayBlock(Component title, Component body) {
        return Component.join(JoinConfiguration.newlines(),
            Component.text("------- ", BLOCK_BORDER_STYLE).append(title.applyFallbackStyle(TEXT_STYLE)),
            body.applyFallbackStyle(TEXT_STYLE),
            Component.text("-------", BLOCK_BORDER_STYLE)
        );
    }

    public static Component displayReadableOptions(ReadableOptions readableOptions) {
        final var entries = readableOptions.entries();
        final List<Component> out = new ArrayList<>(entries.size());
        for (var entry : entries) {
            final String dataTypeId = entry.key();
            out.add(Component.text()
                .append(Component.text(entry.key() + ": ", VAR_STYLE))
                .append(dataTypeId == null ? fail("unknown") : Component.text(dataTypeId + " ", VAR_VAL_STYLE))
                .append(entry.hasDefault() ? entry.defaultValue() == null ? Component.text("optional", EMPHASIS_STYLE) : Component.text("default: " + entry.defaultValue(), EMPHASIS_STYLE) : Component.text("required", EMPHASIS_STYLE))
                .build()
            );
        }
        return Component.join(JoinConfiguration.newlines(), out);
    }

    public static Component fail(String text) {
        return Component.text(text, FAIL_STYLE);
    }

    public static Component succeed(String text) {
        return Component.text(text, SUCCEED_STYLE);
    }
}
