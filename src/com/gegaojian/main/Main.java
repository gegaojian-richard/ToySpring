package com.gegaojian.main;

import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception{

	// write your code here
        String location = "/Users/gegaojian/Documents/JavaProjects/ToySpring/src/com/gegaojian/main/ioc.xml";
        SimpleIOC beanFactory = new SimpleIOC(location);
        Wheel wheel = (Wheel) beanFactory.getBean("wheel");
        logger.info(wheel.toString());
        Car car = (Car) beanFactory.getBean("car");
        logger.info(car.toString());
        logger.info(beanFactory.getBean(("car")).toString());
    }
}
