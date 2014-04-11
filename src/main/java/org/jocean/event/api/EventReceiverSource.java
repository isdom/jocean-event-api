/**
 * 
 */
package org.jocean.event.api;

import org.jocean.event.api.internal.AbstractSourceContext;
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
    
    public final class DefaultContext 
        extends AbstractSourceContext<DefaultContext> {
    }
    
    public EventReceiver create(final Context ctx);
}
