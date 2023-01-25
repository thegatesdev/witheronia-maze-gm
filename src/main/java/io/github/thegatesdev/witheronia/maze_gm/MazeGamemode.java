package io.github.thegatesdev.witheronia.maze_gm;

import io.github.thegatesdev.eventador.event.EventManager;
import io.github.thegatesdev.eventador.event.ListenerManager;
import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.exception.ElementException;
import io.github.thegatesdev.skiller.ItemManager;
import io.github.thegatesdev.skiller.Skiller;
import io.github.thegatesdev.witheronia.maze_gm.command.MazeCommands;
import io.github.thegatesdev.witheronia.maze_gm.generation.maze.MazeGenerator;
import io.github.thegatesdev.witheronia.maze_gm.registry.MazeDataTypes;
import io.github.thegatesdev.witheronia.maze_gm.registry.MazeEvents;
import io.github.thegatesdev.witheronia.maze_gm.registry.MazeItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
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

    private final MazeItems mazeItems = itemManager.addGroup(new MazeItems(this));

    // GENERATION
    private final MazeGenerator basicGenerator = new MazeGenerator().addFeatureGenerators((random, context, x, y, z, filled) -> {
        if (filled) context.setBlockAt(x, y, z, Material.STONE_BRICKS);
    }, (random, context, x, y, z, filled) -> {
        if (y != 1 || filled) return;
        final int distance = context.wallDistance(x, z, 2);
        final Material toPlace = switch (distance) {
            case 1 -> Material.RED_STAINED_GLASS;
            case 2 -> Material.ORANGE_STAINED_GLASS;
            case 3 -> Material.YELLOW_STAINED_GLASS;
            case 4 -> Material.LIME_STAINED_GLASS;
            case 5 -> Material.LIGHT_BLUE_STAINED_GLASS;
            case 6 -> Material.CYAN_STAINED_GLASS;
            case 7 -> Material.BLUE_STAINED_GLASS;
            case 8 -> Material.MAGENTA_STAINED_GLASS;
            case 9 -> Material.PURPLE_STAINED_GLASS;
            case 10 -> Material.PINK_STAINED_GLASS;
            case 11 -> Material.BROWN_STAINED_GLASS;
            case 12 -> Material.GRAY_STAINED_GLASS;
            case 13 -> Material.WHITE_STAINED_GLASS;
            default -> null;
        };
        if (toPlace != null) context.setBlockAt(x, y, z, toPlace);
    });

    // -- PLUGIN

    @Override
    public void onLoad() {
        MazeDataTypes.mapType(mazeEvents);

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

        for (final Player player : getServer().getOnlinePlayers()) {
            final PlayerInventory inventory = player.getInventory();
            inventory.setContents(itemManager.reloadItems(inventory.getContents()));
        }

        // REMAP

        mazeItems.remapEvents(listenerManager);

        // FINISH

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
                    mazeItems.read(element);
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

    public MazeItems getMazeItems() {
        return mazeItems;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public MazeEvents getMazeEvents() {
        return mazeEvents;
    }

    // -- UTIL

    public NamespacedKey key(String id) {
        return new NamespacedKey(this, id);
    }
}
