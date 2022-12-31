package io.github.thegatesdev.witheronia.maze_gm.main;

import io.github.thegatesdev.eventador.event.EventManager;
import io.github.thegatesdev.skiller.ItemManager;
import io.github.thegatesdev.skiller.Skiller;
import io.github.thegatesdev.witheronia.maze_gm.generation.maze.MazeGenerator;
import io.github.thegatesdev.witheronia.maze_gm.items.MazeItems;
import io.github.thegatesdev.witheronia.maze_gm.registry.MazeReactors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class MazeGamemode extends JavaPlugin {

    // EVENT
    private final EventManager eventManager = new EventManager(getClassLoader());
    private final MazeReactors mazeReactors = new MazeReactors(eventManager);
    // ITEM
    private final MazeItems mazeItems = new MazeItems(key("maze_item"), mazeReactors);
    // GENERATION
    private final MazeGenerator basicGenerator = new MazeGenerator().addFeatureGenerators((random, context, x, y, z, filled) -> {
        if (filled) context.setBlockAt(x, y, z, Material.STONE);
    });
    // CONNECTIONS
    private final Skiller skiller = getPlugin(Skiller.class);
    private final ItemManager itemManager = skiller.getItemManager();

    // -- PLUGIN

    @Override
    public void onLoad() {
        itemManager.addRegistry(mazeItems);
    }

    // -- GET/SET

    public MazeItems getMazeItems() {
        return mazeItems;
    }

    public MazeGenerator getBasicGenerator() {
        return basicGenerator;
    }

    // -- UTIL

    public NamespacedKey key(String id) {
        return new NamespacedKey(this, id);
    }
}
