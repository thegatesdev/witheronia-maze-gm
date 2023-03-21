package io.github.thegatesdev.witheronia.maze_gm.data;

import io.github.thegatesdev.actionable.ReactorFactories;
import io.github.thegatesdev.actionable.util.twin.Twin;
import io.github.thegatesdev.eventador.EventData;
import io.github.thegatesdev.eventador.EventManager;
import io.github.thegatesdev.mapletree.data.Readable;
import io.github.thegatesdev.witheronia.maze_gm.util.spigot.ClickLocation;
import io.github.thegatesdev.witheronia.maze_gm.util.spigot.ClickType;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import static io.github.thegatesdev.actionable.Factories.*;

public class MazeReactors extends ReactorFactories {
    public MazeReactors(EventManager eventManager) {
        super(eventManager);
    }

    public void load() {
        addPerformers(new EventData<Entity>(eventManager).add("entity", EntityEvent.class, EntityEvent::getEntity), ENTITY_ACTION, ENTITY_CONDITION);

        doWithFactories(EntityDamageEvent.class, eventFactory -> eventFactory.addPerformer("combined", e -> e instanceof EntityDamageByEntityEvent, e -> Twin.of(e.getEntity(), ((EntityDamageByEntityEvent) e).getDamager()), ENTITY_ENTITY_CONDITION, ENTITY_ENTITY_ACTION));
        doWithFactories(PlayerInteractEvent.class, eventFactory -> {
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
