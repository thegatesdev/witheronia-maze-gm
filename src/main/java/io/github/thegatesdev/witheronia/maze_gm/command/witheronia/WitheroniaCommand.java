package io.github.thegatesdev.witheronia.maze_gm.command.witheronia;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.command.witheronia.args.ModuleCommand;
import io.github.thegatesdev.witheronia.maze_gm.command.witheronia.args.ViewOptionsCommand;

public class WitheroniaCommand {
    private final CommandTree baseCommand = new CommandTree("witheronia").withAliases("wt");
    private final MazeGamemode mazeGamemode;

    private boolean canModify = true;

    public WitheroniaCommand(MazeGamemode mazeGamemode) {
        this.mazeGamemode = mazeGamemode;
    }

    private void load() {
        add(mazeGamemode.registerCached(new ViewOptionsCommand()).get());
        add(new ModuleCommand().get());
    }


    public void add(LiteralArgument... arguments) {
        if (!canModify) throw new RuntimeException("Witheronia command cannot be edited anymore");
        for (LiteralArgument argument : arguments) add(argument);
    }

    public void add(LiteralArgument argument) {
        if (!canModify) throw new RuntimeException("Witheronia command cannot be edited anymore");
        baseCommand.then(argument);
    }


    public void register() {
        load();
        canModify = false;
        baseCommand.register();
    }
}
