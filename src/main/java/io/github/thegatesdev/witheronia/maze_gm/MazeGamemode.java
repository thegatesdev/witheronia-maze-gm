package io.github.thegatesdev.witheronia.maze_gm;

import io.github.thegatesdev.eventador.core.EventTypes;
import io.github.thegatesdev.eventador.listener.ListenerManager;
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
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class MazeGamemode extends JavaPlugin {

    private final Logger logger = getLogger();

    // DATA

    private final Yaml yaml = new Yaml();
    private final Path dataPath = getDataFolder().toPath();
    private final File configFile = dataPath.resolve("mazegm.yml").toFile();
    private DataMap configurationData;

    private final List<Consumer<DataMap>> dataFileLoaders = new ArrayList<>();

    // GLOBAL

    private final EventTypes eventTypes = new EventTypes("io.papermc.paper.event", "org.bukkit.event", "org.spigotmc.event");
    private final ListenerManager listenerManager = new ListenerManager(this, eventTypes);
    private final MazeReactors mazeReactors = new MazeReactors(eventTypes);

    // -- CONNECTIONS

    private final Stacker stacker = getPlugin(Stacker.class);

    // -- MODULES

    private final ModuleManager<MazeGamemode> modules = new ModuleManager<MazeGamemode>(getLogger()).add(
            new MazeCommandModule(this),
            new MazeGenerationModule(this),
            new MazeItemModule(this),
            new MazeQuestModule(this)
    );

    // -- PLUGIN

    public void reload() {
        try {
            listenerManager.handleEvents(false);

            reloadConfigData();

            modules.reloadAll();
            loadDataFiles();

            getServer().getOnlinePlayers().parallelStream().forEach(this::reloadPlayer);
            modules.enableAll();

            listenerManager.handleEvents(true);

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
                    for (final Consumer<DataMap> loader : dataFileLoaders) loader.accept(fileData);
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

    @SafeVarargs
    public final void onDataFileLoad(Consumer<DataMap>... loaders) {
        Collections.addAll(dataFileLoaders, loaders);
    }

    private void reloadPlayer(Player player) {
        final PlayerInventory inventory = player.getInventory();
        inventory.setContents(stacker.itemManager().reloadItems(inventory.getContents()));
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

    public EventTypes eventTypes() {
        return eventTypes;
    }

    public ModuleManager<MazeGamemode> modules() {
        return modules;
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
