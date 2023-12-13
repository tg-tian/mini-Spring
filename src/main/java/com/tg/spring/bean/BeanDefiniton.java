package com.tg.spring.bean;

import lombok.Data;

@Data
public class BeanDefiniton {
    String beanName;
    Class<?> classType;
    String scope;
}
