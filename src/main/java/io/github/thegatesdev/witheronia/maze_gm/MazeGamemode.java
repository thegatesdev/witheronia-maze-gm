package io.github.thegatesdev.witheronia.maze_gm;

import io.github.thegatesdev.eventador.Eventador;
import io.github.thegatesdev.eventador.event.EventManager;
import io.github.thegatesdev.eventador.event.ListenerManager;
import io.github.thegatesdev.stacker.Stacker;
import io.github.thegatesdev.threshold.pluginmodule.ModuleManager;
import io.github.thegatesdev.witheronia.maze_gm.data.MazeReactors;
import io.github.thegatesdev.witheronia.maze_gm.modules.command.MazeCommandModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.generation.maze.MazeGenerationModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.item.MazeItemModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.MazeQuestModule;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;
import java.util.logging.Logger;

public class MazeGamemode extends JavaPlugin {

    private final Logger logger = getLogger();

    private final ListenerManager listenerManager = new ListenerManager(this);
    private final MazeReactors mazeReactors = new MazeReactors(eventManager());

    // -- CONNECTIONS

    private final Stacker stacker = getPlugin(Stacker.class);

    // -- MODULES

    private final ModuleManager<MazeGamemode> modules = new ModuleManager<MazeGamemode>(getLogger()).add(
            new MazeItemModule(this),
            new MazeGenerationModule(this),
            new MazeCommandModule(this),
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
            modules.reloadAll();
            getServer().getOnlinePlayers().parallelStream().forEach(this::reloadPlayer);
            modules.enableAll();
            listenerManager.updateAll();
        } catch (Exception e) {
            logger.warning("Something went wrong while reloading!");
            e.printStackTrace();
        }
    }

    public void reloadPlayer(Player player) {
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

    // -- UTIL

    public NamespacedKey key(String id) {
        return new NamespacedKey(this, id);
    }
}
