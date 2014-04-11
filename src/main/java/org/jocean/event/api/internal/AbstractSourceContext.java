/**
 * 
 */
package org.jocean.event.api.internal;

import org.jocean.event.api.EventReceiverSource.Context;

/**
 * @author isdom
 *
 */
public class AbstractSourceContext<T extends AbstractSourceContext<?>> 
    implements Context {
    public Object flow() {
        return this._flow;
    }
    
    public EventHandler initHandler() {
        return this._initHandler;
    }
    
    @SuppressWarnings("unchecked")
    public T flow(final Object flow) {
        this._flow = flow;
        return (T)this;
    }
    
    @SuppressWarnings("unchecked")
    public T initHandler(final EventHandler initHandler) {
        this._initHandler = initHandler;
        return (T)this;
    }
    
    private Object _flow;
    private EventHandler _initHandler;
}
