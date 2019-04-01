package me.shienpro.core;

public interface Context {
    <T> T getBeanInstance(String beanName);

    <T> T getBeanInstance(Class<T> beanClass);

    <T> T getBeanInstanceByClassName(String className);
}
