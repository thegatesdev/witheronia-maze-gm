package io.github.thegatesdev.witheronia.maze_gm;

import io.github.thegatesdev.eventador.Eventador;
import io.github.thegatesdev.eventador.event.EventManager;
import io.github.thegatesdev.eventador.event.ListenerManager;
import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.data.DataPrimitive;
import io.github.thegatesdev.stacker.Stacker;
import io.github.thegatesdev.threshold.pluginmodule.ModuleManager;
import io.github.thegatesdev.witheronia.maze_gm.data.MazeReactors;
import io.github.thegatesdev.witheronia.maze_gm.modules.command.MazeCommandModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.generation.maze.MazeGenerationModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.item.MazeItemModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.MazeQuestModule;
import io.github.thegatesdev.witheronia.maze_gm.util.DataFileLoader;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

public class MazeGamemode extends JavaPlugin {

    private final Logger logger = getLogger();

    // DATA

    private final Yaml yaml = new Yaml();
    private final Path dataPath = getDataFolder().toPath();
    private final File configFile = dataPath.resolve("mazegm.yml").toFile();
    private DataMap configurationData;

    private final List<DataFileLoader> dataFileLoaders = new ArrayList<>();

    // GLOBAL

    private final ListenerManager listenerManager = new ListenerManager(this);
    private final MazeReactors mazeReactors = new MazeReactors(eventManager());

    // -- CONNECTIONS

    private final Stacker stacker = getPlugin(Stacker.class);

    // -- MODULES

    private final ModuleManager<MazeGamemode> modules = new ModuleManager<MazeGamemode>(getLogger()).add(
            new MazeCommandModule(this),
            new MazeGenerationModule(this),
            new MazeItemModule(this),
            new MazeQuestModule(this)
    );

    // -- INIT

    {
        final Set<Class<? extends Event>> eventClasses = new Reflections(new ConfigurationBuilder().forPackage("io.papermc.paper.event").setScanners(Scanners.SubTypes)).getSubTypesOf(Event.class);
        logger.info("Found %s event classes.".formatted(eventClasses.size()));
        Eventador.EVENT_MANAGER.addEventClasses(eventClasses);
    }

    // -- PLUGIN

    public void reload() {
        try {
            reloadConfigData();

            modules.reloadAll();
            loadDataFiles();

            getServer().getOnlinePlayers().parallelStream().forEach(this::reloadPlayer);
            modules.enableAll();

            listenerManager.updateAll();

        } catch (Exception e) {
            logger.warning("Something went wrong while reloading!");
            e.printStackTrace();
        }
    }

    private void loadDataFiles() {
        configurationData().ifPresent(data -> data.ifList("data_files", elements -> {
            logger.info("Loading items...");
            elements.iterator(DataPrimitive.class).forEachRemaining(primitive -> {
                if (!primitive.isStringValue()) return;
                final String path = primitive.stringValue();
                File itemFile;
                try {
                    itemFile = dataPath.resolve(path).toFile();
                } catch (InvalidPathException e) {
                    logger.warning("Invalid path '%s'".formatted(path));
                    return;
                }
                final Object loaded;
                try {
                    loaded = yaml.load(new FileInputStream(itemFile));
                } catch (FileNotFoundException e) {
                    logger.warning("Cannot find file " + itemFile.getPath());
                    return;
                }
                DataElement.readOf(loaded).ifMap(fileData -> {
                    logger.info("Loading data file " + itemFile.getName());
                    for (final DataFileLoader loader : dataFileLoaders) loader.onDataFileLoad(fileData);
                });
            });
        }, () -> logger.warning("item_files should be a list of file paths")));
    }

    private void reloadConfigData() {
        Object load;
        try {
            load = yaml.load(new FileInputStream(configFile));
        } catch (FileNotFoundException e) {
            logger.warning("No configuration file present.");
            return;
        }
        final DataElement element = DataElement.readOf(load);
        if (!element.isMap()) {
            logger.warning("Invalid configuration file, should be a mapping.");
            return;
        }
        configurationData = element.asMap();
    }

    public void addDataFileLoaders(DataFileLoader... loaders) {
        Collections.addAll(dataFileLoaders, loaders);
    }

    private void reloadPlayer(Player player) {
        final PlayerInventory inventory = player.getInventory();
        inventory.setContents(stacker.itemManager().reloadItems(inventory.getContents()));
    }

    @Override
    public void onLoad() {
        mazeReactors.load();
    }

    @Override
    public void onEnable() {
        reload();
    }

    @Override
    public void onDisable() {
        modules.disableAll();
    }


    // -- GET / SET

    public MazeReactors mazeReactors() {
        return mazeReactors;
    }

    public ModuleManager<MazeGamemode> modules() {
        return modules;
    }

    public EventManager eventManager() {
        return Eventador.EVENT_MANAGER;
    }

    public ListenerManager listenerManager() {
        return listenerManager;
    }

    public Stacker stacker() {
        return stacker;
    }

    public Optional<DataMap> configurationData() {
        return Optional.ofNullable(configurationData);
    }

    public Path dataPath() {
        return dataPath;
    }

    public Yaml yaml() {
        return yaml;
    }

    // -- UTIL

    public NamespacedKey key(String id) {
        return new NamespacedKey(this, id);
    }
}
