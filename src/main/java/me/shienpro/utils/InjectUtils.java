package me.shienpro.utils;

import me.shienpro.excepiton.InjectException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class InjectUtils {


    public static void inject(Field field, Object target, Object property) throws InjectException {
        if (field == null) throw new InjectException(new NullPointerException("field can not be null"));

        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        try {
            field.set(target, property);
        } catch (IllegalAccessException e) {
            throw new InjectException(e);
        }
    }

    public static void injectBySetter(Field field, Object target, Object property) {
        if (field == null) throw new InjectException(new NullPointerException("field can not be null"));

        String setterName = "set" +
                field.getName().substring(0, 1).toUpperCase() +
                field.getName().substring(1);

        Method[] methods = target.getClass().getDeclaredMethods();
        Arrays.stream(methods)
                .filter(method -> method.getName().equals(setterName) &&
                        method.getParameterTypes().length == 1 &&
                        method.getParameterTypes()[0].isInstance(property))
                .forEach(method -> {
                    try {
                        method.invoke(target, property);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new InjectException(e);
                    }
                });
    }

    private static boolean isBoolean(Field field) {
        Class<?> fieldType = field.getType();
        return fieldType.getName().equals("boolean") || fieldType.equals(Boolean.class);
    }
}
