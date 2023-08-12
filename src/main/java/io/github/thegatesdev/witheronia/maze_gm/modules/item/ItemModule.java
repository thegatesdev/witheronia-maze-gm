package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.stacker.item.ItemGroup;
import io.github.thegatesdev.stacker.item.ItemManager;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.util.DisplayUtil;
import org.bukkit.entity.Player;

import java.util.Collections;

public class ItemModule implements MazeGamemode.PluginModule {

    private final ItemGroup itemGroup;

    public ItemModule(ItemManager itemManager) {
        itemGroup = itemManager.addGroup("maze_items");
    }

    @Override
    public void start(MazeGamemode gamemode) {
        gamemode.command().then(command(gamemode.itemManager()));
    }

    @Override
    public void reload(MazeGamemode gamemode, MazeGamemode.ReloadContext context) {
        context.onContentFileLoad().bind(this::onContentFileLoad);
    }

    // --

    private void onContentFileLoad(DataMap data) {
        data.ifList("scourge_items", list -> list.eachMap(itemMap -> {
            var item = ItemBuilder.build(itemMap);
            itemGroup.overwrite(item.key(), item.settings());
        }));
    }

    // --

    private static LiteralArgument command(ItemManager itemManager) {
        return (LiteralArgument) new LiteralArgument("item")
            .then(commandGive(itemManager));
    }

    private static LiteralArgument commandGive(ItemManager itemManager) {
        return (LiteralArgument) new LiteralArgument("give")
            .then(new PlayerArgument("player")
                .then(new StringArgument("group_id")
                    .replaceSuggestions(ArgumentSuggestions.stringCollection(info -> itemManager.groupIds()))
                    .then(new StringArgument("item_id")
                        .replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
                            var group = itemManager.getGroup(info.previousArgs().getUnchecked("group_id"));
                            return group != null ? group.itemIds() : Collections.emptyList();
                        }))
                        .executes((sender, args) -> {
                            String groupId = args.getUnchecked("group_id");
                            var group = itemManager.getGroup(groupId);
                            if (group == null)
                                throw CommandAPIBukkit.failWithAdventureComponent(DisplayUtil.fail("Item group does not exist: " + groupId));
                            String itemId = args.getUnchecked("item_id");
                            var item = group.getItem(itemId);
                            if (item == null)
                                throw CommandAPIBukkit.failWithAdventureComponent(DisplayUtil.fail("Item does not exist in this group: " + itemId));
                            args.<Player>getOptionalUnchecked("player").ifPresent(player -> player.getInventory().addItem(item.build()));
                        })
                    )));
    }

    // -- GET / SET

    @Override
    public String name() {
        return "Item";
    }
}
