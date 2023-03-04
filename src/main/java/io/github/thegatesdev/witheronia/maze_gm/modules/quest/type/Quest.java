package io.github.thegatesdev.witheronia.maze_gm.modules.quest.type;

import io.github.thegatesdev.mapletree.registry.Identifiable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Set;

public interface Quest<O> extends Identifiable {
    void finish(Player player, O origin);

    void accept(Player player, O origin);

    Goal<?, O> goal(int index);

    int goals();

    Set<Class<? extends Event>> goalEvents();

    int difficulty();

    Material displayMaterial();
}
