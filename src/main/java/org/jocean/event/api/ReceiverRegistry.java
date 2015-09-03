package org.jocean.event.api;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ReceiverRegistry {
    private ConcurrentMap<UUID, EventReceiver> _flows;
    private volatile static ReceiverRegistry instance;

    private ReceiverRegistry() {
    }

    public static ReceiverRegistry getInstance() {
        if (instance == null) {
            synchronized (ReceiverRegistry.class) {
                if (instance == null) {
                    instance = new ReceiverRegistry();
                    instance._flows = new ConcurrentHashMap<>();
                }
            }
        }
        return instance;
    }

    public EventReceiver getReceiver(UUID key) {
        return _flows.get(key);
    }

    public EventReceiver addEventReceiver(UUID key, EventReceiver eventReceiver) {
        return _flows.putIfAbsent(key, eventReceiver);
    }

    public EventReceiver remove(UUID key) {
        return _flows.remove(key);
    }
}
