package io.github.thegatesdev.witheronia.maze_gm.modules.command.admin;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import io.github.thegatesdev.actionable.Factories;
import io.github.thegatesdev.mapletree.data.ReadableOptionsHolder;
import io.github.thegatesdev.mapletree.registry.DataTypeInfo;
import io.github.thegatesdev.witheronia.maze_gm.core.Cached;
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

public class ViewOptionsCommand implements Cached {

    private final LiteralArgument viewOptionsCommand = (LiteralArgument) new LiteralArgument("view")
            .then(dataTypeInfoArg())
            .then(factoriesArg());

    private final Map<String, Component> dataTypeDisplayCache = new HashMap<>();

    private LiteralArgument dataTypeInfoArg() {
        return (LiteralArgument) new LiteralArgument("datatype")
                .executes((sender, args) -> {
                    sender.sendMessage(DisplayUtil.displayBlock(text("Available dataTypes"), text(String.join("\n", DataTypeInfo.keys()), VAR_VAL_STYLE)));
                }).then(new StringArgument("datatype_id").replaceSuggestions(ArgumentSuggestions.strings(DataTypeInfo.keys()))
                        .executes((sender, args) -> {
                            // Cached
                            var toSend = dataTypeDisplayCache.computeIfAbsent((String) args.get("datatype_id"), key -> {
                                final DataTypeInfo<?, ?> info = DataTypeInfo.get(key);
                                if (info == null) {
                                    sender.sendMessage(text("Could not find info for datatype " + key + "!", FAIL_STYLE));
                                    return null;
                                }
                                return displayDataTypeInfo(info);
                            });
                            if (toSend != null) sender.sendMessage(toSend);
                        })
                );
    }

    private Component displayDataTypeInfo(DataTypeInfo<?, ?> info) {
        var key = info.dataType().id();
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
    }

    private LiteralArgument factoriesArg() {
        return (LiteralArgument) new LiteralArgument("factory").executes((sender, args) -> {
            sender.sendMessage(DisplayUtil.displayBlock(text("Available factory registries"), text(String.join("\n", Factories.keys()), VAR_VAL_STYLE)));
        }).then(new StringArgument("factory_id")
                .replaceSuggestions(ArgumentSuggestions.strings(Factories.keys()))
                .executes((CommandExecutor) (sender, args) ->
                        sender.sendMessage(displayFactory(args.getUnchecked("factory_id"))))
                .then(new StringArgument("factory_entry_id")
                        .replaceSuggestions(ArgumentSuggestions.stringCollection(info -> Factories.get(info.previousArgs().getUnchecked("factory_id")).keys()))
                        .executes((CommandExecutor) (sender, args) ->
                                sender.sendMessage(displayFactoryEntry(args.getUnchecked("factory_id"), args.getUnchecked("factory_entry_id"))))
                ));
    }

    private Component displayFactory(String factoryId) {
        var factoryRegistry = Factories.get(factoryId);
        if (factoryRegistry == null) return text("Unknown factory registry " + factoryId, FAIL_STYLE);
        return DisplayUtil.displayBlock(text("Available factories"), text(String.join("\n", factoryRegistry.keys()), VAR_VAL_STYLE));
    }

    private Component displayFactoryEntry(String factoryId, String factoryEntryId) {
        var factoryRegistry = Factories.get(factoryId);
        if (factoryRegistry == null) return text("Unknown factory registry " + factoryId, FAIL_STYLE);
        ReadableOptionsHolder optionsHolder = factoryRegistry.get(factoryEntryId);
        if (optionsHolder == null) return text("Unknown " + factoryId + " " + factoryId, FAIL_STYLE);
        return DisplayUtil.displayBlock(
                text("Options for " + factoryId + " " + factoryEntryId),
                DisplayUtil.displayReadableOptions(optionsHolder.readableOptions())
        );
    }


    public LiteralArgument get() {
        return viewOptionsCommand;
    }

    @Override
    public void clearCache() {
        dataTypeDisplayCache.clear();
    }
}
