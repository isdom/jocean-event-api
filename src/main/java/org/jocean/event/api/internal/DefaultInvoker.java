/**
 * 
 */
package org.jocean.event.api.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.jocean.event.api.annotation.OnEvent;
import org.jocean.idiom.ReflectUtils;


/**
 * @author isdom
 *
 */
public class DefaultInvoker implements EventInvoker {
    public static EventInvoker[] invokers(final Object target) {
        if ( null == target ) {
            return null;
        }
        
        final Method[] methods = ReflectUtils.getAnnotationMethodsOf(target.getClass(), OnEvent.class);
        if ( methods.length <= 0 ) {
            return null;
        }
        return (new ArrayList<EventInvoker>() { 
            private static final long serialVersionUID = 1L;
        {
            for ( int idx = 0; idx < methods.length; idx++) {
                this.add(DefaultInvoker.of(target, methods[idx]));
            }
        } }).toArray(new EventInvoker[0]);
    }
    
    public static DefaultInvoker of(final Object target, final String methodName) {
        return of(target, methodName, null);
    }
    
    public static DefaultInvoker of(final Object target, final Method method) {
        return of(target, method, null);
    }
    
    public static DefaultInvoker of(final Object target, final String methodName, 
            final String bindedEvent) {
        if ( null == target ) {
            return null;
        }
        final Method[] methods = target.getClass().getDeclaredMethods();
        for ( Method method : methods ) {
            if ( method.getName().equals(methodName) ) {
                return new DefaultInvoker(target, method, bindedEvent);
            }
        }
        return null;
    }
    
    public static DefaultInvoker of(final Object target, final Method method, 
            final String bindedEvent) {
        if ( null == target || null == method) {
            return null;
        }
        return new DefaultInvoker(target, method, bindedEvent);
    }
    
	private DefaultInvoker(final Object target, final Method method, final String bindedEvent) {
		this._target = target;
		this._method = method;
		this._method.setAccessible(true);
		this._event = ( null != bindedEvent ? bindedEvent 
		        : eventAnnotationByMethod(method));
	}
	
    @SuppressWarnings("unchecked")
	@Override
	public <RET> RET invoke(final Object[] args) throws Exception {
		return (RET)this._method.invoke(this._target, args);
	}
	
	@Override
	public String getBindedEvent() {
	    return this._event;
	}
	
    private static String eventAnnotationByMethod(final Method method) {
        final OnEvent onEvent = method.getAnnotation(OnEvent.class);
        
        return  null != onEvent ? onEvent.event() : null;
    }

	@Override
    public String toString() {
        return "invoker [(" + _target + ")." + _method.getName()
                + "/event(" + _event + ")]";
    }

    private final Object _target;
	private final Method _method;
	private final String _event;
}
