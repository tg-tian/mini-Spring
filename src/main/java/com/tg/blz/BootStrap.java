package com.tg.blz;

import com.tg.spring.context.AnnotationConfigApplicationContext;

public class BootStrap {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        OrderService orderService = (OrderService) context.getBean("orderService");
        orderService.hello();
    }
}
