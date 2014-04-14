package org.jocean.event.api;

import org.jocean.event.api.internal.EventHandler;
import org.jocean.idiom.Detachable;
import org.jocean.idiom.ExectionLoop;

public interface DelayEvent {

    public DelayEvent args(final Object... args);
    
    public Detachable fireWith(final ExectionLoop exectionLoop, 
            final EventReceiver receiver);
    
    public <T extends EventHandler> T owner();
}
