package org.jocean.event.api.internal;


public interface EventHandlerAware {
	public void setEventHandler(final EventHandler handler) throws Exception;
}
