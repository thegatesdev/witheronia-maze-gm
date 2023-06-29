package io.github.thegatesdev.witheronia.maze_gm.command.witheronia.args;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;

public class ReloadCommand {

    public static LiteralArgument create(MazeGamemode mazeGamemode) {
        return (LiteralArgument) new LiteralArgument("reload").executes((CommandExecutor) (sender, args) -> mazeGamemode.reload());
    }
}