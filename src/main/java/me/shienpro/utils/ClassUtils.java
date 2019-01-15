package me.shienpro.utils;

public class ClassUtils {
    public static ClassLoader getClassloader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static Class loadClass(String className) throws ClassNotFoundException {
        return getClassloader().loadClass(className);
    }
}
