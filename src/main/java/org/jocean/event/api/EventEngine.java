/**
 * 
 */
package org.jocean.event.api;

import org.jocean.event.api.internal.EventHandler;


/**
 * @author isdom
 * 
 */
public interface EventEngine {

    /**
     * @param name EventReceiver 的名称，用于日志输出的调试
     * @param init 初始状态
     * @param reactors 0~N个反应器实例, 反应器可以为: 
     *  EventNameAware, 
		EventHandlerAware, 
		EndReasonProvider,
		ExectionLoopAware,
		FlowLifecycleListener, 
		FlowStateChangedListener
     * @return
     * @see EventNameAware
     * @see EventHandlerAware
     * @see EndReasonProvider
     * @see ExectionLoopAware
     * @see FlowLifecycleListener
     * @see FlowStateChangedListener
     */
    public EventReceiver create(
    		final String name, final EventHandler init, final Object... reactors);
}
