/**
 * 
 */
package org.jocean.event.api;

import org.jocean.event.api.internal.EventHandler;


/**
 * @author isdom
 * 
 */
public interface EventReceiverSource {

    public EventReceiver create(final Object flow, final EventHandler initState);
    
    public EventReceiver createFromInnerState(final EventHandler initState);
}
