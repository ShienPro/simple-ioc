package me.shienpro.core.impl;

import me.shienpro.bean.Bean;
import me.shienpro.core.BeanInitializer;
import me.shienpro.core.BeanLoader;
import me.shienpro.core.Context;
import me.shienpro.excepiton.BeanNameRepeatException;
import me.shienpro.excepiton.CanNotFindBeanException;
import me.shienpro.excepiton.MultipleBeanException;

import java.util.*;

public class ContextImpl implements Context {
    private Map<Class, List<Bean>> classBeanMap = new HashMap<>();
    private Map<String, List<Bean>> classNameBeanMap = new HashMap<>();
    private Map<String, Bean> beanNameMap = new HashMap<>();

    public ContextImpl(BeanLoader loader) {
        BeanInjector.init(this);
        List<Bean> beans = loader.load();
        beans.forEach(this::registerBean);
        beans.forEach(BeanInitializer::getBeanInstance);
    }

    private void registerBean(Bean bean) {
        Class<?> beanClass = bean.getBeanClass();
        Set<Class> classSet = new HashSet<>(Arrays.asList(beanClass.getInterfaces()));
        while (beanClass != null) {
            classSet.add(beanClass);
            beanClass = beanClass.getSuperclass();
        }

        classSet.forEach(c -> {
            classBeanMap.computeIfAbsent(c, k -> new ArrayList<>()).add(bean);
            classNameBeanMap.computeIfAbsent(c.getName(), k -> new ArrayList<>()).add(bean);
        });

        Bean b = beanNameMap.get(bean.getBeanName());
        if (b != null) {
            throw new BeanNameRepeatException(bean.getBeanName());
        }
        beanNameMap.put(bean.getBeanName(), bean);
    }

    @Override
    public <T> T getBeanInstance(String beanName) {
        Bean bean = beanNameMap.get(beanName);
        if (bean == null) throw CanNotFindBeanException.ofBeanName(beanName);
        return BeanInitializer.getBeanInstance(bean);
    }

    @Override
    public <T> T getBeanInstance(Class<T> beanClass) {
        List<Bean> list = classBeanMap.get(beanClass);
        if (list == null || list.isEmpty()) throw CanNotFindBeanException.of(beanClass);
        if (list.size() > 1) throw MultipleBeanException.of(beanClass);
        Bean bean = list.get(0);
        return BeanInitializer.getBeanInstance(bean);
    }

    @Override
    public <T> T getBeanInstanceByClassName(String className) {
        List<Bean> list = classNameBeanMap.get(className);
        if (list == null || list.isEmpty()) throw CanNotFindBeanException.ofClassname(className);
        if (list.size() > 1) throw MultipleBeanException.of(className);
        Bean bean = list.get(0);
        return BeanInitializer.getBeanInstance(bean);
    }
}
