package org.jocean.event.api;

import org.jocean.event.api.internal.EventHandler;

public interface FlowStateChangedListener<FLOW, HANDLER extends EventHandler> {
	public void onStateChanged(
		final FLOW		flow,
		final HANDLER 	prev, 
		final HANDLER 	next,
		final String 	causeEvent, 
		final Object[] 	causeArgs) throws Exception;
}
