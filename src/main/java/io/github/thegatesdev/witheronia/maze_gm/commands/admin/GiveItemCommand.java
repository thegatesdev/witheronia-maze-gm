package io.github.thegatesdev.witheronia.maze_gm.commands.admin;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.thegatesdev.skiller.ItemGroup;
import org.bukkit.inventory.ItemStack;

public class GiveItemCommand {
    public static ArgumentTree giveFromGroupArg(ItemGroup itemGroup) {
        return new LiteralArgument(itemGroup.id()).then(new StringArgument("itemKey")
                .replaceSuggestions(ArgumentSuggestions.strings(info -> itemGroup.itemKeys()))
                .executesPlayer((player, args) -> {
                    final ItemStack stack = itemGroup.getStack((String) args[0]);
                    if (stack == null) throw CommandAPI.failWithString("Could not find item '%s'".formatted(args[0]));
                    player.getInventory().addItem(stack);
                }));
    }
}
