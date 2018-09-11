package com.gegaojian.main.aop;

import java.lang.reflect.Proxy;

/**
 * 为指定的目标类生成代理类，并将指定的增强织入到代理类中
 */
public class SimpleAOP {
    public static Object getProxy(Object bean, Advice advice){
        return Proxy.newProxyInstance(SimpleAOP.class.getClassLoader(), bean.getClass().getInterfaces(), advice);
    }
}
