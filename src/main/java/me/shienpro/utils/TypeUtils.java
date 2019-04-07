package me.shienpro.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TypeUtils {
    private final static Map<Class<?>, Class<?>> PRIMITIVE_CLASS_MAP = new HashMap<>();
    private final static Map<Class<?>, Method> PARSE_METHOD_MAP = new HashMap<>();

    static {
        PRIMITIVE_CLASS_MAP.put(boolean.class, Boolean.class);
        PRIMITIVE_CLASS_MAP.put(byte.class, Byte.class);
        PRIMITIVE_CLASS_MAP.put(short.class, Short.class);
        PRIMITIVE_CLASS_MAP.put(char.class, Character.class);
        PRIMITIVE_CLASS_MAP.put(int.class, Integer.class);
        PRIMITIVE_CLASS_MAP.put(long.class, Long.class);
        PRIMITIVE_CLASS_MAP.put(float.class, Float.class);
        PRIMITIVE_CLASS_MAP.put(double.class, Double.class);

        PRIMITIVE_CLASS_MAP.values().forEach(c -> PARSE_METHOD_MAP.put(c, getPM(c)));
    }

    private static Method getPM(Class c) {
        Class[] stringClassArr = {String.class};
        return Arrays.stream(c.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("parse"))
                .filter(m -> m.getParameterCount() == 1)
                .filter(m -> Arrays.equals(m.getParameterTypes(), stringClassArr))
                .findFirst()
                .orElse(null);
    }

    public static boolean isWrapper(Class<?> type) {
        return PRIMITIVE_CLASS_MAP.containsValue(type);
    }

    public static Class primitiveToReference(Class clazz) {
        if (!clazz.isPrimitive()) return clazz;
        return PRIMITIVE_CLASS_MAP.get(clazz);
    }

    public static Method getParseMethod(Class c) {
        return PARSE_METHOD_MAP.get(c);
    }
}
