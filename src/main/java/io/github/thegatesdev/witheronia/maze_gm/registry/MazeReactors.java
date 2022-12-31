package io.github.thegatesdev.witheronia.maze_gm.registry;

import io.github.thegatesdev.eventador.event.EventManager;
import io.github.thegatesdev.eventador.registry.ReactorFactories;
import io.github.thegatesdev.eventador.util.twin.Twin;
import io.github.thegatesdev.mapletree.data.Readable;
import io.github.thegatesdev.witheronia.maze_gm.util.spigot.ClickLocation;
import io.github.thegatesdev.witheronia.maze_gm.util.spigot.ClickType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import static io.github.thegatesdev.eventador.registry.Factories.*;

public class MazeReactors extends ReactorFactories {

    public MazeReactors(EventManager eventManager) {
        super(eventManager);
        loadOptions();
    }

    private void loadOptions() {
        // Events with entities.
        doWithFactoriesOf(EntityEvent.class, eventFactory -> eventFactory.addPerformer("entity", EntityEvent::getEntity, ENTITY_CONDITION, ENTITY_ACTION));
        doWithFactoriesOf(PlayerEvent.class, eventFactory -> eventFactory.addPerformer("entity", PlayerEvent::getPlayer, ENTITY_CONDITION, ENTITY_ACTION));


        doWithFactoriesOf(EntityDamageEvent.class, eventFactory -> eventFactory.addPerformer("both", e -> e instanceof EntityDamageByEntityEvent, e -> Twin.of(e.getEntity(), ((EntityDamageByEntityEvent) e).getDamager()), ENTITY_ENTITY_CONDITION, ENTITY_ENTITY_ACTION));
        doWithFactoriesOf(PlayerInteractEvent.class, eventFactory -> {
            eventFactory.getReadableData()
                    .add("click_type", Readable.enumeration(ClickType.class), ClickType.BOTH)
                    .add("click_location", Readable.enumeration(ClickLocation.class), ClickLocation.BOTH);
            eventFactory.addStaticCondition((data, e) -> {
                if (!ClickType.spigot(e.getAction()).compare(data.get("click_type", ClickType.class))) return false;
                return ClickLocation.spigot(e.getAction()).compare(data.get("click_location", ClickLocation.class));
            });
        });
    }
}
