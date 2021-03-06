package me.shienpro.excepiton;

public class MultipleBeanException extends RuntimeException {
    public MultipleBeanException(String message) {
        super(message);
    }

    public static MultipleBeanException of(Class<?> beanClass) {
        return new MultipleBeanException(String.format("There are multiple %s bean", beanClass.getName()));
    }

    public static MultipleBeanException of(String className) {
        return new MultipleBeanException(String.format("There are multiple %s bean", className));
    }
}
