package com.gegaojian.main;

import com.gegaojian.main.HelloService;

public class HelloWorldImpl implements HelloService{
    @Override
    public void sayHelloWorld() {
        System.out.println("Hello world!");
    }
}
