/**
 * 
 */
package org.jocean.event.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.jocean.event.api.annotation.GuardPaired;
import org.jocean.event.api.annotation.GuardReferenceCounted;
import org.jocean.event.api.annotation.OnEvent;
import org.jocean.event.api.internal.Eventable;
import org.jocean.idiom.ExceptionUtils;
import org.jocean.idiom.Function;
import org.jocean.idiom.Pair;
import org.jocean.idiom.PairedVisitor;
import org.jocean.idiom.ReflectUtils;
import org.jocean.idiom.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observer;

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
            public boolean acceptEvent(final String event, final Object... args) {
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
            public boolean acceptEvent(final Eventable eventable, final Object... args) {
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
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = intf.getClassLoader();
        }
        return (INTF) Proxy.newProxyInstance(cl,
                new Class<?>[]{intf, EventReceiver.class},
                new ReceiverAdapterHandler(intf, receiver));
    }

    private static final class ReceiverAdapterHandler implements
            InvocationHandler {
        private static SimpleCache<Pair<Class<Object>, Method>, Object> _method2eventable = 
                new SimpleCache<Pair<Class<Object>, Method>, Object>(
            new Function<Pair<Class<Object>, Method>, Object>() {
            @Override
            public Object apply(final Pair<Class<Object>, Method> input) {
                final Method[] methods = input.first.getDeclaredMethods();
                for ( Method m : methods ) {
                    if ( m.getName().equals(input.second.getName()) ) {
                        return safeGetEventOf(m);
                    }
                }
                return safeGetEventOf(input.second);
            }

            private Object safeGetEventOf(final Method method) {
                final OnEvent onevent = method.getAnnotation(OnEvent.class);
                final String event = (null != onevent) ? onevent.event() : method.getName();
                final GuardPaired guardPaired = method.getAnnotation(GuardPaired.class);
                if ( null != guardPaired 
                     && guardPaired.value() ) {
                    return new PairedGuardEventable(generatePaired(guardPaired.paired()), event);
                }
                final GuardReferenceCounted guardRefcounted = method.getAnnotation(GuardReferenceCounted.class);
                if ( null != guardRefcounted 
                     && guardRefcounted.value() ) {
                    return new RefcountedGuardEventable(event);
                }
                else {
                    return event;
                }
            }

            @SuppressWarnings("unchecked")
            private PairedVisitor<Object> generatePaired(final String[] textPaireds) {
                if (textPaireds.length == 1) {
                    return generatePaired(textPaireds[0]);
                }
                else {
                    final List<PairedVisitor<Object>> paireds = new ArrayList<PairedVisitor<Object>>() {
                        private static final long serialVersionUID = 1L;
                        {
                            for (String text : textPaireds) {
                                final PairedVisitor<Object> paired = generatePaired(text);
                                if (null != paired) {
                                    this.add(paired);
                                }
                            }
                        }
                    };
                    return !paireds.isEmpty()
                            ? PairedVisitor.Utils.composite(paireds.toArray(new PairedVisitor[0])) 
                            : null;
                }
            }
            
            private PairedVisitor<Object> generatePaired(final String textPaired) {
                if (null == textPaired) {
                    throw new RuntimeException("null paired text.");
                }
                final PairedVisitor<Object> paired = ReflectUtils.getStaticFieldValue(textPaired);
                if ( null != paired ) {
                    return paired;
                }
                throw new RuntimeException("invalid paired text:(" + textPaired + ")");
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
            
            if ( method.getDeclaringClass().equals(EventReceiver.class) ) {
//                public boolean acceptEvent(final String event, final Object... args);
//                public boolean acceptEvent(final Eventable eventable, final Object... args);
                return method.invoke(this._receiver, args);
            }
            
            final Object eventable = 
                    _method2eventable.get(Pair.of(this._cls, method));
            
            boolean isAccepted = (eventable instanceof Eventable) 
                    ? _receiver.acceptEvent((Eventable)eventable, args)
                    : _receiver.acceptEvent(eventable.toString(), args);
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

    @SuppressWarnings("unchecked")
    public static <T> Observer<? super T> receiver2observer(
            final EventReceiver receiver,
            final String ...events
            ) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = Observer.class.getClassLoader();
        }
        return (Observer<? super T>) Proxy.newProxyInstance(cl,
                new Class<?>[]{Observer.class},
                new ObserverHandler(receiver, events));
    }
    
    private static final class ObserverHandler implements InvocationHandler {
        ObserverHandler(final EventReceiver receiver, 
                final String[] events) {
            if (null == receiver) {
                throw new NullPointerException("EventReceiver can't be null");
            }
            this._receiver = receiver;
            this._onNext = safeGetEvent(events, 0);
            this._onError = safeGetEvent(events, 1);
            this._onCompleted = safeGetEvent(events, 2);
        }

        private static String safeGetEvent(final String[] events, int idx) {
            return null != events ? (idx < events.length ? events[idx] : null ) : null;
        }

        @Override
        public Object invoke(final Object proxy, final Method method,
                final Object[] args) throws Throwable {
            // An invocation of the hashCode, equals, or toString methods
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

            if (method.getName().equals("onNext") && null != _onNext) {
                _receiver.acceptEvent(_onNext, args);
            } 
            else if (method.getName().equals("onError") && null != _onError) {
                _receiver.acceptEvent(_onError, args);
            }
            else if (method.getName().equals("onCompleted") && null != _onCompleted) {
                _receiver.acceptEvent(_onCompleted, args);
            }
            return null;
        }

        private final EventReceiver _receiver;
        private final String _onNext;
        private final String _onError;
        private final String _onCompleted;
    }
}
