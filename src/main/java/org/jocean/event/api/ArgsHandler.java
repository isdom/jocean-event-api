/**
 * 
 */
package org.jocean.event.api;

import org.jocean.idiom.ReferenceCounted;

/**
 * @author isdom
 *
 */
public interface ArgsHandler {
	
	public Object[] beforeAcceptEvent(final Object[] args) throws Exception;
	
	public void afterAcceptEvent(final Object[] args) throws Exception;

	final class Consts {
        public static final ArgsHandler _REFCOUNTED_ARGS_GUARD = new ArgsHandler() {
    
            @Override
            public Object[] beforeAcceptEvent(final Object[] args) {
                if ( null != args ) {
                    for ( Object arg : args) {
                        if ( arg instanceof ReferenceCounted ) {
                            ((ReferenceCounted<?>)arg).retain();
                        }
                    }
                }
                return args;
            }
    
            @Override
            public void afterAcceptEvent(final Object[] args) {
                if ( null != args ) {
                    for ( Object arg : args) {
                        if ( arg instanceof ReferenceCounted ) {
                            ((ReferenceCounted<?>)arg).release();
                        }
                    }
                }
            }};
	}
}
