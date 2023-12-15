package com.tg.spring.context;

import com.tg.blz.AppConfig;
import com.tg.spring.annotation.Autowired;
import com.tg.spring.annotation.Component;
import com.tg.spring.annotation.ComponentScan;
import com.tg.spring.annotation.Scope;
import com.tg.spring.bean.BeanDefiniton;
import com.tg.spring.bean.BeanPostProcessor;
import com.tg.spring.bean.InitializingBean;
import com.tg.spring.bean.ScopeType;
import com.tg.spring.exception.BeansException;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

public class AnnotationConfigApplicationContext {
    private Class<?> configClass;

    private Map<String,BeanDefiniton> beanDefinitionMap = new HashMap<>(16);

    private Map<String,Object> singletonObjects = new HashMap<>(16);

    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public AnnotationConfigApplicationContext(Class<?> configClass) {
        this.configClass = configClass;
        //1.扫描所有包路径
        //2.注册
        doScan();
        //3.实例化
        initializeNotLazyBean();
    }

    public void doScan() {
        if (!configClass.isAnnotationPresent(ComponentScan.class)) {
            return;
        }
        String packageName = configClass.getAnnotation(ComponentScan.class).value();
        String path = packageName.replace(".","/");

        URL resource = this.getClass().getClassLoader().getResource(path);
        File file = new File(resource.getFile());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                String className  = packageName + "." + file1.getName().replace(".class","");
                try {
                    Class<?> aClass = Class.forName(className);
                    if (aClass.isAnnotationPresent(Component.class)) {
                        Component component = aClass.getAnnotation(Component.class);
                        String beanName = component.value();
                        if (component.value() .equals("")) {
                            beanName = Introspector.decapitalize(aClass.getSimpleName());
                        }

                        String scope = ScopeType.SINGLETON;
                        if(aClass.isAnnotationPresent(Scope.class)){
                            scope =  aClass.getAnnotation(Scope.class).value();
                        }

                        BeanDefiniton beanDefiniton = new BeanDefiniton();
                        beanDefiniton.setBeanName(beanName);
                        beanDefiniton.setClassType(aClass);
                        beanDefiniton.setScope(scope);
                        this.beanDefinitionMap.put(beanName,beanDefiniton);
                        if (BeanPostProcessor.class.isAssignableFrom(aClass)) {
                            beanPostProcessorList.add(((BeanPostProcessor) getBean(beanName)));
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }



    }
    private void initializeNotLazyBean() {
        for (Map.Entry<String, BeanDefiniton> definitonEntry : beanDefinitionMap.entrySet()) {
            BeanDefiniton beanDefiniton = definitonEntry.getValue();
            if (beanDefiniton.getScope().equals(ScopeType.SINGLETON)) {

                createBean(beanDefiniton);

            }
        }

    }
    private Object createBean(BeanDefiniton beanDefiniton) {


        String beanName = beanDefiniton.getBeanName();
        if (singletonObjects.containsKey(beanName)) {
            return singletonObjects.get(beanName);
        }

        Object bean = doCreateBean(beanDefiniton);

        singletonObjects.put(beanName,bean);

        return bean;
    }

    private Object doCreateBean(BeanDefiniton beanDefiniton) {
        Object exposedObject = null;
        Class<?> classType = beanDefiniton.getClassType();
        try {
            Object instance = classType.getDeclaredConstructor().newInstance();

            populateBean(instance,beanDefiniton);

            instance = initializing(instance,beanDefiniton);

            exposedObject = instance;

            return exposedObject;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BeansException(beanDefiniton.getBeanName() + "创建异常");
        }
    }

    private void populateBean(Object instance, BeanDefiniton beanDefiniton) {

        Class<?> classType = beanDefiniton.getClassType();

        Field[] declaredFields = classType.getDeclaredFields();
        try {
            for (Field declaredField : declaredFields) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    String simpleName = declaredField.getType().getSimpleName();
                    String fieldBeanName = Introspector.decapitalize(simpleName);
                    declaredField.setAccessible(true);
                    declaredField.set(instance, getBean(fieldBeanName));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new BeansException(beanDefiniton.getBeanName() + "创建异常");
        }


    }


    private Object initializing(Object instance, BeanDefiniton beanDefiniton) throws Exception {

        Object rs = postProcessBeforeInitialization(instance,beanDefiniton);
        if (rs != null) {
            return rs;
        }

        if (instance instanceof InitializingBean) {
            ((InitializingBean) instance).afterPropertiesSet();
        }

        rs = postProcessAfterInitialization(instance,beanDefiniton);
        if (rs != null) {
            return rs;
        }

        return instance;
    }

    private Object postProcessBeforeInitialization(Object instance, BeanDefiniton beanDefiniton) {
        if (instance instanceof BeanPostProcessor) {
            return instance;
        }

        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            Object o = beanPostProcessor.postProcessBeforeInitialization(instance, beanDefiniton.getBeanName());
            if (o != null) {
                return o;
            }
        }
        return null;
    }

    private Object postProcessAfterInitialization(Object instance, BeanDefiniton beanDefiniton) {
        if (instance instanceof BeanPostProcessor) {
            return instance;
        }

        for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
            Object o = beanPostProcessor.postProcessAfterInitialization(instance, beanDefiniton.getBeanName());
            if (o != null) {
                return o;
            }
        }
        return null;
    }





    public Object getBean(String beanName){
        if (!beanDefinitionMap.containsKey(beanName)) {
            throw new BeansException("找不到" + beanName);
        }
        BeanDefiniton beanDefiniton = beanDefinitionMap.get(beanName);
        if (beanDefiniton.getScope().equals(ScopeType.SINGLETON)) {
           return createBean((beanDefiniton));
        }else{
            return doCreateBean(beanDefiniton);
        }

    }



}
