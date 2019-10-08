package me.shienpro.core.impl;

import me.shienpro.bean.ArgType;
import me.shienpro.bean.Bean;
import me.shienpro.bean.ConstructorArg;
import me.shienpro.bean.InjectArg;
import me.shienpro.core.Context;
import me.shienpro.excepiton.BeanDoesNotExistException;
import me.shienpro.excepiton.ContextInitializationException;
import me.shienpro.utils.InjectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class BeanInjector {
    private static Context context;

    static void init(Context context) {
        BeanInjector.context = context;
    }

    @SuppressWarnings("unchecked")
    static <T> T createBeanInstance(Bean bean) {
        try {
            Class beanClass = bean.getBeanClass();
            List<ConstructorArg> args = Optional.ofNullable(bean.getConstructorArgs()).orElseGet(ArrayList::new);
            Constructor<T> constructor = beanClass.getDeclaredConstructor(
                    args.stream()
                            .map(ConstructorArg::getArgClass)
                            .toArray(Class[]::new)
            );
            constructor.setAccessible(true);
            return constructor.newInstance(
                    args.stream().map(arg -> {
                        int argType = arg.getArgType();
                        if (argType == ArgType.VALUE) {
                            return arg.getValue();
                        }
                        // ref type arg
                        else {
                            if (arg.getArgType() == ArgType.REF_BEAN_NAME) {
                                return context.getBeanInstance(arg.getRefBeanName());
                            } else if (arg.getArgType() == ArgType.REF_CLASS_NAME) {
                                return context.getBeanInstanceByClassName(arg.getArgClass().getName());
                            }
                            throw new BeanDoesNotExistException(arg.getArgClass());
                        }
                    }).toArray()
            );
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new ContextInitializationException(e);
        }
    }

    static void injectArgs(Bean bean, Object instance) {
        try {
            if (bean.getInjectArgs() == null || bean.getInjectArgs().isEmpty()) return;

            for (InjectArg injectArg : bean.getInjectArgs()) {
                Object property = null;
                int argType = injectArg.getArgType();
                if (argType == ArgType.VALUE) {
                    property = injectArg.getValue();
                }
                // ref type arg
                else {
                    if (injectArg.getArgType() == ArgType.REF_BEAN_NAME) {
                        property = context.getBeanInstance(injectArg.getRefBeanName());
                    } else if (injectArg.getArgType() == ArgType.REF_CLASS_NAME) {
                        property = context.getBeanInstanceByClassName(injectArg.getArgClass().getName());
                    }

                    if (property == null) {
                        throw new BeanDoesNotExistException(injectArg.getName());
                    }
                }

                Field field = bean.getBeanClass().getDeclaredField(injectArg.getName());

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
