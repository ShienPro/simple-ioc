package me.shienpro.core;

public interface Context {
    <T> T getBean(String beanName);

    <T> T getBean(Class<T> beanClass);

    <T> T getBeanByClassName(String className);
}
