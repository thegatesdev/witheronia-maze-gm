package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.PlayerResultingCommandExecutor;
import dev.jorel.commandapi.executors.ResultingCommandExecutor;
import io.github.thegatesdev.eventador.core.EventType;
import io.github.thegatesdev.eventador.listener.stat.EventTypeHolder;
import io.github.thegatesdev.eventador.listener.stat.StaticListener;
import io.github.thegatesdev.eventador.util.MappedListeners;
import io.github.thegatesdev.maple.data.DataValue;
import io.github.thegatesdev.maple.exception.ElementException;
import io.github.thegatesdev.maple.read.struct.DataType;
import io.github.thegatesdev.stacker.CustomItem;
import io.github.thegatesdev.stacker.ItemGroup;
import io.github.thegatesdev.threshold.pluginmodule.ModuleManager;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.util.DisplayUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MazeItemModule extends PluginModule<MazeGamemode> {

    private final DataType<DataValue<MazeItemBuilder.ReadItem>> itemDataType = MazeItemBuilder.createDataType(plugin.mazeEvents());

    private final ItemGroup mazeItemGroup = new ItemGroup("maze_items", plugin.key("maze_item"), true);
    private final MappedListeners<ItemStack, String> mazeItemListeners = new MazeItemListener(plugin.listenerManager(), plugin.eventTypes(), mazeItemGroup);

    private final List<MazeItemBuilder.ReadItem> loadedItems = new ArrayList<>();

    public MazeItemModule(ModuleManager<MazeGamemode> moduleManager) {
        super("items", moduleManager);
    }

    // -- MODULE

    @Override
    protected void onInitialize() {
        plugin.EVENT_LOAD_DATAFILE.bind(this::onDataFileLoad);
        plugin.stacker().itemManager().addGroup(mazeItemGroup);
        plugin.witheroniaCommand().add(command());
    }

    @Override
    protected void onUnload() {
        mazeItemListeners.clear();
        mazeItemGroup.clear();
        loadedItems.clear();
    }

    @Override
    protected void onLoad() {
        pollLoaded();
    }

    @Override
    protected void onEnable() {
        mazeItemListeners.enable();
    }

    @Override
    protected void onDisable() {
        mazeItemListeners.disable();
    }

    // -- LOADING

    private void pollLoaded() {
        loadedItems.forEach(this::register);
        logger.info("Successfully loaded " + loadedItems.size() + " items");
        loadedItems.clear();
    }

    private void onDataFileLoad(MazeGamemode.LoadDataFileInfo info) {
        info.data().ifList("maze_items", list -> list.each(el -> {
            if (el.isMap()) try {
                loadedItems.add(itemDataType.read(el).value());
            } catch (ElementException e) {
                plugin.logError(e);
            }
        }));
    }

    // -- ITEMS

    private void register(MazeItemBuilder.ReadItem item) {
        mazeItemGroup.register(item.display());
        for (var listener : item.listeners()) onItemEvent(item.id(), listener);
    }

    public MazeItemModule register(CustomItem customItem) {
        mazeItemGroup.register(customItem);
        return this;
    }

    public <E extends Event> MazeItemModule onItemEvent(String itemId, EventType<E> eventType, StaticListener<E> listener) {
        if (!mazeItemGroup.itemKeys().contains(itemId)) throw new RuntimeException("Cannot listen to non-existent item " + itemId);
        mazeItemListeners.listen(itemId, eventType, listener);
        return this;
    }

    public <E extends Event, L extends StaticListener<E> & EventTypeHolder<E>> MazeItemModule onItemEvent(String itemId, L listener) {
        return onItemEvent(itemId, listener.eventType(), listener);
    }

    // -- COMMAND

    private LiteralArgument command() {
        return (LiteralArgument) new LiteralArgument("item")
                .then(new LiteralArgument("give")
                        .then(new StringArgument("item_id").replaceSuggestions(ArgumentSuggestions.stringCollection(info -> mazeItemGroup.itemKeys()))
                                .executesPlayer((PlayerResultingCommandExecutor) (sender, args) -> handleGiveCommand(args.getUnchecked("item_id"), sender, sender)))
                        .then(new PlayerArgument("player").then(new StringArgument("item_id")
                                .executes((ResultingCommandExecutor) (sender, args) -> handleGiveCommand(args.getUnchecked("item_id"), sender, args.getUnchecked("player")))))
                );
    }

    private int handleGiveCommand(String itemId, CommandSender sender, Player toGive) {
        var itemStack = mazeItemGroup.getStack(itemId);
        if (itemStack == null) {
            sender.sendMessage(Component.text("Could not find item " + itemId + "!", DisplayUtil.FAIL_STYLE));
            return 0;
        }
        var inventory = toGive.getInventory();
        if (inventory.getItemInMainHand().getAmount() == 0) inventory.setItemInMainHand(itemStack);
        else if (!inventory.addItem(itemStack).isEmpty()) {
            sender.sendMessage(Component.text("Inventory is full!", DisplayUtil.FAIL_STYLE));
            return 0;
        }
        return 1;
    }

    // -- GET / SET

    public ItemGroup mazeItemGroup() {
        return mazeItemGroup;
    }
}
