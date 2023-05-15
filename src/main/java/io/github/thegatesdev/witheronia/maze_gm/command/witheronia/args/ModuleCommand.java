package io.github.thegatesdev.witheronia.maze_gm.command.witheronia.args;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.thegatesdev.threshold.pluginmodule.ModuleManager;
import io.github.thegatesdev.witheronia.maze_gm.util.DisplayUtil;
import net.kyori.adventure.text.Component;

public class ModuleCommand {

    private final LiteralArgument moduleCommand = new LiteralArgument("module");

    private MultiLiteralArgument enableDisableArg(ModuleManager<?> moduleManager) {
        return (MultiLiteralArgument) new MultiLiteralArgument("enable", "disable")
                .then(new StringArgument("module_id")
                        .replaceSuggestions(ArgumentSuggestions.stringCollection(i -> moduleManager.moduleKeys()))
                        .executes((sender, args) -> {
                            String moduleId = args.getUnchecked("module_id");
                            var module = moduleManager.get(moduleId);
                            if (module == null) {
                                sender.sendMessage(Component.text("Could not find module " + moduleId, DisplayUtil.FAIL_STYLE));
                                return;
                            }
                            if (args.get("enable") != null) {
                                module.enable();
                                sender.sendMessage(Component.text("Enabled module " + moduleId, DisplayUtil.SUCCEED_STYLE));
                            } else {
                                module.disable();
                                sender.sendMessage(Component.text("Disabled module " + moduleId, DisplayUtil.SUCCEED_STYLE));
                            }
                        }));
    }

    public LiteralArgument get() {
        return moduleCommand;
    }
}
