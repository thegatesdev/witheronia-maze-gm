package io.github.thegatesdev.witheronia.maze_gm.command;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;

public class Command extends CommandTree {

    private final MazeGamemode gamemode;

    public Command(MazeGamemode gamemode) {
        super("scourge");
        this.gamemode = gamemode;
        withAliases("sc");
    }

    @Override
    public void register() {
        addDefaults();
        super.register();
    }

    private void addDefaults() {
        then(cmdReload(gamemode));
    }

    private static LiteralArgument cmdReload(MazeGamemode mazeGamemode) {
        return (LiteralArgument) new LiteralArgument("reload").executes((CommandExecutor) (sender, args) -> mazeGamemode.reload());
    }
}
