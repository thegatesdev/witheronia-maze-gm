package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.eventador.listener.struct.EventTypeHolder;
import io.github.thegatesdev.eventador.listener.struct.Listeners;
import io.github.thegatesdev.eventador.listener.struct.StaticListener;
import io.github.thegatesdev.stacker.item.ItemManager;
import org.bukkit.event.Event;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MazeItemListener {
    private final ItemManager itemManager;
    private final Listeners listeners;

    private final Map<Class<? extends Event>, ItemEvent<?>> itemEvents = new HashMap<>();

    private boolean enabled = true;

    public MazeItemListener(ItemManager itemManager, Listeners listeners) {
        this.itemManager = itemManager;
        this.listeners = listeners;
    }

    {
        itemEvent(PlayerInteractEvent.class, PlayerInteractEvent::getItem);
        itemEvent(PlayerDropItemEvent.class, e -> e.getItemDrop().getItemStack());
        itemEvent(PlayerItemConsumeEvent.class, PlayerItemConsumeEvent::getItem);
        itemEvent(PlayerItemBreakEvent.class, PlayerItemBreakEvent::getBrokenItem);
        itemEvent(PlayerAttemptPickupItemEvent.class, e -> e.getItem().getItemStack());
    }

    @SafeVarargs
    public final <E extends Event> void itemEvent(Class<E> eventClass, Function<E, ItemStack>... itemGetter) {
        itemEvents.putIfAbsent(eventClass, new ItemEvent<>(eventClass, itemGetter));
    }

    public <E extends Event> void listen(String itemKey, Class<E> eventClass, StaticListener<E> listener) {
        @SuppressWarnings("unchecked")
        var itemEvent = ((ItemEvent<E>) itemEvents.get(eventClass));
        if (itemEvent == null)
            throw new IllegalArgumentException(eventClass.getSimpleName() + " cannot be listened to");
        itemEvent.listen(itemKey, listener);
    }

    public <E extends Event, L extends StaticListener<E> & EventTypeHolder<E>> void listen(String itemKey, L listener) {
        listen(itemKey, listener.eventType(), listener);
    }

    public void clear() {
        itemEvents.forEach(listeners::stop);
        itemEvents.clear();
    }


    public MazeItemListener enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    private class ItemEvent<E extends Event> implements StaticListener<E> {
        private final Class<E> eventClass;
        private final Function<E, ItemStack>[] itemGetters;
        private Map<String, StaticListener<E>> mappedListeners;

        private ItemEvent(Class<E> eventClass, Function<E, ItemStack>[] itemGetters) {
            this.eventClass = eventClass;
            this.itemGetters = itemGetters;
        }


        private void register() {
            listeners.listen(eventClass, this);
        }

        public void listen(String key, StaticListener<E> listener) {
            if (mappedListeners == null) {
                mappedListeners = new HashMap<>();
                register();
            }
            mappedListeners.put(key, listener);
        }

        @Override
        public void onEvent(E event) {
            if (!enabled || mappedListeners == null) return;
            for (int i = 0; i < itemGetters.length; i++) {
                var stack = itemGetters[i].apply(event);
                if (stack == null) continue;
                var key = itemManager.itemId(stack.getItemMeta());
                if (key == null) continue;
                var listener = mappedListeners.get(key);
                if (listener == null) continue;
                listener.onEvent(event);
            }
        }
    }
}
