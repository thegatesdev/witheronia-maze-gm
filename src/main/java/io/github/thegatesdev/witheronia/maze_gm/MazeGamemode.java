package io.github.thegatesdev.witheronia.maze_gm;

import io.github.thegatesdev.actionable.Factories;
import io.github.thegatesdev.eventador.Eventador;
import io.github.thegatesdev.eventador.core.EventTypes;
import io.github.thegatesdev.eventador.listener.ListenerManager;
import io.github.thegatesdev.maple.data.DataElement;
import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.stacker.Stacker;
import io.github.thegatesdev.threshold.PluginEvent;
import io.github.thegatesdev.threshold.pluginmodule.ModuleManager;
import io.github.thegatesdev.witheronia.maze_gm.command.witheronia.WitheroniaCommand;
import io.github.thegatesdev.witheronia.maze_gm.core.Cached;
import io.github.thegatesdev.witheronia.maze_gm.data.MazeEvents;
import io.github.thegatesdev.witheronia.maze_gm.modules.item.MazeItemModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.MazeQuestModule;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class MazeGamemode extends JavaPlugin {

    public final PluginEvent<LoadDataFileInfo> EVENT_LOAD_DATAFILE = new PluginEvent<>(this::logError);

    public record LoadDataFileInfo(DataMap data, String name, Path path) {
    }

    private final Logger logger = getLogger();

    private ExecutorService executorService;

    // DATA

    private final Yaml yaml = new Yaml();
    private final Path dataPath = getDataFolder().toPath();
    private final Path configFile = dataPath.resolve("config.yml");

    private DataMap configurationData;
    private DataMap settings;

    // GLOBAL

    private final EventTypes eventTypes = Eventador.eventTypes();
    private final ListenerManager listenerManager = new ListenerManager(this, eventTypes);
    private final MazeEvents mazeEvents = Factories.add(new MazeEvents(eventTypes));

    // CONNECTIONS

    private final Stacker stacker = getPlugin(Stacker.class);

    // MODULES

    private final ModuleManager<MazeGamemode> modules = new ModuleManager<>(this).add(
            MazeItemModule::new, MazeQuestModule::new
    );

    // COMMANDS

    private final WitheroniaCommand witheroniaCommand = new WitheroniaCommand(this);

    // PLUGIN

    private final List<Exception> instanceErrors = new ArrayList<>();
    private final List<WeakReference<Cached>> cachedComponents = new ArrayList<>();

    // -- PLUGIN

    @Override
    public void onLoad() {
        modules.initialize();
        witheroniaCommand.register();
        listenerManager.listen(PlayerJoinEvent.class, (event, type) -> reloadPlayer(event.getPlayer()));
    }

    @Override
    public void onEnable() {
        //executorService = Executors.newFixedThreadPool(3);
        reload();
    }

    @Override
    public void onDisable() {
        modules.disable();
        if (executorService != null) executorService.shutdown();
    }

    // -- ERRORS

    public void logError(Collection<Exception> errors) {
        instanceErrors.addAll(errors);
    }

    public void logError(Exception error) {
        instanceErrors.add(error);
    }

    public void displayErrors(boolean trace) {
        if (!instanceErrors.isEmpty()) logger.warning("There are active errors...");
        if (trace) for (final Exception err : instanceErrors) err.printStackTrace();
        else for (final Exception err : instanceErrors) logger.warning(err.getMessage());
    }

    public void clearErrors() {
        instanceErrors.clear();
    }

    // -- LOADING

    public void reload() {
        listenerManager.handleEvents(false);
        try {
            configurationData = getConfigData();
            settings = configurationData == null ? new DataMap() : configurationData.getMap("settings", new DataMap());

            // Unload
            modules.unload();
            clearCache();

            // Load
            loadDataFiles();
            modules.load();

            getServer().getOnlinePlayers().forEach(this::reloadPlayer);
        } catch (Exception e) {
            logger.warning("Something unexpected happened while reloading:");
            e.printStackTrace();
            logger.warning("Modules will not be enabled...");
            return;
        }
        displayErrors(settings.getBoolean("error_stacktrace", false));

        modules.enable();

        listenerManager.handleEvents(true);
    }

    private void clearCache() {
        for (var iterator = cachedComponents.iterator(); iterator.hasNext(); ) {
            var cachedComponent = iterator.next().get();
            if (cachedComponent == null) iterator.remove();
            else cachedComponent.clearCache();
        }
    }

    private void loadDataFiles() {
        configurationData().ifPresent(data -> data.ifList("data_files", elements -> elements.forEach(el -> {
            if (!el.isPrimitive() || !el.asPrimitive().isStringValue()) {
                logger.warning("Invalid data file entry; " + el);
                return;
            }
            final String path = el.asPrimitive().stringValue();
            Path itemPath;
            try {
                itemPath = dataPath.resolve(path);
            } catch (InvalidPathException e) {
                logger.warning("Invalid path '%s'".formatted(path));
                return;
            }
            final String fileName = itemPath.getFileName().toString();
            final Object loaded;
            try {
                loaded = yaml.load(Files.newInputStream(itemPath));
            } catch (IOException e) {
                logger.warning("Failed to data file " + fileName + "; " + e.getMessage());
                return;
            }
            DataElement.readOf(loaded).ifMap(fileData -> {
                logger.info("Loading data file " + fileName);
                EVENT_LOAD_DATAFILE.dispatch(new LoadDataFileInfo(fileData, fileName, itemPath));
            }, () -> logger.warning("Failed to load file " + itemPath.getFileName() + "; not a map"));
        }), () -> logger.warning("item_files should be a list of file paths")));
    }

    private DataMap getConfigData() {
        final Object load;
        try {
            load = yaml.load(Files.newInputStream(configFile));
        } catch (IOException e) {
            logger.warning("Could not load data config file.");
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

    // -- GET / SET

    public MazeEvents mazeEvents() {
        return mazeEvents;
    }

    public EventTypes eventTypes() {
        return eventTypes;
    }

    public ModuleManager<MazeGamemode> modules() {
        return modules;
    }

    public DataMap settings() {
        return settings;
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

    public <T extends Cached> T registerCached(T cachedComponent) {
        cachedComponents.add(new WeakReference<>(cachedComponent));
        return cachedComponent;
    }
}
