package org.jocean.event.api;

import org.jocean.event.api.internal.EventHandler;

public interface FlowStateChangedListener<HANDLER extends EventHandler> {
	public void onStateChanged(
		final HANDLER 	prev, 
		final HANDLER 	next,
		final String 	causeEvent, 
		final Object[] 	causeArgs) throws Exception;
}
