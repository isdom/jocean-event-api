/**
 * 
 */
package org.jocean.event.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author isdom
 *
 */
@Retention(RetentionPolicy.RUNTIME) 
public @interface GuardReferenceCounted {
	public abstract boolean value() default true;
}
