/**
 * 
 */
package org.jocean.event.api.internal;

/**
 * @author isdom
 *
 */
public interface EventInvoker {
	
	public <RET> RET invoke(final Object[] args) throws Exception;
	
	public String	getBindedEvent();
}
