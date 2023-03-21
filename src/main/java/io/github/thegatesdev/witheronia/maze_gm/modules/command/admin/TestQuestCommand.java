package io.github.thegatesdev.witheronia.maze_gm.modules.command.admin;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.MazeQuestModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.data.PlayerQuestData;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.structs.quest.Quest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Map;

public class TestQuestCommand {

    public static ArgumentTree activateQuestArg(MazeQuestModule questModule) {
        Map<String, Quest<?>> testQuests = questModule.testQuests();
        return new LiteralArgument("activate")
                .then(new StringArgument("questId").replaceSuggestions(ArgumentSuggestions.strings(info -> testQuests.keySet().toArray(String[]::new)))
                        .executesPlayer((sender, args) -> {
                            final String questId = ((String) args[0]);
                            final Quest<?> quest = testQuests.get(questId);
                            if (quest == null) throw CommandAPI.failWithString("Unknown test quest!");
                            PlayerQuestData player = questModule.questData().getOrCreatePlayer(sender.getUniqueId());
                            if (player.shouldActivate(quest)) {
                                player.activate(Quest.activate(quest, sender));
                                sender.sendMessage(Component.text("Activated quest '%s'".formatted(quest.id()), TextColor.color(40, 255, 100)));
                            } else
                                sender.sendMessage(Component.text("Could not activate quest '%s'".formatted(quest.id()), TextColor.color(255, 50, 100)));
                        })
                );
    }
}
