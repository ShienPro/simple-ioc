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
        T instance = (T) beanEntityMap.get(bean);
        if (instance != null) {
            return instance;
        }

        instance = BeanInjector.createBeanInstance(bean);
        BeanInjector.injectArgs(bean, instance);

        // Register bean
        if (instance != null) {
            beanEntityMap.put(bean, instance);
        }

        return instance;
    }

    @Override
    public <T> ObjectFactory<T> geObjectFactory(Bean bean) {
        T instance = getInstance(bean);
        return () -> instance;
    }
}
