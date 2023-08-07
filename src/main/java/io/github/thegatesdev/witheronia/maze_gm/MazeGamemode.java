package io.github.thegatesdev.witheronia.maze_gm;

import io.github.thegatesdev.witheronia.maze_gm.command.witheronia.WitheroniaCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class MazeGamemode extends JavaPlugin {
    private final WitheroniaCommand witheroniaCommand = new WitheroniaCommand(this);

    @Override
    public void onLoad() {
        witheroniaCommand.register();
    }

    public void reload() {

    }
}
