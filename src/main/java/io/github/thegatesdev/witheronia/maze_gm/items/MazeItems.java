package io.github.thegatesdev.witheronia.maze_gm.items;

import io.github.thegatesdev.eventador.registry.ReactorFactories;
import io.github.thegatesdev.skiller.ItemRegistry;
import io.github.thegatesdev.skiller.Skiller;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

public class MazeItems extends ItemRegistry {

    private final ReactorFactories reactors;

    public MazeItems(NamespacedKey itemKey, ReactorFactories reactors) {
        super("maze_items", Skiller.persistentDataTransfer(itemKey, PersistentDataType.STRING));
        this.reactors = reactors;
    }
}
