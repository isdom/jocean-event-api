package org.jocean.event.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class EventUtilsTest {

    public static interface DemoIntf {
        public void func1(final String s);
    }
    
    public static class Demo implements DemoIntf {
        public void func1(final String s) {
            System.out.print("hello," + s);
        }
    }
    
    public static interface DemoIntf2 extends DemoIntf {
        public void func2(final String s);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        DemoIntf2 demo = (DemoIntf2) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(), new Class<?>[] { DemoIntf2.class },
                new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method,
                            Object[] args) throws Throwable {
                        if ( method.getDeclaringClass().equals( DemoIntf.class) ) {
                            return method.invoke(new Demo(), args);
                        }
                        else {
                            throw new RuntimeException("not support");
                        }
                    }} );
        demo.func1("world");

    }

}
