package me.shienpro.core;

import me.shienpro.bean.Bean;
import me.shienpro.bean.ConstructorArg;
import me.shienpro.bean.InjectArg;
import me.shienpro.excepiton.BeanNameRepeatException;
import me.shienpro.excepiton.CanNotFindBeanException;
import me.shienpro.excepiton.ContextInitializationException;
import me.shienpro.excepiton.MultipleBeanException;
import me.shienpro.utils.InjectUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextImpl implements Context {
    private Map<String, Object> beanNameMap = new HashMap<>();
    private Map<String, List<Object>> classNameBeanMap = new HashMap<>();
    private Map<Class, List<Object>> classBeanMap = new HashMap<>();

    private Map<Bean, Object> beanEntityMap = new HashMap<>();

    public ContextImpl(BeanLoader loader) {
        List<Bean> beans = loader.load();
        beans.forEach(this::registerBean);
    }

    private synchronized void registerBean(Bean bean) {
        if (beanEntityMap.get(bean) != null) return;

        try {
            Object instance = getInstance(bean);
            injectArgs(bean, instance);
            register(bean.getBeanName(), instance);
            beanEntityMap.put(bean, instance);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | NoSuchFieldException | InvocationTargetException e) {
            throw new ContextInitializationException(e);
        }
    }

    private Object getInstance(Bean bean) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class<?> beanClass = bean.getBeanClass();
        if (CollectionUtils.isEmpty(bean.getConstructorArgs())) {
            return beanClass.newInstance();
        }

        List<ConstructorArg> args = bean.getConstructorArgs();
        Constructor<?> constructor = beanClass.getConstructor(args.stream()
                .map(ConstructorArg::getArgClass).toArray(Class[]::new));
        return constructor.newInstance(args.stream().map(arg -> {
            if (arg.getRefBean() == null) return arg.getValue();
            else return getBeanObject(arg.getRefBean());
        }).toArray());
    }

    private void injectArgs(Bean bean, Object instance) throws NoSuchFieldException {
        if (CollectionUtils.isEmpty(bean.getInjectArgs())) return;

        for (InjectArg injectArg : bean.getInjectArgs()) {
            Field field = bean.getBeanClass().getDeclaredField(injectArg.getName());
            Object property = injectArg.getRefBean() == null ?
                    injectArg.getValue() : getBeanObject(injectArg.getRefBean());
            inject(field, instance, property, injectArg.isUseSetter());
        }
    }

    private Object getBeanObject(Bean bean) {
        Object entity = beanEntityMap.get(bean);
        if (entity != null) return entity;
        registerBean(bean);
        return beanEntityMap.get(bean);
    }

    private void inject(Field field, Object target, Object property, boolean useSetter) {
        if (useSetter) {
            InjectUtils.injectBySetter(field, target, property);
        } else {
            InjectUtils.inject(field, target, property);
        }
    }

    private void register(String beanName, Object bean) {
        Object oldBean = beanNameMap.get(beanName);
        if (oldBean != null) throw new BeanNameRepeatException(beanName);

        beanNameMap.put(beanName, bean);

        // TODO register super class and interface
        registerByClassName(bean.getClass().getName(), bean);
        registerByClass(bean.getClass(), bean);
    }

    private void registerByClassName(String className, Object bean) {
        classNameBeanMap.computeIfAbsent(className, k -> new ArrayList<>()).add(bean);
    }

    private void registerByClass(Class beanClass, Object bean) {
        classBeanMap.computeIfAbsent(beanClass, k -> new ArrayList<>()).add(bean);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(String beanName) {
        Object obj = beanNameMap.get(beanName);
        if (obj == null) throw CanNotFindBeanException.ofBeanName(beanName);
        return (T) obj;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> beanClass) {
        List<Object> list = classBeanMap.get(beanClass);
        if (CollectionUtils.isEmpty(list)) throw CanNotFindBeanException.of(beanClass);
        if (list.size() > 1) throw MultipleBeanException.of(beanClass);
        Object obj = list.get(0);
        return (T) obj;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBeanByClassName(String className) {
        List<Object> list = classNameBeanMap.get(className);
        if (CollectionUtils.isEmpty(list)) throw CanNotFindBeanException.ofClassname(className);
        if (list.size() > 1) throw MultipleBeanException.of(className);
        Object obj = list.get(0);
        return (T) obj;
    }
}
