package io.github.thegatesdev.witheronia.maze_gm;

import io.github.thegatesdev.eventador.core.EventTypes;
import io.github.thegatesdev.eventador.listener.ListenerManager;
import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.data.DataPrimitive;
import io.github.thegatesdev.stacker.Stacker;
import io.github.thegatesdev.threshold.PluginEvent;
import io.github.thegatesdev.threshold.pluginmodule.ModuleManager;
import io.github.thegatesdev.witheronia.maze_gm.data.MazeEvents;
import io.github.thegatesdev.witheronia.maze_gm.modules.command.MazeCommandModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.generation.maze.MazeGenerationModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.item.MazeItemModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.MazeQuestModule;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class MazeGamemode extends JavaPlugin {

    public final PluginEvent<LoadDataFileInfo> EVENT_LOAD_DATAFILE = new PluginEvent<>();

    public record LoadDataFileInfo(DataMap data, String name, Path path) {
    }

    private final Logger logger = getLogger();

    private ExecutorService executorService;

    // DATA

    private final Yaml yaml = new Yaml();
    private final Path dataPath = getDataFolder().toPath();
    private final Path configFile = dataPath.resolve("MazeGM.yml");

    private DataMap configurationData = getConfigData();
    private final DataMap staticSettings = configurationData == null ? new DataMap() : configurationData.getMap("settings", new DataMap());

    // GLOBAL

    private final EventTypes eventTypes = new EventTypes("io.papermc.paper.event", "org.bukkit.event", "org.spigotmc.event");
    private final ListenerManager listenerManager = new ListenerManager(this, eventTypes);
    private final MazeEvents mazeEvents = new MazeEvents(eventTypes);

    // CONNECTIONS

    private final Stacker stacker = getPlugin(Stacker.class);

    // MODULES

    private final ModuleManager<MazeGamemode> modules = new ModuleManager<MazeGamemode>(getLogger()).add(
            new MazeCommandModule(this),
            new MazeGenerationModule(this),
            new MazeItemModule(this),
            new MazeQuestModule(this)
    );

    // PLUGIN

    private final List<Throwable> instanceErrors = new ArrayList<>();

    // -- PLUGIN

    {
        listenerManager.listen(PlayerJoinEvent.class, (event, type) -> reloadPlayer(event.getPlayer()));
    }

    public void reload() {
        instanceErrors.clear();
        listenerManager.handleEvents(false);
        try {
            configurationData = getConfigData();

            modules.unload();
            loadDataFiles();
            modules.load();

            getServer().getOnlinePlayers().forEach(this::reloadPlayer);
        } catch (Exception e) {
            logger.warning("Something went wrong while reloading!");
            e.printStackTrace();
            logger.warning("Modules will not be enabled.");
            return;
        }
        modules.enable();

        listenerManager.handleEvents(true);
    }

    private void loadDataFiles() {
        configurationData().ifPresent(data -> data.ifList("data_files", elements -> {
            logger.info("Loading items...");
            elements.iterator(DataPrimitive.class).forEachRemaining(primitive -> {
                if (!primitive.isStringValue()) return;
                final String path = primitive.stringValue();
                Path itemPath;
                try {
                    itemPath = dataPath.resolve(path);
                } catch (InvalidPathException e) {
                    logger.warning("Invalid path '%s'".formatted(path));
                    return;
                }
                final Object loaded;
                try {
                    loaded = yaml.load(Files.newInputStream(itemPath));
                } catch (IOException e) {
                    logger.warning("Failed to data file " + itemPath.getFileName() + "; " + e.getMessage());
                    return;
                }
                DataElement.readOf(loaded).ifMap(fileData -> {
                    logger.info("Loading data file " + itemPath.getFileName());
                    EVENT_LOAD_DATAFILE.dispatchAsync(new LoadDataFileInfo(fileData, itemPath.getFileName().toString(), itemPath), executorService);
                }, () -> logger.warning("Failed to load file " + itemPath.getFileName() + "; not a map"));
            });
        }, () -> logger.warning("item_files should be a list of file paths")));
    }

    private DataMap getConfigData() {
        final Object load;
        try {
            load = yaml.load(Files.newInputStream(configFile));
        } catch (IOException e) {
            logger.warning("Could not data config file.");
            return null;
        }
        final DataElement element = DataElement.readOf(load);
        if (!element.isMap()) {
            logger.warning("Invalid configuration file, should be a map.");
            return null;
        }
        return element.asMap();
    }

    private void reloadPlayer(Player player) {
        final PlayerInventory inventory = player.getInventory();
        inventory.setContents(stacker.itemManager().reloadItems(inventory.getContents()));
    }

    @Override
    public void onEnable() {
        executorService = Executors.newFixedThreadPool(3);
        reload();
    }

    @Override
    public void onDisable() {
        modules.disable();
        executorService.shutdown();
    }


    public void logError(Throwable... error) {
        instanceErrors.addAll(Arrays.asList(error));
    }

    public void displayErrors(boolean trace) {
        if (trace) for (final Throwable err : instanceErrors) err.printStackTrace();
        else for (final Throwable err : instanceErrors) logger.warning(err.getMessage());
    }


    // -- GET / SET

    public MazeEvents mazeReactors() {
        return mazeEvents;
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

    public ExecutorService executorService() {
        return executorService;
    }

    // -- UTIL

    public NamespacedKey key(String id) {
        return new NamespacedKey(this, id);
    }
}
