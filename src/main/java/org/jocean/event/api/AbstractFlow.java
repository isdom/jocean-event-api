/**
 * 
 */
package org.jocean.event.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jocean.event.api.annotation.OnDelayed;
import org.jocean.event.api.annotation.OnEvent;
import org.jocean.event.api.internal.DefaultInvoker;
import org.jocean.event.api.internal.EndReasonSource;
import org.jocean.event.api.internal.EventHandler;
import org.jocean.event.api.internal.EventHandlerAware;
import org.jocean.event.api.internal.EventInvoker;
import org.jocean.event.api.internal.EventNameAware;
import org.jocean.event.api.internal.ExectionLoopAware;
import org.jocean.event.api.internal.FlowLifecycleAware;
import org.jocean.idiom.COWCompositeSupport;
import org.jocean.idiom.Detachable;
import org.jocean.idiom.ExceptionUtils;
import org.jocean.idiom.ExectionLoop;
import org.jocean.idiom.InterfaceSource;
import org.jocean.idiom.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author isdom
 *
 */
public abstract class AbstractFlow<FLOW>
	implements EventNameAware, 
		EventHandlerAware, 
		FlowLifecycleAware, 
		EndReasonSource,
		ExectionLoopAware,
		InterfaceSource,
		FlowStateChangedListener<FLOW,EventHandler>
		{

    private static final Logger LOG = 
            LoggerFactory.getLogger(AbstractFlow.class);
    
    @SuppressWarnings("unchecked")
    @Override
	public <INTF> INTF queryInterfaceInstance(final Class<INTF> intfCls) {
        if ( intfCls.equals(EventReceiver.class)) {
            return (INTF)selfEventReceiver();
        }
        
		INTF ret = (INTF)this._adapters.get(intfCls);
		
		if ( null != ret ) {
			return ret;
		}
		return createAndSaveInterfaceAdapter(intfCls);
	}
	
	@SuppressWarnings("unchecked")
	private <INTF> INTF createAndSaveInterfaceAdapter(final Class<INTF> intfCls) {
		final INTF intf = EventUtils.buildInterfaceAdapter(intfCls, this._receiver);
		final Object oldIntf = this._adapters.putIfAbsent(intfCls, intf);
		if ( null != oldIntf ) {
			return (INTF)oldIntf;
		}
		else {
			return intf;
		}
	}
	
	@SuppressWarnings("unchecked")
	public FLOW addFlowLifecycleListener(final FlowLifecycleListener<FLOW> lifecycle) {
		this._lifecycleSupport.addComponent(lifecycle);
		return (FLOW)this;
	}
	
	@SuppressWarnings("unchecked")
	public FLOW removeFlowLifecycleListener(final FlowLifecycleListener<FLOW> lifecycle) {
		this._lifecycleSupport.removeComponent(lifecycle);
		return (FLOW)this;
	}
	
	public EventInvoker selfInvoker(final String methodName) {
		return DefaultInvoker.of(this, methodName);
	}
	
    public EventInvoker[] handlersOf(final Object target) {
        return DefaultInvoker.invokers(target, OnEvent.class);
    }
    
    public EventInvoker[] delayedHandlersOf(final Object target) {
        return DefaultInvoker.invokers(target, OnDelayed.class);
    }
    
    protected Detachable fireDelayEvent(final DelayEvent delayEvent) {
        return delayEvent.fireWith( this._exectionLoop, this._receiver);
    }
    
    public <T extends EventHandler> T fireDelayEventAndAddTo(
            final DelayEvent delayEvent, final Collection<Detachable> timers) {
        final Detachable task = delayEvent.fireWith( 
                this._exectionLoop, this._receiver);
        if ( null!=task && null!=timers ) {
            timers.add(task);
        }
        return delayEvent.owner();
    }

    public void removeAndCancelAllDealyEvents(final Collection<Detachable> timers) {
        while ( null!=timers && !timers.isEmpty() ) {
            final Iterator<Detachable> itr = timers.iterator();
            final Detachable task = itr.next();
            itr.remove();
            if ( null != task ) {
                try {
                    task.detach();
                }
                catch (Throwable e) {
                    LOG.warn("exception when cancel timer, detail:{}", 
                            ExceptionUtils.exception2detail(e));
                }
            }
        }
    }
    
	@Override
	public void setEventHandler(final EventHandler handler) throws Exception {
		this._handler = handler;
	}

	@Override
	public void setEventName(final String event) throws Exception {
		this._event = event;
	}
	
	@Override
	public void afterEventReceiverCreated(final EventReceiver receiver) throws Exception {
		this._receiver = receiver;
    	if (!this._lifecycleSupport.isEmpty()) {
			this._lifecycleSupport.foreachComponent(
				new Visitor<FlowLifecycleListener<FLOW>>() {
					@SuppressWarnings("unchecked")
					@Override
					public void visit(final FlowLifecycleListener<FLOW> lifecycle) throws Exception {
						lifecycle.afterEventReceiverCreated((FLOW)AbstractFlow.this, receiver);
					}});
    	}
	}
	
	@Override
	public void afterFlowDestroy() throws Exception {
    	if (!this._lifecycleSupport.isEmpty()) {
			this._lifecycleSupport.foreachComponent(
				new Visitor<FlowLifecycleListener<FLOW>>() {
					@SuppressWarnings("unchecked")
					@Override
					public void visit(final FlowLifecycleListener<FLOW> lifecycle) throws Exception {
						lifecycle.afterFlowDestroy((FLOW)AbstractFlow.this);
					}});
    	}
	}
	
	@SuppressWarnings("unchecked")
	public FLOW addFlowStateChangedListener(
			final FlowStateChangedListener<FLOW, ? extends EventHandler> listener) {
		this._stateChangedSupport.addComponent((FlowStateChangedListener<FLOW, EventHandler>) listener);
		return (FLOW)this;
	}
	
	@SuppressWarnings("unchecked")
	public FLOW removeFlowStateChangedListener(
			final FlowStateChangedListener<FLOW, ? extends EventHandler> listener) {
		this._stateChangedSupport.removeComponent((FlowStateChangedListener<FLOW, EventHandler>) listener);
		return (FLOW)this;
	}
	
    @Override
	public void onStateChanged(
			final FLOW			flow,
			final EventHandler 	prev, 
			final EventHandler 	next,
			final String 	causeEvent, 
			final Object[] 	causeArgs) throws Exception {
    	if (!this._stateChangedSupport.isEmpty()) {
	    	this._stateChangedSupport.foreachComponent(
				new Visitor<FlowStateChangedListener<FLOW,  EventHandler>>() {
					@Override
					public void visit(
							final FlowStateChangedListener<FLOW, EventHandler> listener)
							throws Exception {
						listener.onStateChanged(flow, prev, next, causeEvent, causeArgs);
					}});
    	}
    }
	
    @Override
    public void setExectionLoop(final ExectionLoop exectionLoop) {
        this._exectionLoop = exectionLoop;
    }
    
	@Override
	public Object getEndReason() throws Exception {
		return _endreason;
	}

	protected EventReceiver	selfEventReceiver() {
		return	this._receiver;
	}
	
	protected String	currentEvent() {
		return	this._event;
	}
	
	@SuppressWarnings("unchecked")
    protected <T extends EventHandler> T currentEventHandler() {
		return	(T)this._handler;
	}

	protected void 	setEndReason(final Object endreason) {
		this._endreason = endreason;
	}
	
	protected ExectionLoop selfExectionLoop() {
	    return this._exectionLoop;
	}
	
	private String			_event;
	private EventHandler 	_handler;
	private Object 			_endreason;
	private EventReceiver	_receiver;
	private ExectionLoop    _exectionLoop;
	
	private final COWCompositeSupport<FlowLifecycleListener<FLOW>> _lifecycleSupport
		= new COWCompositeSupport<>();
	private final COWCompositeSupport<FlowStateChangedListener<FLOW, EventHandler>> _stateChangedSupport
		= new COWCompositeSupport<>();
	private final ConcurrentMap<Class<?>, Object> _adapters = 
			new ConcurrentHashMap<>();
}
