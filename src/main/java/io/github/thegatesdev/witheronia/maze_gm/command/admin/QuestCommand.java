package io.github.thegatesdev.witheronia.maze_gm.command.admin;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.thegatesdev.witheronia.maze_gm.quest.QuestData;

public class QuestCommand {
    public static ArgumentTree addEntityQuestArg(QuestData questData) {
        return new LiteralArgument("add")
                .then(new LiteralArgument("entity")
                        .then(new EntitySelectorArgument.OneEntity("quest_entity")
                                .then(new StringArgument("quest").replaceSuggestions(ArgumentSuggestions.strings(questData.getQuestIds()))

                                )));
    }
}
