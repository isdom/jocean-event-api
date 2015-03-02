/**
 * 
 */
package org.jocean.event.api;

import org.jocean.event.api.internal.EventHandler;


/**
 * @author isdom
 * 
 */
public interface EventEngine {

    public EventReceiver create(final Object flow, final EventHandler initState);
    
    public EventReceiver createFromInnerState(final EventHandler initState);
}
