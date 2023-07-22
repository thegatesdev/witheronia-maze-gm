package io.github.thegatesdev.witheronia.maze_gm.command.witheronia.args;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import io.github.thegatesdev.actionable.registry.Registries;
import io.github.thegatesdev.maple.read.struct.DataType;
import io.github.thegatesdev.witheronia.maze_gm.util.DisplayUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.github.thegatesdev.witheronia.maze_gm.util.DisplayUtil.*;
import static net.kyori.adventure.text.Component.text;

public class ViewOptionsCommand {

    private static final Map<String, Component> dataTypeDisplayCache = new HashMap<>();

    public static LiteralArgument create() {
        return (LiteralArgument) new LiteralArgument("view")
            .then(dataTypeInfoArg())
            .then(factoriesArg());
    }

    private static LiteralArgument dataTypeInfoArg() {
        return (LiteralArgument) new LiteralArgument("datatype")
            .executes((sender, args) -> {
                sender.sendMessage(DisplayUtil.displayBlock(text("Available dataTypes"), text(String.join("\n", DataType.Info.keys()), VAR_VAL_STYLE)));
            }).then(new StringArgument("datatype_id").replaceSuggestions(ArgumentSuggestions.strings(DataType.Info.keys()))
                .executes((sender, args) -> {
                    // Cached
                    var toSend = dataTypeDisplayCache.computeIfAbsent((String) args.get("datatype_id"), key -> {
                        final var info = DataType.Info.of(key);
                        if (info == null) {
                            sender.sendMessage(text("Could not find info for datatype " + key + "!", FAIL_STYLE));
                            return null;
                        }
                        return displayDataTypeInfo(key, info);
                    });
                    if (toSend != null) sender.sendMessage(toSend);
                })
            );
    }

    private static Component displayDataTypeInfo(String key, DataType.Info info) {
        var toDisplay = new ArrayList<Component>();

        if (info.description() != null)
            toDisplay.add(text(info.description(), TEXT_STYLE));

        if (info.origin() != null)
            toDisplay.add(text().append(
                text("From: ", EMPHASIS_STYLE),
                text(info.origin(), VAR_VAL_STYLE)).build());

        if (info.representation() != null)
            toDisplay.add(text().append(
                text("Representation: ", EMPHASIS_STYLE),
                text(info.representation(), VAR_VAL_STYLE)).build());

        if (info.possibleValues() != null) {
            var possibleValues = info.possibleValues();
            int maxLen = 10;
            toDisplay.add(text().append(
                text("Possible values: ", EMPHASIS_STYLE),
                text(String.join(", ", Arrays.copyOf(possibleValues, maxLen)) +
                        (possibleValues.length > maxLen ? " and " + (possibleValues.length - maxLen) + " more..." : ""),
                    VAR_VAL_STYLE)).build());
        }

        return DisplayUtil.displayBlock(text("DataType " + key), Component.join(JoinConfiguration.newlines(), toDisplay));
    }

    private static LiteralArgument factoriesArg() {
        return (LiteralArgument) new LiteralArgument("builder").executes((sender, args) -> {
            sender.sendMessage(DisplayUtil.displayBlock(text("Available builder registries"), text(String.join("\n", Registries.keys()), VAR_VAL_STYLE)));
        }).then(new StringArgument("builder_id")
            .replaceSuggestions(ArgumentSuggestions.strings(Registries.keys()))
            .executes((CommandExecutor) (sender, args) ->
                sender.sendMessage(displayFactory(args.getUnchecked("builder_id"))))
            .then(new StringArgument("builder_entry_id")
                .replaceSuggestions(ArgumentSuggestions.stringCollection(info -> Registries.get(info.previousArgs().getUnchecked("builder_id")).keys()))
                .executes((CommandExecutor) (sender, args) ->
                    sender.sendMessage(displayFactoryEntry(args.getUnchecked("builder_id"), args.getUnchecked("builder_entry_id"))))
            ));
    }

    private static Component displayFactory(String factoryId) {
        var factoryRegistry = Registries.get(factoryId);
        if (factoryRegistry == null) return text("Unknown builder registry " + factoryId, FAIL_STYLE);
        return DisplayUtil.displayBlock(text("Available builders"), text(String.join("\n", factoryRegistry.keys()), VAR_VAL_STYLE));
    }

    private static Component displayFactoryEntry(String factoryId, String factoryEntryId) {
        var factoryRegistry = Registries.get(factoryId);
        if (factoryRegistry == null) return text("Unknown builder registry " + factoryId, FAIL_STYLE);
        var builder = factoryRegistry.get(factoryEntryId);
        if (builder == null) return text("Unknown " + factoryId + " " + factoryId, FAIL_STYLE);
        return DisplayUtil.displayBlock(
            text("Options for " + factoryId + " " + factoryEntryId),
            DisplayUtil.displayReadableOptions(builder.readableOptions())
        );
    }
}
