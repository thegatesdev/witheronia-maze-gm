package io.github.thegatesdev.witheronia.maze_gm.modules.quest.type;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

public interface Goal<E extends Event, O> {

    static FunctionalGoal<PlayerInteractAtEntityEvent, Entity> takeItems(Material material, int minimum, int take) {
        return new FunctionalGoal<>(PlayerInteractAtEntityEvent.class, (event, entity) -> {
            if (event.getRightClicked() != entity) return false;
            final ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
            if (item.getType() != material) return false;
            final int amount = item.getAmount();
            if (amount < minimum) return false;
            item.setAmount(amount - take);
            return true;
        });
    }

    boolean completesGoal(E event, O origin);

    void accept(Player player, O origin);

    Class<E> eventClass();
}
