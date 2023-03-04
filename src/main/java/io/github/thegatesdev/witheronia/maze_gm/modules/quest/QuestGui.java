package io.github.thegatesdev.witheronia.maze_gm.modules.quest;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.type.ActiveQuest;
import io.github.thegatesdev.witheronia.maze_gm.modules.quest.type.Quest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class QuestGui {

    private final OutlinePane backGroundPane = new OutlinePane(0, 0, 9, 6, Pane.Priority.LOWEST);

    {
        backGroundPane.addItem(new GuiItem(new ItemStack(Material.BLUE_STAINED_GLASS_PANE)));
        backGroundPane.setRepeat(true);
    }

    private ItemStack createQuestItem(Quest<?> quest, QuestData.PlayerEntry playerData) {
        ItemStack itemStack = new ItemStack(quest.displayMaterial() == null ? Material.STRUCTURE_VOID : quest.displayMaterial());
        ItemMeta meta = itemStack.getItemMeta();
        String id = quest.id();
        meta.displayName(Component.text(id, TextColor.color(200, 200, 50)));
        meta.lore(List.of(
                Component.text("Difficulty: " + quest.difficulty(), TextColor.color(50, 100, 255)),
                Component.text(playerData.isFinished(id) ? "Finished" : (playerData.isActive(id) ? "Active" : "Available"), TextColor.color(100, 90, 230))
        ));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private <O> void handleQuestGuiClick(Gui gui, GuiItem item, Quest<O> quest, O origin, QuestData.PlayerEntry playerData, Player player) {
        if (playerData.isFinished(quest.id())) {
            player.sendMessage(Component.text("You have already finished this quest!", TextColor.color(0, 255, 0)));
            return;
        }
        if (playerData.activate(new ActiveQuest<>(quest, origin, player))) {
            item.setItem(createQuestItem(quest, playerData));
            gui.update();
            player.sendMessage(Component.text("You accepted a quest.", TextColor.color(0, 255, 0)));
        } else {
            player.sendMessage(Component.text("You have already accepted this quest!", TextColor.color(0, 255, 0)));
        }
    }

    public <O> ChestGui create(QuestData.PlayerEntry playerData, O origin, List<Quest<O>> quests, String name) {
        final ChestGui gui = new ChestGui(6, name);
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        gui.setOnGlobalDrag(event -> event.setCancelled(true));

        OutlinePane questsPane = new OutlinePane(2, 1, 6, 4);
        for (final Quest<O> quest : quests) {
            final GuiItem item = new GuiItem(createQuestItem(quest, playerData));
            item.setAction(event -> handleQuestGuiClick(gui, item, quest, origin, playerData, (Player) event.getWhoClicked()));
            questsPane.addItem(item);
        }
        gui.addPane(backGroundPane);
        gui.addPane(questsPane);
        return gui;
    }
}
