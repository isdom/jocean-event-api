/**
 * 
 */
package org.jocean.event.api;

import org.jocean.event.api.internal.Eventable;
import org.jocean.idiom.ArgsHandler;

/**
 * @author isdom
 *
 */
public class RefcountedGuardEventable extends ArgsHandler.Consts.RefcountedArgsGuard implements Eventable {

    public RefcountedGuardEventable(final String event) {
        this._event = event;
    }
    
    @Override
    public String event() {
        return this._event;
    }

    private final String _event;
}
