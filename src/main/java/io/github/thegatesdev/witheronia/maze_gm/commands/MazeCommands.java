package io.github.thegatesdev.witheronia.maze_gm.commands;

import dev.jorel.commandapi.CommandTree;
import io.github.thegatesdev.witheronia.maze_gm.commands.admin.GenerateMazeCommand;
import io.github.thegatesdev.witheronia.maze_gm.main.MazeGamemode;

import java.util.ArrayList;
import java.util.List;

public class MazeCommands {

    private final MazeGamemode mazeGamemode;
    private final List<CommandTree> mazeCommands = new ArrayList<>();

    private boolean created = false, registered = false;

    public MazeCommands(MazeGamemode mazeGamemode) {
        this.mazeGamemode = mazeGamemode;
    }

    public void create() {
        if (created) throw new RuntimeException("Already created");

        add(new CommandTree("basic_maze")
                .then(GenerateMazeCommand.generateArg(mazeGamemode.getBasicGenerator()))
                .then(GenerateMazeCommand.placeBlocksArg(mazeGamemode, mazeGamemode.getBasicGenerator())
                ));

        created = true;
    }

    public void register() {
        if (registered) throw new RuntimeException("Already registered");

        for (final CommandTree command : mazeCommands) {
            command.register();
        }

        registered = true;
    }

    private void add(CommandTree commandTree) {
        mazeCommands.add(commandTree);
    }
}
