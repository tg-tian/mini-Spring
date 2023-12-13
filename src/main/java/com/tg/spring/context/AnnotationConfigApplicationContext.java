package com.tg.spring.context;

public class AnnotationConfigApplicationContext {
    private Class<?> configClass;


    public AnnotationConfigApplicationContext(Class<?> configClass) {
        this.configClass = configClass;
        //1.扫描所有包路径
        //2.注册
        //3.实例化

    }

    public Object getBean(String beanName){
        return null;
    }


}
