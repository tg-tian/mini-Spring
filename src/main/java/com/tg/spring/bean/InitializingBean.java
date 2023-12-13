package com.tg.spring.bean;

public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}
