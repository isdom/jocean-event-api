jocean-event-api
============

jocean's 异步事件库 API

2014-09-30： release 0.0.6 版本：
  1、添加异常EventUnhandleException，用于在事件处理方法中，不再进行当前事件处理的情况下，则可以抛出该异常。如果当前事件是通过实现了EventUnhandleAware接口的Eventable发射的，则事件框架会在稍后调用其对应的onEventUnhandle接口方法
  2、在EventUtils.buildInterfaceAdapter 实现中，处理Thread.currentThread().getContextClassLoader() 返回为 null 的情况，直接使用 intf.getClassLoader()。
  
2014-08-19： release 0.0.5 版本：
  1、对通过 buildInterfaceAdapter 产生的对象，默认实现 EventReceiver接口，并在对应接口方法被调用时，委托给 成员变量 receiver 执行
  2、增加自定义注解 GuardReferenceCounted ，并在 EventUtils.buildInterfaceAdapter中根据对应的 接口方法是是否有 GuardReferenceCounted(value=true) 注解，来选择 acceptEvent中传入的事件参数为简单字符串类型，还是 RefcountedGuardEventable 事件对象。
  3、添加 RefcountedGuardEventable 实现 对于单个事件指定 需要在事件送达处理单元后，对于实现了ReferenceCounted 接口的参数，立刻调用 retain 保护参数，并在事件处理完成或被Unhandle之前，调用 相关参数的release 方式进行资源释放
  4、添加 OnDelayed 注解，用来标示用于 delay 事件的处理方法，BizStep 在构造函数中，也会扫描并注册 OnDelayed标注的处理方法
  5、在 BizStep 构造函数中，检查自身是否有EventInvoker，并添加到 _handlers 中
  6、在 BizStep 中增加 delayed 方法，预定义延时事件处理方法。并增加 makePredefineDelayEvent用来根据预设的延时事件处理方法产生延时事件，注意：如果有多个预设延时事件处理方法，则需确保事件参数相同

2014-06-11： release 0.0.4 版本：
  1、将 AbstractFlow.fireDelayEventAndPush 变更为 public 方法
  2、http://rdassist.widget-inc.com:65480/browse/CHANNEL-103:改进 AbstractFlow 中的一次性定时器 启动和移除API，将内部保存定时器任务更改为由外部提供Collection<Detachable>来保存定时器任务

2014-05-22： release 0.0.3 版本：
  1、 使用 buildInterfaceAdapter 传入的Class<?> 类型中的接口方法上的 OnEvent 注解获取事件名称
