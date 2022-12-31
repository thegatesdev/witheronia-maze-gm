package io.github.thegatesdev.witheronia.maze_gm.commands.admin;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LongArgument;
import io.github.thegatesdev.witheronia.maze_gm.generation.maze.MazeGenerator;
import io.github.thegatesdev.witheronia.maze_gm.generation.maze.Vector3;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class GenerateMazeCommand {
    public static ArgumentTree generateArg(MazeGenerator generator) {
        return new LiteralArgument("generate")
                .then(new IntegerArgument("space").then(new IntegerArgument("wallSize")
                        .then(new IntegerArgument("width").then(new IntegerArgument("depth").then(new IntegerArgument("height")
                                .then(new LongArgument("seed")
                                        .executes((sender, args) -> {
                                            final int space = (int) args[0], wallSize = (int) args[1];
                                            final int width = (int) args[2], depth = (int) args[3], height = (int) args[4];
                                            final long seed = (long) args[5];
                                            final long beforeTime = System.nanoTime();
                                            generator.generate(seed, new Vector3(width, height, depth), space, wallSize);
                                            sender.sendMessage("Maze was generated in %sms".formatted((System.nanoTime() - beforeTime) * 1E6));
                                        })
                                ))))));
    }

    public static ArgumentTree placeBlocksArg(Plugin plugin, MazeGenerator generator) {
        return new LiteralArgument("place")
                .then(new LocationArgument("location")
                        .then(new IntegerArgument("perTick")
                                .executes((sender, objects) -> {
                                    if (!generator.isGenerated())
                                        throw CommandAPI.failWithString("No maze generated yet!");
                                    final Location loc = (Location) objects[0];
                                    generator.placeBlocksThreaded(plugin, loc, (int) objects[1]);
                                })
                        ));
    }
}
