package com.gegaojian.main;

import com.gegaojian.main.aop.Advice;
import com.gegaojian.main.aop.BeforeAdvice;
import com.gegaojian.main.aop.SimpleAOP;
import com.gegaojian.main.ioc.SimpleIOC;

import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception{

	    // 测试IOC
        String location = "/Users/gegaojian/Documents/JavaProjects/ToySpring/src/com/gegaojian/main/ioc.xml";
        SimpleIOC beanFactory = new SimpleIOC(location);
        Wheel wheel = (Wheel) beanFactory.getBean("wheel");
        logger.info(wheel.toString());
        Car car = (Car) beanFactory.getBean("car");
        logger.info(car.toString());

        // 测试AOP
        HelloService helloService = new HelloWorldImpl();
        // 1. 创建一个增强
        Advice beforeAdvice = new BeforeAdvice(helloService, ()->System.out.println("Log"));
        // 2. 生成代理对象并织入增强
        HelloService helloServiceImplProxy = (HelloService) SimpleAOP.getProxy(helloService, beforeAdvice);
        // 3. 通过代理类来访问接口
        helloServiceImplProxy.sayHelloWorld();
    }
}
