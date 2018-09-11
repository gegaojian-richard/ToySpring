package com.gegaojian.main.aop;

/**
 * 函数式接口
 * 其实现类包含了切面的逻辑
 */
@FunctionalInterface
public interface MethodInvocation {
    void invoke();
}
