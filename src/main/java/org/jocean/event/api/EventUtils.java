/**
 * 
 */
package org.jocean.event.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.jocean.event.api.annotation.OnEvent;
import org.jocean.event.api.internal.Eventable;
import org.jocean.idiom.ExceptionUtils;
import org.jocean.idiom.Function;
import org.jocean.idiom.Pair;
import org.jocean.idiom.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author isdom
 * 
 */
public class EventUtils {

    private static final Logger LOG = LoggerFactory
            .getLogger(EventUtils.class);

    public static EventReceiver combineEventReceivers(
            final EventReceiver... receivers) {
        return new EventReceiver() {

            @Override
            public boolean acceptEvent(final String event, final Object... args)
                    throws Exception {
                boolean handled = false;
                for (EventReceiver receiver : receivers) {
                    try {
                        if (receiver.acceptEvent(event, args)) {
                            handled = true;
                        }
                    } catch (final Exception e) {
                        LOG.error("failed to acceptEvent event:({}), detail: {}",
                                event, ExceptionUtils.exception2detail(e));
                    }
                }
                return handled;
            }

            @Override
            public boolean acceptEvent(final Eventable eventable, final Object... args)
                    throws Exception {
                boolean handled = false;
                for (EventReceiver receiver : receivers) {
                    try {
                        if (receiver.acceptEvent(eventable, args)) {
                            handled = true;
                        }
                    } catch (final Exception e) {
                        LOG.error("failed to acceptEvent event:({}), detail: {}",
                                eventable.event(), ExceptionUtils.exception2detail(e));
                    }
                }
                return handled;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <INTF> INTF buildInterfaceAdapter(final Class<INTF> intf,
            final EventReceiver receiver) {
        return (INTF) Proxy.newProxyInstance(Thread.currentThread()
                .getContextClassLoader(), new Class<?>[] { intf },
                new ReceiverAdapterHandler(intf, receiver));
    }

    private static final class ReceiverAdapterHandler implements
            InvocationHandler {
        private static SimpleCache<Pair<Class<Object>, Method>, String> _method2event = 
                new SimpleCache<Pair<Class<Object>, Method>, String>(
            new Function<Pair<Class<Object>, Method>, String>() {
            @Override
            public String apply(final Pair<Class<Object>, Method> input) {
                final Method[] methods = input.first.getDeclaredMethods();
                for ( Method m : methods ) {
                    if ( m.getName().equals(input.second.getName()) ) {
                        return safeGetEventOf(m);
                    }
                }
                return safeGetEventOf(input.second);
            }

            private String safeGetEventOf(final Method method) {
                final OnEvent onevent = method.getAnnotation(OnEvent.class);
                return (null != onevent) ? onevent.event() : method.getName();
            }});
        
        @SuppressWarnings("unchecked")
        ReceiverAdapterHandler(final Class<?> intf, final EventReceiver receiver) {
            if (null == receiver) {
                throw new NullPointerException("EventReceiver can't be null");
            }
            this._cls = (Class<Object>)intf;
            this._receiver = receiver;
        }

        @Override
        public Object invoke(final Object proxy, final Method method,
                final Object[] args) throws Throwable {
            //   An invocation of the hashCode, equals, or toString methods
            // declared in java.lang.Object on a proxy instance will be 
            // encoded and dispatched to the invocation handler's invoke
            // method in the same manner as interface method invocations are
            // encoded and dispatched, as described above. The declaring 
            // class of the Method object passed to invoke will be
            // java.lang.Object. Other public methods of a proxy instance
            // inherited from java.lang.Object are not overridden by a proxy
            // class, so invocations of those methods behave like they do
            // for instances of java.lang.Object.
            if (method.getName().equals("hashCode")) {
                return this._receiver.hashCode();
            } else if (method.getName().equals("equals")) {
                return (proxy == args[0]);
            } else if (method.getName().equals("toString")) {
                return this._receiver.toString();
            }
            
            final String eventName = 
                    _method2event.get(Pair.of(this._cls, method));
            
            boolean isAccepted = _receiver.acceptEvent(eventName, args);
            if (method.getReturnType().equals(Boolean.class)
                    || method.getReturnType().equals(boolean.class)) {
                return isAccepted;
            } else {
                return null;
            }
        }

        private final EventReceiver _receiver;
        private final Class<Object> _cls;
    }
}
