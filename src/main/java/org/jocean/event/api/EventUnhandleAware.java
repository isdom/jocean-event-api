/**
 * 
 */
package org.jocean.event.api;

import org.jocean.event.api.internal.Eventable;

/**
 * @author isdom
 *
 */
public interface EventUnhandleAware extends Eventable {
    public void onEventUnhandle(final String event, final Object ... args) throws Exception;
}
