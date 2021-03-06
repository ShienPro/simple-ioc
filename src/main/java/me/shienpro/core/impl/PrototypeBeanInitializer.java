package me.shienpro.core.impl;

import me.shienpro.bean.Bean;
import me.shienpro.core.BeanInitializer;
import me.shienpro.core.ObjectFactory;

import java.util.HashMap;
import java.util.Map;

public class PrototypeBeanInitializer implements BeanInitializer {
    private final Map<Bean, ObjectFactory<?>> beanObjectFactoryMap = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Bean bean) {
        return (T) geObjectFactory(bean).getObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ObjectFactory<T> geObjectFactory(Bean bean) {
        ObjectFactory<T> of = (ObjectFactory<T>) beanObjectFactoryMap.get(bean);
        if (of != null) {
            return of;
        }

        of = createObjectFactory(bean);

        // register object factory
        if (of.getObject() != null) {
            beanObjectFactoryMap.put(bean, of);
        }

        return of;
    }

    private <T> ObjectFactory<T> createObjectFactory(Bean bean) {
        return () -> {
            T instance = BeanInjector.createBeanInstance(bean);
            BeanInjector.injectArgs(bean, instance);
            return instance;
        };
    }
}
