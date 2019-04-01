package me.shienpro.core.impl;

import me.shienpro.bean.Bean;
import me.shienpro.core.BeanInitializer;
import me.shienpro.core.ObjectFactory;

import java.util.HashMap;
import java.util.Map;

public class SingletonBeanInitializer implements BeanInitializer {
    private Map<Bean, Object> beanEntityMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getInstance(Bean bean) {
        T entity = (T) beanEntityMap.get(bean);
        if (entity != null) {
            return entity;
        }

        entity = BeanInjector.createBean(bean);
        BeanInjector.injectArgs(bean, entity);

        // Register bean
        if (entity != null) {
            beanEntityMap.put(bean, entity);
        }

        return entity;
    }

    @Override
    public <T> ObjectFactory<T> geObjectFactory(Bean bean) {
        T instance = getInstance(bean);
        return () -> instance;
    }
}
