package me.shienpro.core;

import me.shienpro.bean.Bean;
import me.shienpro.core.impl.PrototypeBeanInitializer;
import me.shienpro.core.impl.SingletonBeanInitializer;

public interface BeanInitializer {
    SingletonBeanInitializer SINGLETON_BEAN_INITIALIZER = new SingletonBeanInitializer();
    PrototypeBeanInitializer PROTOTYPE_BEAN_INITIALIZER = new PrototypeBeanInitializer();

    <T> T getInstance(Bean bean);

    <T> ObjectFactory<T> geObjectFactory(Bean bean);

    @SuppressWarnings("unchecked")
    static <T> T getBeanInstance(Bean bean) {
        BeanInitializer beanInitializer =
                bean.getScope() == Bean.SINGLETON ?
                        BeanInitializer.SINGLETON_BEAN_INITIALIZER :
                        BeanInitializer.PROTOTYPE_BEAN_INITIALIZER;
        return (T) beanInitializer.geObjectFactory(bean).getObject();
    }
}
