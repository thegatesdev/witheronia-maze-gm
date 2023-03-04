package io.github.thegatesdev.witheronia.maze_gm.modules.command.admin;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.thegatesdev.actionable.Factories;
import io.github.thegatesdev.actionable.ReactorFactories;
import io.github.thegatesdev.actionable.factory.ReactorFactory;
import io.github.thegatesdev.mapletree.data.ReadableOptions;
import io.github.thegatesdev.mapletree.registry.DataTypeInfo;
import io.github.thegatesdev.witheronia.maze_gm.util.DisplayUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;

import java.util.ArrayList;
import java.util.List;

public class OptionsCommand {
    public static ArgumentTree eventOptionsArg(ReactorFactories reactorFactories) {
        return new LiteralArgument("event").then(
                new StringArgument("eventId").replaceSuggestions(ArgumentSuggestions.strings(reactorFactories.keySet()))
                        .executes((sender, args) -> {
                            final ReactorFactory<?> factory = reactorFactories.getFactory((String) args[0]);
                            sender.sendMessage(DisplayUtil.displayList(Component.text("Event " + args[0]), DisplayUtil.displayReadableData(factory.readableOptions())));
                        })
        );
    }

    public static ArgumentTree dataTypeOptionsArg() {
        return new LiteralArgument("datatype").then(
                new StringArgument("datatypeId").replaceSuggestions(ArgumentSuggestions.strings(DataTypeInfo.keys()))
                        .executes((sender, args) -> {
                            final String key = (String) args[0];
                            final DataTypeInfo<?, ?> info = DataTypeInfo.get(key);
                            if (info == null)
                                throw CommandAPI.failWithAdventureComponent(Component.text("DataType %s not found!".formatted(key), DisplayUtil.FAIL_STYLE));
                            final List<Component> toSend = new ArrayList<>();
                            for (final String s : info.description()) toSend.add(Component.text(s));
                            sender.sendMessage(DisplayUtil.displayList(Component.text(key), Component.join(JoinConfiguration.newlines(), toSend)));
                        })
        );
    }

    public static ArgumentTree factoryOptionsArg() {
        return new LiteralArgument("factory").then(
                new StringArgument("factoryType").replaceSuggestions(ArgumentSuggestions.strings(Factories.keys()))
                        .executes((sender, args) -> {
                            final String[] strings = Factories.get((String) args[0]).keyArray(new String[0]);
                            final ArrayList<Component> components = new ArrayList<>(strings.length);
                            for (final String string : strings)
                                components.add(Component.text(string, DisplayUtil.TEXT_STYLE));
                            sender.sendMessage(DisplayUtil.displayList(Component.text(""), Component.join(JoinConfiguration.commas(true), components)));
                        }).then(new StringArgument("factoryEntry").replaceSuggestions(ArgumentSuggestions.strings(info -> Factories.get((String) info.previousArgs()[0]).keyArray(new String[0])))
                                .executes((sender, args) -> {
                                    final String factoryType = (String) args[0];
                                    final String factoryEntry = (String) args[1];
                                    final ReadableOptions readableData = Factories.get(factoryType).get(factoryEntry).readableOptions();
                                    sender.sendMessage(DisplayUtil.displayList(Component.text("Data for " + factoryType + " " + factoryEntry), DisplayUtil.displayReadableData(readableData)));
                                })
                        )
        );
    }

    public static ArgumentTree readableOptionsArg(String name, ReadableOptions readableData) {
        return new LiteralArgument(name).executes((sender, args) -> {
            sender.sendMessage(DisplayUtil.displayList(Component.text("Options for " + name), DisplayUtil.displayReadableData(readableData)));
        });
    }
}
