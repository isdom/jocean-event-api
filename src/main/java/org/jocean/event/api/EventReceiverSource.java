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

    public interface Context {
        public Object flow();
        public EventHandler initHandler();
    }
    
    public class DefaultContext<T extends Context> implements Context {
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
    
    public EventReceiver create(final Context ctx);
}
