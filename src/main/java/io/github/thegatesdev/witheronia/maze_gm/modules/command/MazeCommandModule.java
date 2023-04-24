package io.github.thegatesdev.witheronia.maze_gm.modules.command;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import io.github.thegatesdev.witheronia.maze_gm.modules.command.admin.GenerateMazeCommand;
import io.github.thegatesdev.witheronia.maze_gm.modules.command.admin.GiveItemCommand;
import io.github.thegatesdev.witheronia.maze_gm.modules.command.admin.OptionsCommand;
import io.github.thegatesdev.witheronia.maze_gm.modules.command.admin.QuestCommand;
import io.github.thegatesdev.witheronia.maze_gm.modules.generation.maze.MazeGenerationModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.generation.maze.MazeGenerator;
import io.github.thegatesdev.witheronia.maze_gm.modules.item.MazeItemModule;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.MazeQuestModule;

public class MazeCommandModule extends PluginModule<MazeGamemode> {
    private final CommandTree baseCommand = new CommandTree("witheronia").withAliases("wt");

    public MazeCommandModule(final MazeGamemode plugin) {
        super("commands", plugin);
    }

    private void createCommands() {
        add(GiveItemCommand.giveFromGroupArg(plugin.modules().getStatic(MazeItemModule.class).mazeItemGroup()));
        add(new LiteralArgument("reload").executes((CommandExecutor) (sender, objects) -> plugin.reload()));
        {
            final LiteralArgument mazeArg = new LiteralArgument("maze");
            MazeGenerator basicGenerator = plugin.modules().getStatic(MazeGenerationModule.class).basicGenerator();
            mazeArg.then(GenerateMazeCommand.placeBlocksArg(plugin, basicGenerator));
            mazeArg.then(GenerateMazeCommand.generateArg(basicGenerator));
            add(mazeArg);
        }
        add(OptionsCommand.dataTypeOptionsArg());
        add(OptionsCommand.factoryOptionsArg());
        add(OptionsCommand.eventOptionsArg(plugin.mazeReactors()));
        add(new LiteralArgument("quest").then(QuestCommand.activateQuestArg(plugin.modules().getStatic(MazeQuestModule.class))));
    }

    private void add(ArgumentTree args) {
        baseCommand.then(args);
    }

    // -- MODULE

    @Override
    protected void onFirstLoad() {
        createCommands();
        baseCommand.register();
    }
}
