package org.jocean.event.api.internal;

public interface InterfaceSource {
    public <INTF> INTF queryInterfaceInstance(final Class<INTF> intfCls);
}
