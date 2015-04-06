/**
 * 
 */
package org.jocean.event.api;

import org.jocean.event.api.internal.Eventable;

/**
 * @author isdom
 *
 */
public interface EventReceiver {
	
	public boolean acceptEvent(final String event, final Object... args);
	
    public boolean acceptEvent(final Eventable eventable, final Object... args);
}
