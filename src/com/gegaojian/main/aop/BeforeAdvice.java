package com.gegaojian.main.aop;

import java.lang.reflect.Method;

public class BeforeAdvice implements Advice{

    // 目标对象
    private Object bean;

    // 切面逻辑 ()->void
    private MethodInvocation methodInvocation;

    public BeforeAdvice(Object bean, MethodInvocation methodInvocation) {
        this.bean = bean;
        this.methodInvocation = methodInvocation;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        methodInvocation.invoke();
        return method.invoke(bean, args);
    }
}
