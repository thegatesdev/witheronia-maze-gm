package io.github.thegatesdev.witheronia.maze_gm;

import io.github.thegatesdev.eventador.listener.struct.ClassListener;
import io.github.thegatesdev.eventador.listener.struct.Listeners;
import io.github.thegatesdev.eventador.listener.struct.StaticListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DataListeners<Data, Key> {
    private final Listeners listeners;
    private final Function<Data, Key> keyGetter;

    private final Map<Class<?>, Listener<?>> data = new HashMap<>();

    private boolean enabled;

    public DataListeners(Listeners listeners, Function<Data, Key> keyGetter) {
        this.listeners = listeners;
        this.keyGetter = keyGetter;
    }

    public DataListeners<Data, Key> enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public void reset() {
        data.forEach(listeners::stop);
        data.clear();
    }


    @SuppressWarnings("unchecked")
    public <Event> Listener<Event> event(Class<Event> eventClass) {
        return (Listener<Event>) data.computeIfAbsent(eventClass, Listener::new);
    }

    public <Event> boolean listen(Key key, ClassListener<Event> listener, String dataName) {
        return event(listener.eventType()).listen(key, listener, dataName);
    }

    public <Event> void data(Class<Event> eventClass, Function<Event, Data> getter, String name) {
        event(eventClass).add(getter, name);
    }

    public <Event> void data(Class<Event> eventClass, Function<Event, Data> getter) {
        event(eventClass).add(getter);
    }


    public class Listener<Event> implements StaticListener<Event> {
        private final Class<Event> eventClass;
        private final Map<String, EventData<Event, Data, Key>> mapped = new HashMap<>();
        private final List<EventData<Event, Data, Key>> listed = new ArrayList<>();

        public Listener(Class<Event> eventClass) {
            this.eventClass = eventClass;
        }

        public Listener<Event> add(Function<Event, Data> getter, String name) {
            mapped.computeIfAbsent(name, s -> {
                var data = new EventData<Event, Data, Key>(s, getter);
                listed.add(data);
                return data;
            });
            return this;
        }

        public Listener<Event> add(Function<Event, Data> getter) {
            return add(getter, "single");
        }

        public boolean listen(Key key, StaticListener<Event> listener, String dataName) {
            if (mapped.isEmpty()) return false;
            if (dataName == null) listed.forEach(e -> e.listeners.putIfAbsent(key, listener));
            else {
                var data = mapped.get(dataName);
                return data != null && data.listeners.putIfAbsent(key, listener) == null;
            }
            return true;
        }

        public boolean listen(Key key, StaticListener<Event> listener) {
            return listen(key, listener, null);
        }

        private void register() {
            listeners.listen(eventClass, this);
        }

        @Override
        public void onEvent(Event event) {
            if (enabled) for (int i = 0; i < listed.size(); i++) {
                var entry = listed.get(i);
                var data = entry.getter.apply(event);
                if (data == null) continue;
                var key = keyGetter.apply(data);
                if (key == null) continue;
                var listener = entry.listeners.get(key);
                if (listener != null) listener.onEvent(event);
            }
        }
    }

    private record EventData<Event, Data, Key>(
        String name,
        Function<Event, Data> getter,
        Map<Key, StaticListener<Event>> listeners) {

        public EventData(String name, Function<Event, Data> getter) {
            this(name, getter, new HashMap<>());
        }
    }
}
