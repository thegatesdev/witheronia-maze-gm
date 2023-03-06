package io.github.thegatesdev.witheronia.maze_gm.modules.command;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.CommandTree;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.modules.command.admin.GenerateMazeCommand;
import io.github.thegatesdev.witheronia.maze_gm.modules.command.admin.GiveItemCommand;
import io.github.thegatesdev.witheronia.maze_gm.modules.command.admin.OptionsCommand;
import io.github.thegatesdev.witheronia.maze_gm.modules.generation.maze.MazeGenerationModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.generation.maze.MazeGenerator;
import io.github.thegatesdev.witheronia.maze_gm.modules.item.MazeItemModule;

public class MazeCommandModule extends PluginModule<MazeGamemode> {
    private final CommandTree baseCommand = new CommandTree("witheronia").withAliases("wt");

    public MazeCommandModule(final MazeGamemode plugin) {
        super("commands", plugin);
    }

    private void createCommands() {
        add(GiveItemCommand.giveFromGroupArg(plugin.modules().getStatic(MazeItemModule.class).itemGroup()));
        {
            MazeGenerator basicGenerator = plugin.modules().getStatic(MazeGenerationModule.class).basicGenerator();
            add(GenerateMazeCommand.generateArg(basicGenerator));
            add(GenerateMazeCommand.placeBlocksArg(plugin, basicGenerator));
        }
        add(OptionsCommand.dataTypeOptionsArg());
        add(OptionsCommand.factoryOptionsArg());
        add(OptionsCommand.eventOptionsArg(plugin.mazeReactors()));
    }

    private void add(ArgumentTree args) {
        baseCommand.then(args);
    }

    // -- MODULE


    @Override
    public void onLoad() {
        createCommands();
    }

    @Override
    public void onEnable() {
        baseCommand.register();
    }
}