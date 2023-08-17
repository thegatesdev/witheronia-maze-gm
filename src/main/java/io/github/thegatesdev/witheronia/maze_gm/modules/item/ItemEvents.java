package io.github.thegatesdev.witheronia.maze_gm.modules.item;

import io.github.thegatesdev.stacker.item.ItemManager;
import io.github.thegatesdev.threshold.event.listening.ClassListener;
import io.github.thegatesdev.threshold.event.listening.Listeners;
import io.github.thegatesdev.threshold.event.listening.StaticListener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ItemEvents {
    private final Listeners listeners;
    private final ItemManager itemManager;
    private final Map<Class<?>, ItemEvent<?>> entries = new HashMap<>();

    public ItemEvents(Listeners listeners, ItemManager itemManager) {
        this.listeners = listeners;
        this.itemManager = itemManager;
    }

    public <Event> ItemEvent<Event> define(Class<Event> eventClass, Function<Event, ItemStack> stackGetter) {
        if (entries.containsKey(eventClass)) throw new IllegalArgumentException("Item event for " + eventClass.getSimpleName() + " already defined");
        var ev = new ItemEvent<>(eventClass, stackGetter);
        entries.put(eventClass, ev);
        return ev;
    }

    @SuppressWarnings("unchecked")
    public <Event> ItemEvent<Event> get(Class<Event> eventClass) {
        var itemEvent = (ItemEvent<Event>) entries.get(eventClass);
        if (itemEvent == null) throw new IllegalArgumentException("Event is not an item event: " + eventClass.getSimpleName());
        return itemEvent;
    }

    public <Event> void setListener(String itemKey, ClassListener<Event> listener) {
        get(listener.eventType()).setListener(itemKey, listener);
    }

    public <Event> void clearListeners(String itemKey) {
        entries.values().forEach(itemEvent -> itemEvent.clearListener(itemKey));
    }

    public boolean has(Class<?> eventClass) {
        return entries.containsKey(eventClass);
    }

    public class ItemEvent<Event> implements StaticListener<Event> {
        private final Class<Event> eventClass;
        private final Function<Event, ItemStack> stackGetter;
        private final Map<String, StaticListener<Event>> itemListeners = new HashMap<>();

        private ItemEvent(Class<Event> eventClass, Function<Event, ItemStack> stackGetter) {
            this.eventClass = eventClass;
            this.stackGetter = stackGetter;
        }

        public void setListener(String itemKey, StaticListener<Event> listener) {
            if (itemListeners.isEmpty()) listeners.listen(eventClass, this);
            itemListeners.put(itemKey, listener);
        }

        public void clearListener(String itemKey) {
            itemListeners.remove(itemKey);
            if (itemListeners.isEmpty()) listeners.stop(eventClass, this);
        }

        @Override
        public void onEvent(Event event) {
            if (itemListeners.isEmpty()) return;
            var stack = stackGetter.apply(event);
            if (stack == null) return;
            var itemKey = itemManager.itemId(stack);
            if (itemKey == null) return;
            var listener = itemListeners.get(itemKey);
            if (listener != null) listener.onEvent(event);
        }
    }
}
