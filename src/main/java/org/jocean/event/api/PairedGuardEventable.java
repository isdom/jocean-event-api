/**
 * 
 */
package org.jocean.event.api;

import org.jocean.event.api.internal.Eventable;
import org.jocean.idiom.ArgsHandler;
import org.jocean.idiom.PairedVisitor;

/**
 * @author isdom
 *
 */
public class PairedGuardEventable extends ArgsHandler.Consts.PairedArgsGuard implements Eventable {

    public PairedGuardEventable(final PairedVisitor<Object> paired, final String event) {
        super(paired);
        this._event = event;
    }
    
    @Override
    public String event() {
        return this._event;
    }

    private final String _event;
}
