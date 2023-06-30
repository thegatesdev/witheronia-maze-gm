package io.github.thegatesdev.witheronia.maze_gm.command.witheronia;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.command.witheronia.args.MetaCommands;
import io.github.thegatesdev.witheronia.maze_gm.command.witheronia.args.ModuleCommand;
import io.github.thegatesdev.witheronia.maze_gm.command.witheronia.args.ViewOptionsCommand;

public class WitheroniaCommand {
    private final CommandTree baseCommand = new CommandTree("witheronia").withAliases("wt");
    private final MazeGamemode mazeGamemode;

    private boolean canModify = true;

    public WitheroniaCommand(MazeGamemode mazeGamemode) {
        this.mazeGamemode = mazeGamemode;
    }

    private void loadDefault() {
        add(ViewOptionsCommand.create());
        add(ModuleCommand.create(mazeGamemode.modules()));
        add(MetaCommands.reload(mazeGamemode));
        add(MetaCommands.errors(mazeGamemode));
    }


    public void add(LiteralArgument... arguments) {
        for (LiteralArgument argument : arguments) add(argument);
    }

    public void add(LiteralArgument argument) {
        if (!canModify) throw new RuntimeException("Witheronia command cannot be edited anymore");
        baseCommand.then(argument);
    }


    public void register() {
        loadDefault();
        canModify = false;
        baseCommand.register();
    }
}
