package me.shienpro.utils;

import java.lang.reflect.Field;

public class InjectUtils {
    public static void inject(Field field, Object target, Object property) throws IllegalAccessException {
        if (field == null) return;

        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        field.set(target, property);
    }
}
