/**
 * 
 */
package org.jocean.event.helper;

/**
 * @author isdom
 *
 */
public interface FlowTracker {
	
	public void registerFlowStateChangeListener(
			final FlowStateChangeListener listener);
	
	public void unregisterFlowStateChangeListener(
			final FlowStateChangeListener listener);

}
