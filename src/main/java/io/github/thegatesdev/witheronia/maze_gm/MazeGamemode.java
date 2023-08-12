package io.github.thegatesdev.witheronia.maze_gm;

import io.github.thegatesdev.maple.data.DataMap;
import io.github.thegatesdev.maple.data.DataValue;
import io.github.thegatesdev.stacker.item.ItemManager;
import io.github.thegatesdev.threshold.event.DataEvent;
import io.github.thegatesdev.threshold.event.listening.BukkitListeners;
import io.github.thegatesdev.threshold.event.listening.Listeners;
import io.github.thegatesdev.witheronia.maze_gm.command.Command;
import io.github.thegatesdev.witheronia.maze_gm.modules.item.ItemModule;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class MazeGamemode extends JavaPlugin {

    // PLUGIN

    private final Logger logger = getLogger();
    private final Server server = getServer();

    // DATA

    private final Config config = new Config(getDataFolder().toPath());

    // GLOBAL

    private final ItemManager itemManager = new ItemManager(this);
    private final Listeners listeners = new BukkitListeners(this);

    // COMMAND

    private final Command command = new Command(this);

    // MODULES

    private final List<PluginModule> modules = List.of(
        new ItemModule(itemManager)
    );

    // -- PLUGIN

    @Override
    public void onLoad() {
        listeners.listen(PlayerJoinEvent.class, event -> reloadPlayer(event.getPlayer()));

        start();
        command.register();
    }

    @Override
    public void onEnable() {
        reload();
    }

    // --

    public void start() {
        logger.info("Starting " + modules.size() + " modules...");

        config.reloadMainConfig();

        for (var module : modules) {
            try {
                module.start(this);
            } catch (Exception e) {
                logger.warning("Error while starting module " + module.name() + ": " + e.getMessage());
            }
        }
    }

    public void reload() {
        logger.info("Reloading " + modules.size() + " modules...");

        config.reloadMainConfig();

        var context = new ReloadContext(
            new DataEvent<>(throwable -> logger.warning("Error while loading content file: " + throwable.getMessage()))
        );

        reloadModules(context);
        passContentFiles(context);

        server.getOnlinePlayers().forEach(this::reloadPlayer);
    }

    private void reloadPlayer(Player player) {
        var contents = player.getInventory().getContents();
        itemManager.update(contents);
        player.getInventory().setContents(contents);
    }

    private void reloadModules(ReloadContext context) {
        // Reload modules
        for (var module : modules) {
            try {
                module.reload(this, context);
            } catch (Exception e) {
                logger.warning("Error while reloading module " + module.name() + ": " + e.getMessage());
            }
        }
    }

    // -- CONTENT FILES

    private void passContentFiles(ReloadContext context) {
        if (context.onContentFileLoad.isEmpty()) return;
        var data = config.mainConfig().orElse(null);
        if (data == null) return;
        var list = data.getList("content_paths", null);
        if (list == null) return;

        logger.info("Started loading content files");
        long start = System.currentTimeMillis();

        var inputList = list.valueList(DataValue::stringValue);
        var pathCache = new HashSet<Path>();
        for (var input : inputList) {
            var inputPath = config.tryResolve(input);
            if (inputPath.isEmpty()) {
                logger.warning("Could not resolve path " + input);
                continue;
            }
            passContentFilesAt(context, pathCache, inputPath.get());
        }

        logger.info("Finished loading content files in " + (System.currentTimeMillis() - start) + "ms");
        logger.info("Loaded a total of " + pathCache.size() + " content files");
    }

    private void passContentFilesAt(ReloadContext context, Set<Path> skipPaths, Path inputPath) {
        try (var str = Config.contentFilesForPath(inputPath)) {
            str.forEach(path -> {
                if (!skipPaths.add(path)) return;
                try {
                    config.loadPath(path).ifMap(context.onContentFileLoad::dispatch,
                        () -> logger.warning("Content file should be a map: " + path));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            logger.warning("Could not access content files at " + inputPath);
        }
    }

    // -- GET

    public ItemManager itemManager() {
        return itemManager;
    }

    public Config config() {
        return config;
    }

    public Command command() {
        return command;
    }

    // -- DEFINITIONS

    public record ReloadContext(DataEvent<DataMap> onContentFileLoad) {
    }

    public interface PluginModule {
        String name();

        default void start(MazeGamemode gamemode) {
        }

        default void reload(MazeGamemode gamemode, ReloadContext context) {
        }
    }
}
