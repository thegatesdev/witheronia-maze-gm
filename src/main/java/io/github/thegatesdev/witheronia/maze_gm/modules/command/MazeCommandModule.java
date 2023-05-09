package io.github.thegatesdev.witheronia.maze_gm.modules.command;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import io.github.thegatesdev.threshold.pluginmodule.ModuleManager;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.modules.command.admin.ViewOptionsCommand;

public class MazeCommandModule extends PluginModule<MazeGamemode> {
    private final CommandTree baseCommand = new CommandTree("witheronia").withAliases("wt");

    public MazeCommandModule(ModuleManager<MazeGamemode> moduleManager) {
        super("commands", moduleManager);
    }

    public void add(LiteralArgument... arguments) {
        for (LiteralArgument argument : arguments) add(argument);
    }

    public void add(LiteralArgument argument) {
        baseCommand.then(argument);
    }

    // -- MODULE


    @Override
    protected void onInitialize() {
        add(new ViewOptionsCommand(plugin.mazeEvents()).get());
    }

    @Override
    protected void onFirstLoad() {
        baseCommand.register();
    }
}
