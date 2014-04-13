/**
 * 
 */
package org.jocean.event.api;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jocean.event.api.internal.EventHandler;
import org.jocean.event.api.internal.EventInvoker;
import org.jocean.idiom.Detachable;
import org.jocean.idiom.ExceptionUtils;
import org.jocean.idiom.ExectionLoop;
import org.jocean.idiom.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author isdom
 *
 */
public class BizStep implements Cloneable, EventHandler {

	private static final Logger LOG = 
        	LoggerFactory.getLogger(BizStep.class);
    
    @Override
	protected BizStep clone() {
		final BizStep cloned = new BizStep( this._name );
		
		cloned._handlers.putAll( this._handlers );
    	
    	return	cloned;
	}

	public BizStep(final String name) {
    	this._name = name;
    }
    
	public BizStep rename(final String name) {
		if ( !name.equals(this._name) ) {
			if ( !this._isFrozen ) {
				this._name = name;
				return	this;
			}
			else {
				return	this.clone().rename(name);
			}
		}
		else {
			return	this;
		}
	}
	
    public BizStep handler(final EventInvoker eventInvoker) {
    	if ( null == eventInvoker ) {
	    	LOG.warn("add handler failed, invoker is null.");
    		return	this;
    	}
    	
    	if ( !this._isFrozen ) {
    		addHandler(eventInvoker);
    		return this;
    	}
    	else {
    		return	this.clone().handler(eventInvoker);
    	}
    }

    /**
     * @param eventInvoker
     * @return
     */
    private void addHandler(final EventInvoker eventInvoker) {
        final String bindedEvent = eventInvoker.getBindedEvent();
        if ( null != bindedEvent ) {
        	this._handlers.put(bindedEvent, eventInvoker);
        }
        else {
        	LOG.warn("add handler failed for {}, no binded event.", eventInvoker);
        }
    }

    private boolean removeHandlerOf(final String event) {
        return (this._handlers.remove(event) != null);
    }
    
    public BizStep freeze() {
        this._isFrozen = true;
    	return	this;
    }
    
    private final class DelayEventImpl implements DelayEvent {

        DelayEventImpl(final EventInvoker eventInvoker) {
            this._invoker = eventInvoker;
        }
        
        @Override
        public DelayEvent args(final Object... args) {
            this._args = args;
            return this;
        }

        @Override
        public DelayEvent delayMillis(final long delayMillis) {
            this._delayMillis = delayMillis;
            return this;
        }

        @Override
        public Detachable fireWith(final ExectionLoop exectionLoop,
                final EventReceiver receiver) {
            final String event = UUID.randomUUID().toString();
            final Object[] args = this._args;
            
            BizStep.this.addHandler(new EventInvoker() {
                @Override
                public String toString() {
                    return _invoker.toString();
                }
                @Override
                public <RET> RET invoke(Object[] args) throws Exception {
                    return _invoker.invoke(args);
                }
                @Override
                public String getBindedEvent() {
                    return event;
                }} );
            
            final Detachable cancel = exectionLoop.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        receiver.acceptEvent(event, args);
                    } catch (Exception e) {
                        LOG.error("exception when acceptEvent for event {}, detail: {}", 
                                event, ExceptionUtils.exception2detail(e));
                    }
                }}, this._delayMillis);
            
            return new Detachable() {

                @Override
                public void detach() throws Exception {
                    if ( LOG.isDebugEnabled() ) {
                        LOG.debug("cancel delay event {}/{}", event, _invoker);
                    }
                    try {
                        BizStep.this.removeHandlerOf(event);
                        cancel.detach();
                    }
                    catch (Exception e) {
                        LOG.warn("exception when cancel delay event {}, detail:{}",
                                event, ExceptionUtils.exception2detail(e));
                    }
                }};
        }

        @SuppressWarnings("unchecked")
        @Override
        public BizStep owner() {
            return BizStep.this;
        }
        
        private Object[] _args = null;
        private long _delayMillis;
        private final EventInvoker _invoker;
    }
    
    public DelayEvent delayEvent(final EventInvoker eventInvoker) {
        if ( !this._isFrozen ) {
            return new DelayEventImpl(eventInvoker);
        }
        else {
            return this.clone().delayEvent(eventInvoker);
        }
    }
    
    private final Map<String, EventInvoker> _handlers = 
    		new ConcurrentHashMap<String, EventInvoker>();

    private volatile String _name;
    
    private boolean _isFrozen = false;

    //	implements EventHandler
	@Override
	public String getName() {
		return this._name;
	}

	@Override
	public Pair<EventHandler, Boolean> process(final String event, final Object[] args) {
		try {
			final EventInvoker eventInvoker = this._handlers.get(event);
			
			if ( null != eventInvoker ) {
				return Pair.of((EventHandler)eventInvoker.invoke(args), true);
			}
			else {
			    if ( LOG.isDebugEnabled() ) {
    				LOG.debug("BizStep [{}] don't except event {} , just ignore", 
    						this._name, event);
			    }
				//	do not change state
				return Pair.of((EventHandler)this, false);
			}
		}
		catch (Exception e) {
			LOG.error("exception when process event {}, detail:{}", 
					event, ExceptionUtils.exception2detail(e));
			return Pair.of((EventHandler)this, false);
		}
	}
}
