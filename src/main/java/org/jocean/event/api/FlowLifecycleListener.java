/**
 * 
 */
package org.jocean.event.api;

/**
 * @author isdom
 *
 */
public interface FlowLifecycleListener {
	
	public void afterEventReceiverCreated(final EventReceiver receiver) 
			throws Exception;
	
	public void afterFlowDestroy() throws Exception;	
}
