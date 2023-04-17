package io.github.thegatesdev.witheronia.maze_gm.data;

import io.github.thegatesdev.actionable.EventFactories;
import io.github.thegatesdev.actionable.util.twin.Twin;
import io.github.thegatesdev.eventador.core.EventTypes;
import io.github.thegatesdev.mapletree.data.Readable;
import io.github.thegatesdev.witheronia.maze_gm.util.spigot.ClickLocation;
import io.github.thegatesdev.witheronia.maze_gm.util.spigot.ClickType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import static io.github.thegatesdev.actionable.Factories.*;

public class MazeReactors extends EventFactories {
    public MazeReactors(EventTypes eventTypes) {
        super(eventTypes);
    }

    private void load() {
        eachFactory(EntityEvent.class, r -> r.addPerformer("entity", EntityEvent::getEntity, ENTITY_CONDITION, ENTITY_ACTION));
        eachFactory(EntityDamageEvent.class, r -> r.addPerformer("combined", e -> Twin.of(e.getEntity(), ((EntityDamageByEntityEvent) e).getDamager()), e -> e instanceof EntityDamageByEntityEvent, ENTITY_ENTITY_CONDITION, ENTITY_ENTITY_ACTION));

        eachFactory(PlayerInteractEvent.class, eventFactory -> {
            eventFactory.readableOptions()
                    .add("click_type", Readable.enumeration(ClickType.class), ClickType.BOTH)
                    .add("click_location", Readable.enumeration(ClickLocation.class), ClickLocation.BOTH);
            eventFactory.addStaticCondition((data, e) -> {
                if (!ClickType.spigot(e.getAction()).compare(data.get("click_type", ClickType.class))) return false;
                return ClickLocation.spigot(e.getAction()).compare(data.get("click_location", ClickLocation.class));
            });
        });
    }
}
