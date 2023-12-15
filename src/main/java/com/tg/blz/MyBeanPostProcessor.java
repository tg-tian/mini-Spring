package com.tg.blz;

import com.tg.spring.annotation.Component;
import com.tg.spring.bean.BeanPostProcessor;
import com.tg.spring.exception.BeansException;

@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof OrderService) {
            System.out.println(beanName + "BeforeInitialization");
        }
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof OrderService) {
            System.out.println(beanName + "AfterInitialization");
        }
        return null;
    }
}
