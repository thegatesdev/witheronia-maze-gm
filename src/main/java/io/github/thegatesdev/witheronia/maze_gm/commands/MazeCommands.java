package io.github.thegatesdev.witheronia.maze_gm.commands;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import io.github.thegatesdev.skiller.ItemGroup;
import io.github.thegatesdev.skiller.ItemManager;
import io.github.thegatesdev.witheronia.maze_gm.commands.admin.GenerateMazeCommand;
import io.github.thegatesdev.witheronia.maze_gm.commands.admin.GiveItemCommand;
import io.github.thegatesdev.witheronia.maze_gm.commands.admin.OptionsCommand;
import io.github.thegatesdev.witheronia.maze_gm.main.MazeGamemode;

public class MazeCommands {
    private final MazeGamemode mazeGamemode;
    private final CommandTree baseCommand = new CommandTree("witheronia").withAliases("wt");

    private boolean created = false;
    private boolean registered = false;

    public MazeCommands(MazeGamemode mazeGamemode) {
        this.mazeGamemode = mazeGamemode;
    }

    public void create() {
        if (created) throw new RuntimeException("Already created");

        add(new LiteralArgument("basic_maze")
                .then(GenerateMazeCommand.generateArg(mazeGamemode.getBasicGenerator()))
                .then(GenerateMazeCommand.placeBlocksArg(mazeGamemode, mazeGamemode.getBasicGenerator()))
        );

        {
            final LiteralArgument give = new LiteralArgument("give");
            final ItemManager itemManager = mazeGamemode.getItemManager();
            for (final ItemGroup group : itemManager.groups()) {
                give.then(GiveItemCommand.giveFromGroupArg(group));
            }
            add(give);
        }

        {
            final LiteralArgument options = new LiteralArgument("options");
            options.then(OptionsCommand.eventOptionsArg(mazeGamemode.getMazeEvents()));
            options.then(OptionsCommand.dataTypeOptionsArg());
            options.then(OptionsCommand.factoryOptionsArg());
            add(options);
        }

        add(new LiteralArgument("reload").executes((CommandExecutor) (sender, args) -> mazeGamemode.reload()));

        created = true;
    }

    public void add(ArgumentTree argumentTree) {
        baseCommand.then(argumentTree);
    }

    public void register() {
        if (registered) throw new RuntimeException("Already registered");
        baseCommand.register();
        registered = true;
    }
}
