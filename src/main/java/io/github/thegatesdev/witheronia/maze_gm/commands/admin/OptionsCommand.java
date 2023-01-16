package io.github.thegatesdev.witheronia.maze_gm.commands.admin;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.thegatesdev.eventador.factory.ReactorFactory;
import io.github.thegatesdev.eventador.registry.ReactorFactories;
import io.github.thegatesdev.witheronia.maze_gm.util.DisplayUtil;
import net.kyori.adventure.text.Component;

public class OptionsCommand {
    public static ArgumentTree eventOptionsArg(ReactorFactories reactorFactories) {
        return new LiteralArgument("event").then(
                new StringArgument("eventId").replaceSuggestions(ArgumentSuggestions.strings(reactorFactories.keySet()))
                        .executes((sender, args) -> {
                            final ReactorFactory<?> factory = reactorFactories.getFactory((String) args[0]);
                            sender.sendMessage(DisplayUtil.displayList(Component.text("Event " + args[0]), DisplayUtil.displayReadableData(factory.getReadableData())));
                        })
        );
    }

    public static ArgumentTree dataTypeOptionsArg() {
        return new LiteralArgument("datatype").then(
                new StringArgument("datatypeId")
        );
    }
}
