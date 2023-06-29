package io.github.thegatesdev.witheronia.maze_gm.data;

import io.github.thegatesdev.actionable.EventFactories;
import io.github.thegatesdev.actionable.util.twin.Twin;
import io.github.thegatesdev.eventador.core.EventTypes;
import io.github.thegatesdev.maple.read.Readable;
import io.github.thegatesdev.witheronia.maze_gm.util.spigot.ClickLocation;
import io.github.thegatesdev.witheronia.maze_gm.util.spigot.ClickType;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import static io.github.thegatesdev.actionable.Factories.*;

public class MazeEvents extends EventFactories {
    public MazeEvents(EventTypes eventTypes) {
        super(eventTypes);
    }

    {
        eachFactory(EntityEvent.class, r -> r.addPerformer("entity", EntityEvent::getEntity, ENTITY_CONDITION, ENTITY_ACTION));
        eachFactory(PlayerEvent.class, r -> r.addPerformer("player", PlayerEvent::getPlayer, ENTITY_CONDITION, ENTITY_ACTION));

        eachFactory(EntityDamageEvent.class, r -> r.addPerformer("combined",
                e -> Twin.of(e.getEntity(), ((EntityDamageByEntityEvent) e).getDamager()),
                e -> e instanceof EntityDamageByEntityEvent, ENTITY_ENTITY_CONDITION, ENTITY_ENTITY_ACTION));

        eachFactory(PlayerInteractEvent.class, r -> {
            r.readableOptions()
                    .add("click_type", Readable.enumeration(ClickType.class), ClickType.ANY)
                    .add("click_location", Readable.enumeration(ClickLocation.class), ClickLocation.ANY);
            r.addStaticCondition((data, e) -> {
                final Action action = e.getAction();
                return data.<ClickType>getUnsafe("click_type").compare(action) &&
                        data.<ClickLocation>getUnsafe("click_location").compare(action);
            });
        });
    }
}
