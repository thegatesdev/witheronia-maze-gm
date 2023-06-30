package io.github.thegatesdev.witheronia.maze_gm.command.witheronia.args;

import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import io.github.thegatesdev.witheronia.maze_gm.MazeGamemode;
import net.kyori.adventure.text.Component;

public class MetaCommands {

    public static LiteralArgument reload(MazeGamemode mazeGamemode) {
        return (LiteralArgument) new LiteralArgument("reload").executes((CommandExecutor) (sender, args) -> mazeGamemode.reload());
    }

    public static LiteralArgument errors(MazeGamemode mazeGamemode) {
        return (LiteralArgument) new LiteralArgument("errors")
                .then(new LiteralArgument("clear").executes((sender, args) -> {
                    mazeGamemode.clearErrors();
                    sender.sendMessage(Component.text("Errors have been cleared"));
                }))
                .then(new LiteralArgument("print").executes((sender, args) -> {
                    mazeGamemode.displayErrors(false);
                    sender.sendMessage(Component.text("Error messages have been printed in the console"));
                }))
                .then(new LiteralArgument("stacktrace").executes((sender, args) -> {
                    mazeGamemode.displayErrors(true);
                    sender.sendMessage(Component.text("Error stacktrace have been printed in the console"));
                }));
    }
}
