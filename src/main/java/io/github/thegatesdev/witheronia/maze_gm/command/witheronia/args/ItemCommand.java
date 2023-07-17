package io.github.thegatesdev.witheronia.maze_gm.command.witheronia.args;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.thegatesdev.stacker.item.ItemManager;
import io.github.thegatesdev.witheronia.maze_gm.util.DisplayUtil;
import org.bukkit.entity.Player;

public class ItemCommand {
    public static LiteralArgument create(ItemManager itemManager) {
        return (LiteralArgument) new LiteralArgument("item")
                .then(giveArg(itemManager));
    }

    private static LiteralArgument giveArg(ItemManager itemManager) {
        return (LiteralArgument) new LiteralArgument("give")
                .then(new PlayerArgument("player")
                        .then(new StringArgument("groupId")
                                .replaceSuggestions(ArgumentSuggestions.strings(itemManager.groupIds()))
                                .then(new StringArgument("itemId")
                                        .replaceSuggestions(ArgumentSuggestions.stringCollection(info ->
                                                itemManager.getGroup(info.previousArgs().getUnchecked("groupId")).itemIds()))
                                        .executes((sender, args) -> {
                                            String groupId = args.getUnchecked("groupId");
                                            var group = itemManager.getGroup(groupId);
                                            if (group == null) {
                                                sender.sendMessage(DisplayUtil.fail("Could not find item group: " + groupId));
                                                return 0;
                                            }
                                            String itemId = args.getUnchecked("itemId");
                                            var item = group.getItem(itemId);
                                            if (item == null) {
                                                sender.sendMessage(DisplayUtil.fail("Could not find item: " + groupId + ":" + itemId));
                                                return 0;
                                            }
                                            Player player = args.getUnchecked("player");
                                            if (player == null) return 0;
                                            player.getInventory().addItem(item.build());
                                            return 1;
                                        })
                                )));
    }
}
