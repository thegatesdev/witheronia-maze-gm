package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.thegatesdev.actionable.registry.Registries;
import io.github.thegatesdev.maple.data.DataList;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.exception.ElementException;
import io.github.thegatesdev.stacker.item.ItemGroup;
import io.github.thegatesdev.stacker.item.ItemManager;
import io.github.thegatesdev.stacker.item.ItemSettings;
import io.github.thegatesdev.threshold.Threshold;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.util.DisplayUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.Collections;

public class ItemModule implements MazeGamemode.PluginModule {

    private ItemGroup itemGroup;
    private ItemEvents itemEvents;

    @Override
    public void start(MazeGamemode gamemode) {
        itemGroup = gamemode.itemManager().addGroup("maze_items");
        itemEvents = new ItemEvents(gamemode.listeners(), gamemode.itemManager());

        gamemode.command().then(command(gamemode.itemManager()));
    }

    @Override
    public void reload(MazeGamemode gamemode, MazeGamemode.ReloadContext context) {
        context.onContentFileLoad().bind(this::onContentFileLoad);
    }

    // --

    private void onContentFileLoad(DataMap data) {
        data.ifList("scourge_items", list -> list.eachMap(this::loadItem));
    }

    private void loadItem(DataMap itemMap) {
        var itemKey = itemMap.getString("key");
        var settings = buildItemSettings(itemMap);
        var reactors = itemMap.getList("reactors", new DataList()).map(element -> {
            var reactor = Registries.REACTORS.build(element.requireOf(DataMap.class));
            if (!itemEvents.has(reactor.eventType())) throw new ElementException(element, "This event is not an item event: " + reactor.eventType().getSimpleName());
            return reactor;
        });

        itemGroup.overwrite(itemKey, settings);

        itemEvents.clearListeners(itemKey);
        reactors.forEach(reactor -> itemEvents.setListener(itemKey, reactor));
    }

    private ItemSettings buildItemSettings(DataMap data) throws ElementException {
        var builder = new ItemSettings(Threshold.enumGetThrow(Material.class, data.getString("material")));

        data.ifValue("name", value ->
            builder.name(MiniMessage.miniMessage().deserialize(value.stringValue())));
        data.ifList("lore", list ->
            builder.lore(list.mapValues(value -> MiniMessage.miniMessage().deserialize(value.stringValue()))));
        data.ifList("flags", list ->
            builder.flag(list.mapValues(value -> Threshold.enumGetThrow(ItemFlag.class, value.stringValue()))));

        return builder;
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
                            var item = itemManager.buildItem(group, itemId);
                            if (item == null)
                                throw CommandAPIBukkit.failWithAdventureComponent(DisplayUtil.fail("Item does not exist in this group: " + itemId));
                            args.<Player>getOptionalUnchecked("player").ifPresent(player -> player.getInventory().addItem(item));
                        })
                    )));
    }

    // -- GET / SET

    public ItemGroup items() {
        return itemGroup;
    }

    @Override
    public String name() {
        return "Item";
    }
}
