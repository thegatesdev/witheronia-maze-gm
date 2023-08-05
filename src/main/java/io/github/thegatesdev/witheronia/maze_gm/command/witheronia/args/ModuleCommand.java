package io.github.thegatesdev.witheronia.maze_gm.command.witheronia.args;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import io.github.thegatesdev.threshold.pluginmodule.ModuleManager;
import io.github.thegatesdev.threshold.pluginmodule.PluginModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.thegatesdev.witheronia.maze_gm.util.DisplayUtil.*;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;

public final class ModuleCommand {

    public static LiteralArgument create(ModuleManager<?> moduleManager) {
        return (LiteralArgument) new LiteralArgument("module")
            .then(enableDisableArg(moduleManager))
            .then(statusArg(moduleManager));
    }

    private static MultiLiteralArgument enableDisableArg(ModuleManager<?> moduleManager) {
        return (MultiLiteralArgument) new MultiLiteralArgument("state", List.of("enable", "disable"))
            .then(new StringArgument("module_id")
                .replaceSuggestions(ArgumentSuggestions.stringCollection(i -> moduleManager.moduleKeys()))
                .executes((sender, args) -> {
                    String moduleId = args.getUnchecked("module_id");
                    var module = moduleManager.get(moduleId);
                    if (module == null) {
                        sender.sendMessage(text("Could not find module " + moduleId, FAIL_STYLE));
                        return;
                    }
                    if (Objects.equals(args.getUnchecked("state"), "enable")) {
                        module.enable();
                        sender.sendMessage(text("Enabled module " + moduleId, SUCCEED_STYLE));
                    } else {
                        module.disable();
                        sender.sendMessage(text("Disabled module " + moduleId, SUCCEED_STYLE));
                    }
                }));
    }

    private static LiteralArgument statusArg(ModuleManager<?> moduleManager) {
        return (LiteralArgument) new LiteralArgument("status").executes((sender, args) -> {
            var collect = new ArrayList<Component>(moduleManager.moduleCount());
            for (PluginModule<?> module : moduleManager) {
                var comp = text().append(text(module.id() + ":", VAR_STYLE).appendSpace());
                if (module.isEnabled()) comp.append(text("enabled", SUCCEED_STYLE.decorate(TextDecoration.ITALIC)));
                else comp.append(text("disabled", FAIL_STYLE.decorate(TextDecoration.ITALIC)));
                collect.add(comp.build());
            }
            sender.sendMessage(displayBlock(text("Module status"), join(JoinConfiguration.newlines(), collect)));
        });
    }
}
