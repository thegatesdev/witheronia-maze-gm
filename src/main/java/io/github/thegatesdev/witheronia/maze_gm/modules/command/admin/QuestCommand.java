package io.github.thegatesdev.witheronia.maze_gm.modules.command.admin;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.MazeQuestModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.data.PlayerQuestData;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.Quest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class QuestCommand {

    public static ArgumentTree activateQuestArg(MazeQuestModule questModule) {
        return new LiteralArgument("activate")
                .then(new StringArgument("questId").replaceSuggestions(ArgumentSuggestions.strings(questModule.questData().questKeys()))
                        .executesPlayer((sender, args) -> {
                            final String questId = ((String) args[0]);
                            final Quest quest = questModule.questData().getQuest(questId);
                            if (quest == null) throw CommandAPI.failWithString("Unknown quest!");
                            final PlayerQuestData playerData = questModule.questData().getOrCreatePlayer(sender.getUniqueId());
                            if (playerData.canActivate(quest)) {
                                sender.sendMessage(Component.text("Activating quest '%s'".formatted(quest.id()), TextColor.color(40, 255, 100)));
                                playerData.activate(quest, sender);
                            } else
                                sender.sendMessage(Component.text("Could not activate quest '%s'".formatted(quest.id()), TextColor.color(255, 50, 100)));
                        })
                );
    }
}
