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
public @interface GuardPaired {
	public abstract boolean value() default true;
	public abstract String[] paired();
}
