package io.github.thegatesdev.witheronia.maze_gm.util;

import io.github.thegatesdev.mapletree.data.ReadableOptions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DisplayUtil {
    public static final Style LIST_BORDER_STYLE = Style.style().color(NamedTextColor.GREEN).build();
    public static final Style TEXT_STYLE = Style.style().color(NamedTextColor.GRAY).build();
    public static final Style VAR_STYLE = Style.style().color(NamedTextColor.GOLD).build();
    public static final Style FAIL_STYLE = Style.style().color(NamedTextColor.RED).build();
    public static final Style SUCCEED_STYLE = Style.style().color(TextColor.color(158, 255, 5)).build();
    public static final Style VAR_VAL_STYLE = Style.style().color(NamedTextColor.AQUA).build();
    public static final Style EMPHASIS_STYLE = Style.style().color(NamedTextColor.BLUE).decorate(TextDecoration.ITALIC).build();

    public static Component displayList(Component title, Component body) {
        return Component.join(JoinConfiguration.newlines(),
                Component.text("------- ", LIST_BORDER_STYLE).append(title.style(EMPHASIS_STYLE)),
                body.applyFallbackStyle(TEXT_STYLE),
                Component.text("-------", LIST_BORDER_STYLE)
        );
    }

    public static Component displayReadableData(ReadableOptions readableData) {
        final Map<String, ReadableOptions.Entry<?>> entries = readableData.getEntries();
        final List<Component> out = new ArrayList<>(entries.size());
        entries.forEach((s, entry) -> {
            final String id = entry.dataType().id();
            out.add(Component.text()
                    .append(Component.text(s + ": ", VAR_STYLE))
                    .append(id == null ? Component.text("unknown ", FAIL_STYLE) : Component.text(id + " ", VAR_VAL_STYLE))
                    .append(entry.hasDefault() ? entry.getDefaultValue() == null ? Component.text("optional", EMPHASIS_STYLE) : Component.text("default: " + entry.getDefaultValue(), EMPHASIS_STYLE) : Component.text("required", EMPHASIS_STYLE))
                    .build()
            );
        });
        return Component.join(JoinConfiguration.newlines(), out);
    }
}
