package me.shienpro.core.impl;

import me.shienpro.bean.Bean;
import me.shienpro.bean.ConstructorArg;
import me.shienpro.bean.InjectArg;
import me.shienpro.core.BeanInitializer;
import me.shienpro.excepiton.ContextInitializationException;
import me.shienpro.utils.InjectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

class BeanInjector {
    @SuppressWarnings("unchecked")
    static <T> T createBean(Bean bean) {
        try {
            Class beanClass = bean.getBeanClass();
            if (bean.getConstructorArgs() == null || bean.getConstructorArgs().isEmpty()) {
                return (T) beanClass.newInstance();
            } else {
                List<ConstructorArg> args = bean.getConstructorArgs();

                Constructor<T> constructor = beanClass.getConstructor(
                        args.stream()
                                .map(ConstructorArg::getArgClass)
                                .toArray(Class[]::new)
                );

                return constructor.newInstance(
                        args.stream().map(arg -> {
                            Bean refBean = arg.getRefBean();
                            if (refBean == null) {
                                // value type arg
                                return arg.getValue();
                            } else {
                                // ref type arg
                                return BeanInitializer.getBeanInstanceAndRegister(refBean);
                            }
                        }).toArray()
                );
            }
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new ContextInitializationException(e);
        }
    }

    static void injectArgs(Bean bean, Object instance) {
        try {
            if (bean.getInjectArgs() == null || bean.getInjectArgs().isEmpty()) return;

            for (InjectArg injectArg : bean.getInjectArgs()) {
                Field field = bean.getBeanClass().getDeclaredField(injectArg.getName());

                Object property = injectArg.getRefBean() == null ?
                        injectArg.getValue() :
                        BeanInitializer.getBeanInstanceAndRegister(injectArg.getRefBean());

                if (injectArg.isUseSetter()) {
                    InjectUtils.injectBySetter(field, instance, property);
                } else {
                    InjectUtils.inject(field, instance, property);
                }
            }
        } catch (NoSuchFieldException e) {
            throw new ContextInitializationException(e);
        }
    }
}
