/**
 * 
 */
package org.jocean.event.api.internal;

import org.jocean.event.api.EventReceiver;

/**
 * @author isdom
 *
 */
public interface FlowLifecycleAware {
	public void afterEventReceiverCreated(final EventReceiver receiver) throws Exception;
	public void afterFlowDestroy() throws Exception;
}
