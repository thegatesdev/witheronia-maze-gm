package io.github.thegatesdev.witheronia.maze_gm.quest;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class Goal<E extends Event, T> {

    private final Class<E> eventClass;
    private final BiPredicate<E, T> doesComplete;

    private BiConsumer<Player, T> acceptAction;
    private BiConsumer<E, T> completeAction, failAction;


    public static Goal<PlayerInteractAtEntityEvent, Entity> takeItems(Material material, int minimum, int take) {
        return new Goal<>(PlayerInteractAtEntityEvent.class, (event, entity) -> {
            if (event.getRightClicked() != entity) return false;
            final ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
            if (item.getType() != material) return false;
            final int amount = item.getAmount();
            if (amount < minimum) return false;
            item.setAmount(amount - take);
            return true;
        });
    }


    public Goal(Class<E> eventClass, BiPredicate<E, T> doesComplete) {
        this.eventClass = eventClass;
        this.doesComplete = doesComplete;
    }

    public Goal<E, T> onAccept(final BiConsumer<Player, T> acceptAction) {
        this.acceptAction = acceptAction;
        return this;
    }

    public Goal<E, T> onComplete(final BiConsumer<E, T> completeAction) {
        this.completeAction = completeAction;
        return this;
    }

    public Goal<E, T> onFail(final BiConsumer<E, T> failAction) {
        this.failAction = failAction;
        return this;
    }

    public <B extends T> boolean didComplete(E event, B origin) {
        if (doesComplete.test(event, origin)) {
            completeAction.accept(event, origin);
            return true;
        }
        failAction.accept(event, origin);
        return false;
    }

    public void accept(Player player, T origin) {
        if (acceptAction != null) acceptAction.accept(player, origin);
    }

    public Class<E> getEventClass() {
        return eventClass;
    }
}
