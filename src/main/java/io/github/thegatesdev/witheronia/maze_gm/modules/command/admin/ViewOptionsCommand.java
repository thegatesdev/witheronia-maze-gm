package io.github.thegatesdev.witheronia.maze_gm.modules.command.admin;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.thegatesdev.actionable.EventFactories;
import io.github.thegatesdev.actionable.Factories;
import io.github.thegatesdev.actionable.factory.EventFactory;
import io.github.thegatesdev.mapletree.data.ReadableOptionsHolder;
import io.github.thegatesdev.mapletree.registry.DataTypeInfo;
import io.github.thegatesdev.mapletree.registry.FactoryRegistry;
import io.github.thegatesdev.witheronia.maze_gm.util.DisplayUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.thegatesdev.witheronia.maze_gm.util.DisplayUtil.*;
import static net.kyori.adventure.text.Component.text;

public class ViewOptionsCommand {
    private final Map<String, Component> dataTypeDisplayCache = new HashMap<>();
    private final EventFactories eventFactories;

    public ViewOptionsCommand(EventFactories eventFactories) {
        this.eventFactories = eventFactories;
    }

    private LiteralArgument eventArg(EventFactories eventFactories) {
        return (LiteralArgument) new LiteralArgument("event")
                .executes((sender, args) -> {
                    sender.sendMessage(DisplayUtil.displayBlock(text("Available event factories"), text(String.join("\n", eventFactories.keys()), VAR_VAL_STYLE)));
                }).then(new StringArgument("event_id").replaceSuggestions(ArgumentSuggestions.strings(eventFactories.keys()))
                        .executes((sender, args) -> {
                            String eventId = (String) args.get("event_id");
                            final EventFactory<?> factory = eventFactories.getFactory(eventId);
                            if (factory == null) {
                                sender.sendMessage(text("Unknown event factory: " + eventId + "!", FAIL_STYLE));
                                return 0;
                            }
                            sender.sendMessage(displayBlock(text("Options for " + eventId), displayReadableOptions(factory.readableOptions())));
                            return 1;
                        })
                );
    }

    private LiteralArgument dataTypeInfoArg() {
        return (LiteralArgument) new LiteralArgument("datatype")
                .executes((sender, args) -> {
                    sender.sendMessage(DisplayUtil.displayBlock(text("Available dataTypes"), text(String.join("\n", DataTypeInfo.keys()), VAR_VAL_STYLE)));
                }).then(new StringArgument("datatype_id").replaceSuggestions(ArgumentSuggestions.strings(DataTypeInfo.keys()))
                        .executes((sender, args) -> {
                            sender.sendMessage(dataTypeDisplayCache.computeIfAbsent((String) args.get("datatype_id"), key -> {
                                final DataTypeInfo<?, ?> info = DataTypeInfo.get(key);
                                if (info == null) return text("Could not find info for datatype " + key + "!", FAIL_STYLE);

                                var toDisplay = new ArrayList<Component>();

                                if (info.description() != null)
                                    toDisplay.add(text(info.description(), TEXT_STYLE));

                                if (info.origin() != null)
                                    toDisplay.add(text().append(
                                            text("From: ", EMPHASIS_STYLE),
                                            text(info.origin(), VAR_VAL_STYLE)).build());

                                if (info.representation() != null)
                                    toDisplay.add(text().append(
                                            text("Use: ", EMPHASIS_STYLE),
                                            text(key + "_option: ", VAR_STYLE),
                                            text(info.representation(), VAR_VAL_STYLE)).build());

                                if (info.possibleValues() != null) {
                                    List<String> possibleValues = info.possibleValues();
                                    int maxLen = 10;
                                    toDisplay.add(text().append(
                                            text("Possible values: ", EMPHASIS_STYLE),
                                            text(possibleValues.stream().limit(maxLen).collect(Collectors.joining(", ")) +
                                                            (possibleValues.size() > maxLen ? " and" + (possibleValues.size() - maxLen) + " more..." : ""),
                                                    VAR_VAL_STYLE)).build());
                                }

                                if (info.readableOptions() != null)
                                    toDisplay.add(DisplayUtil.displayReadableOptions(info.readableOptions()));

                                return DisplayUtil.displayBlock(text("DataType " + key), Component.join(JoinConfiguration.newlines(), toDisplay));
                            }));
                        })
                );
    }

    private LiteralArgument factoriesArg() {
        return (LiteralArgument) new LiteralArgument("factory").executes((sender, args) -> {
            sender.sendMessage(DisplayUtil.displayBlock(text("Available factories"), text(String.join("\n", Factories.keys()), VAR_VAL_STYLE)));
        }).then(new StringArgument("factory_id")
                .replaceSuggestions(ArgumentSuggestions.strings(Factories.keys()))
                .executes((sender, args) -> {
                    String factoryId = args.getUnchecked("factory_id");
                    FactoryRegistry<?, ?> factoryRegistry = Factories.get(factoryId);
                    if (factoryRegistry == null) {
                        sender.sendMessage(text("Unknown factory: " + factoryId, FAIL_STYLE));
                        return 0;
                    }
                    sender.sendMessage(DisplayUtil.displayBlock(text("Available factories"), text(String.join("\n", factoryRegistry.keys()), VAR_VAL_STYLE)));
                    return 1;
                }).then(new StringArgument("factory_entry_id")
                        .replaceSuggestions(ArgumentSuggestions.stringCollection(info -> Factories.get(info.previousArgs().getUnchecked("factory_id")).keys()))
                        .executes((sender, args) -> {
                            String factoryId = args.getUnchecked("factory_id");
                            FactoryRegistry<?, ?> factoryRegistry = Factories.get(factoryId);
                            if (factoryRegistry == null) {
                                sender.sendMessage(text("Unknown factory registry " + factoryId, FAIL_STYLE));
                                return 0;
                            }
                            String factoryEntryId = args.getUnchecked("factory_entry_id");
                            ReadableOptionsHolder factory = factoryRegistry.get(factoryEntryId);
                            if (factory == null) {
                                sender.sendMessage(text("Unknown " + factoryId + " " + factoryId, FAIL_STYLE));
                                return 0;
                            }
                            sender.sendMessage(DisplayUtil.displayBlock(
                                    text("Options for " + factoryId + " " + factoryEntryId),
                                    DisplayUtil.displayReadableOptions(factory.readableOptions())));
                            return 1;
                        })
                ));
    }


    public LiteralArgument get() {
        return (LiteralArgument) new LiteralArgument("view")
                .then(eventArg(eventFactories))
                .then(dataTypeInfoArg())
                .then(factoriesArg());
    }
}
