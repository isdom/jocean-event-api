jocean-event-api
============

jocean's 异步事件库 API

2014-05-22： release 0.0.3 版本：
  1、 使用 buildInterfaceAdapter 传入的Class<?> 类型中的接口方法上的 OnEvent 注解获取事件名称

2014-06-11： release 0.0.4 版本：
  1、将 AbstractFlow.fireDelayEventAndPush 变更为 public 方法
  2、http://rdassist.widget-inc.com:65480/browse/CHANNEL-103:改进 AbstractFlow 中的一次性定时器 启动和移除API，将内部保存定时器任务更改为由外部提供Collection<Detachable>来保存定时器任务
  
