package me.shienpro.excepiton;

public class CanNotFindBeanException extends RuntimeException {
    public CanNotFindBeanException(String message) {
        super(message);
    }

    public static CanNotFindBeanException of(Class<?> beanClass) {
        return new CanNotFindBeanException(String.format("Cannot find bean with class %s", beanClass.getName()));
    }

    public static CanNotFindBeanException ofClassname(String className) {
        return new CanNotFindBeanException(String.format("Cannot find bean with classname %s", className));
    }

    public static CanNotFindBeanException ofBeanName(String className) {
        return new CanNotFindBeanException(String.format("Cannot find bean with bean name %s", className));
    }
}
