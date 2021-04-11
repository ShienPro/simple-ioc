package me.shienpro.core.impl;

import me.shienpro.bean.Bean;
import me.shienpro.core.BeanInitializer;
import me.shienpro.core.ObjectFactory;

import java.util.HashMap;
import java.util.Map;

public class SingletonBeanInitializer implements BeanInitializer {
    private final Map<Bean, Object> beanEntityMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getInstance(Bean bean) {
        T instance = (T) beanEntityMap.get(bean);
        if (instance != null) {
            return instance;
        }

        instance = BeanInjector.createBeanInstance(bean);

        // register bean
        beanEntityMap.put(bean, instance);
        BeanInjector.injectArgs(bean, instance);

        return instance;
    }

    @Override
    public <T> ObjectFactory<T> geObjectFactory(Bean bean) {
        T instance = getInstance(bean);
        return () -> instance;
    }
}
