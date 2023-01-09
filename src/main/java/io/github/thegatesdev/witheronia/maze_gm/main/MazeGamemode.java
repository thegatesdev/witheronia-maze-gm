package io.github.thegatesdev.witheronia.maze_gm.main;

import io.github.thegatesdev.eventador.event.EventManager;
import io.github.thegatesdev.eventador.event.ListenerManager;
import io.github.thegatesdev.eventador.event.MappedReactors;
import io.github.thegatesdev.eventador.factory.ReactorFactory;
import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.exception.ElementException;
import io.github.thegatesdev.mapletree.data.ExpandableType;
import io.github.thegatesdev.mapletree.data.Readable;
import io.github.thegatesdev.mapletree.data.ReadableData;
import io.github.thegatesdev.skiller.*;
import io.github.thegatesdev.witheronia.maze_gm.commands.MazeCommands;
import io.github.thegatesdev.witheronia.maze_gm.generation.maze.MazeGenerator;
import io.github.thegatesdev.witheronia.maze_gm.registry.MazeDataTypes;
import io.github.thegatesdev.witheronia.maze_gm.registry.MazeEvents;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MazeGamemode extends JavaPlugin {

    // CONNECTIONS

    private final Skiller skiller = getPlugin(Skiller.class);
    private final ItemManager itemManager = skiller.getItemManager();

    // DATA

    private final Yaml yaml = new Yaml();

    private final File configFile = getDataFolder().toPath().resolve("config.yml").toFile();
    private DataMap configData;

    // PLUGIN

    private final Logger logger = getLogger();

    private final MazeCommands commands = new MazeCommands(this);

    // EVENT

    private final EventManager eventManager = new EventManager(getClassLoader()); // Handle event classes.

    {
        int i = eventManager.addEventClasses("io.papermc.paper.event");
        logger.log(Level.INFO, "Reflections found %s event classes".formatted(i));
    }

    private final MazeEvents mazeEvents = new MazeEvents(eventManager); // Options for reading events.
    private final ListenerManager listenerManager = new ListenerManager(this); // Run listeners on certain events.

    // ITEM

    private final ItemGroup mazeItems = itemManager.addGroup(new ItemGroup("maze_items", key("maze_item"))); // Maze items
    private final MappedReactors<ItemStack, String> mazeItemReactors = new MappedReactors<>(eventManager, mazeEvents.itemStackEvents, mazeItems::itemId); // Maze item events

    //<editor-fold desc="Maze item type">
    public final ExpandableType<CustomItem> mazeItemType = new ExpandableType<>(CustomItem.class, new ReadableData()
            .add("material", Readable.enumeration(Material.class))
            .add("id", Readable.primitive(String.class)), data -> {
        final Material material = data.get("material", Material.class);
        return new CustomItem(data.getString("id"), material, new MetaBuilder(material));
    }).expand("reactors", mazeEvents.listType(), (reactors, customItem) -> {
                for (final ReactorFactory<?>.ReadReactor reactor : reactors) {
                    mazeItemReactors.addReactor(customItem.id(), reactor);
                }
            }).expand("name", MazeDataTypes.COLORED_STRING, (component, customItem) -> customItem.metaBuilder().name(component))
            .expand("lore", MazeDataTypes.COLORED_STRING.listType(), (components, customItem) -> customItem.metaBuilder().addLore(components));
    //</editor-fold>

    // GENERATION

    private final MazeGenerator basicGenerator = new MazeGenerator().addFeatureGenerators((random, context, x, y, z, filled) -> {
        if (filled) context.setBlockAt(x, y, z, Material.STONE);
    });

    // -- PLUGIN

    @Override
    public void onLoad() {
        commands.create();
    }

    @Override
    public void onEnable() {
        commands.register();
        reload();
    }

    public void reload() {
        logger.info("Reloading...");
        // UNLOAD
        listenerManager.cancelAllEvents(true);
        listenerManager.handleEvents(false);
        mazeItems.clear();
        mazeItemReactors.clear();

        // LOAD
        reloadConfig();

        if (configData == null) {
            logger.info("Not loading any files, no config data.");
        } else {
            configData.ifList("data_files", filesList -> {
                final Path dataPath = getDataFolder().toPath();
                for (final String filePath : filesList.primitiveList(String.class)) {
                    loadFile(dataPath.resolve(filePath).toFile());
                }
            }, () -> logger.info("Not loading any files, no data files supplied."));
        }

        listenerManager.remap(mazeItemReactors, mazeItemReactors.listenedEvents());

        for (final Player player : getServer().getOnlinePlayers()) {
            final PlayerInventory inventory = player.getInventory();
            inventory.setContents(itemManager.reloadItems(inventory.getContents()));
        }
        listenerManager.handleEvents(true);
        listenerManager.cancelAllEvents(false);
        logger.info("Reloaded.");
    }

    public void reloadConfig() {
        final DataElement config = loadYaml(configFile);
        configData = null;
        if (config == null) logger.info("Config file was not found.");
        else if (!config.isMap()) logger.warning("Config file is not a map structure.");
        else configData = config.asMap();
    }

    private void loadFile(File file) {
        final DataElement fileData = loadYaml(file);
        if (fileData == null) {
            logger.warning("Could not find file '%s'".formatted(file.getPath()));
            return;
        }
        fileData.asMap().ifList("maze_items", list -> {
            for (final DataElement element : list) {
                try {
                    final CustomItem read = mazeItemType.read(element);
                    mazeItems.register(read);
                } catch (ElementException e) {
                    logger.warning("Failed to read an item: " + e.getMessage());
                }
            }
        });
    }

    // DATA

    @Nullable
    private DataElement loadYaml(File file) {
        Object load;
        try {
            load = yaml.load(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            return null;
        }
        return DataElement.readOf(load);
    }

    // -- GET/SET

    public MazeGenerator getBasicGenerator() {
        return basicGenerator;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    // -- UTIL

    public NamespacedKey key(String id) {
        return new NamespacedKey(this, id);
    }
}
