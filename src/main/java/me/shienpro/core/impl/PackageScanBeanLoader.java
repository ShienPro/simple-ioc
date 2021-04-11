package me.shienpro.core.impl;

import me.shienpro.annotation.Autowired;
import me.shienpro.annotation.Component;
import me.shienpro.annotation.Value;
import me.shienpro.bean.ArgType;
import me.shienpro.bean.Bean;
import me.shienpro.bean.ConstructorArg;
import me.shienpro.bean.InjectArg;
import me.shienpro.core.BeanLoader;
import me.shienpro.excepiton.ContextInitializationException;
import me.shienpro.excepiton.ValueException;
import me.shienpro.utils.PackageScanUtils;
import me.shienpro.utils.TypeUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class PackageScanBeanLoader implements BeanLoader {
    private PackageScanBeanLoader() {
    }

    private Class<?> mainClass;
    private final Map<String, Integer> beanNameCache = new HashMap<>();

    public static PackageScanBeanLoader fromMainClass(Class<?> mainClass) {
        PackageScanBeanLoader loader = new PackageScanBeanLoader();
        loader.mainClass = mainClass;
        return loader;
    }

    @Override
    public List<Bean> load() {
        Set<String> classes = PackageScanUtils.scanPackage(mainClass);
        return classes.stream()
                .map(PackageScanBeanLoader::getClass)
                .filter(c -> c.getAnnotation(Component.class) != null)
                .map(this::classToBean)
                .collect(toList());
    }

    private static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ContextInitializationException(e);
        }
    }

    private Bean classToBean(Class<?> c) {
        Component component = c.getAnnotation(Component.class);
        String beanName;
        if (component.value().length() > 0) {
            beanName = component.value();
        } else {
            String simpleName = c.getSimpleName();
            Integer count = beanNameCache.getOrDefault(simpleName, 0);
            beanName = simpleName + count;
            beanNameCache.put(simpleName, ++count);
        }

        Bean bean = new Bean().setBeanClass(c).setBeanName(beanName).setScope(component.scope());

        Constructor<?> ctor = Arrays.stream(c.getDeclaredConstructors())
                .filter(constructor -> constructor.getAnnotation(Autowired.class) != null)
                .findFirst().orElseGet(() -> {
                    try {
                        return c.getDeclaredConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new ContextInitializationException(e);
                    }
                });

        List<ConstructorArg<?>> constructorArgs = Arrays.stream(ctor.getParameters())
                .map(p -> {
                    Class<?> type = p.getType();

                    Value value = p.getAnnotation(Value.class);
                    if (value != null) {
                        return convertValueToConstructorArg(value, type);
                    }

                    Autowired autowired = p.getAnnotation(Autowired.class);
                    return convertAutowiredToArg(autowired, type);
                }).collect(toList());

        List<InjectArg<?>> injectArgs = Stream.concat(
                Arrays.stream(c.getDeclaredFields())
                        .filter(this::hasAnnotation)
                        .map(this::convertFieldToArg),
                Arrays.stream(c.getDeclaredMethods())
                        .filter(this::hasAnnotation)
                        .filter(this::isSetMethod)
                        .map(this::convertMethodToArg)
        ).collect(toList());

        return bean
                .setConstructorArgs(constructorArgs)
                .setInjectArgs(injectArgs);
    }

    private <T> ConstructorArg<T> convertValueToConstructorArg(Value value, Class<T> type) {
        ConstructorArg<T> constructorArg = new ConstructorArg<T>()
                .setArgType(ArgType.VALUE)
                .setArgClass(type);

        T injectValue = getValueOfValueAnnotation(value, type);
        return constructorArg.setValue(injectValue);
    }

    @SuppressWarnings("unchecked")
    private <T> T getValueOfValueAnnotation(Value value, Class<T> type) {
        String v = value.value();
        if (v.length() == 0) {
            throw new ValueException(type.getName());
        }

        if (!type.isPrimitive() && !TypeUtils.isWrapper(type) && !type.equals(String.class)) {
            throw new ValueException();
        }

        if (type.equals(String.class)) {
            return (T) v;
        }

        T injectValue = null;

        Class<T> referenceType = (Class<T>) TypeUtils.primitiveToReference(type);

        Method parseMethod = TypeUtils.getParseMethod(referenceType);
        if (parseMethod != null) {
            try {
                injectValue = (T) parseMethod.invoke(null, v);
            } catch (IllegalAccessException | InvocationTargetException ignore) {
            }
        } else {
            injectValue = (T) (Character) v.charAt(0);
        }

        return injectValue;
    }

    private <T> ConstructorArg<?> convertAutowiredToArg(Autowired autowired, Class<T> type) {
        ConstructorArg<T> constructorArg = new ConstructorArg<T>()
                .setArgClass(type);

        if (autowired != null && autowired.value().length() > 0) {
            return constructorArg
                    .setArgType(ArgType.REF_BEAN_NAME)
                    .setRefBeanName(autowired.value());
        }

        return constructorArg
                .setArgType(ArgType.REF_CLASS_NAME);
    }

    private boolean hasAnnotation(AccessibleObject ao) {
        return ao.getAnnotation(Autowired.class) != null || ao.getAnnotation(Value.class) != null;
    }

    private InjectArg<?> convertFieldToArg(Field field) {
        return convertParamToArg(
                field.getName(),
                field.getType(),
                field.getAnnotation(Value.class),
                field.getAnnotation(Autowired.class),
                false
        );
    }

    private boolean isSetMethod(Method method) {
        String name = method.getName();
        return method.getParameterCount() == 1 &&
                name.startsWith("set") &&
                name.length() > 3 &&
                Character.isUpperCase(name.charAt(3));
    }

    private InjectArg<?> convertMethodToArg(Method method) {
        String methodName = method.getName();
        String name = methodName.length() == 4 ?
                String.valueOf(Character.toLowerCase(methodName.charAt(3))) :
                Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        Class<?> type = method.getParameters()[0].getType();
        return convertParamToArg(
                name,
                type,
                method.getAnnotation(Value.class),
                method.getAnnotation(Autowired.class),
                true
        );
    }

    private <T> InjectArg<T> convertParamToArg(String name, Class<T> type, Value value, Autowired autowired, boolean useSetter) {
        InjectArg<T> injectArg = new InjectArg<T>()
                .setName(name)
                .setArgClass(type)
                .setUseSetter(useSetter);

        if (value != null) {
            T v = getValueOfValueAnnotation(value, type);
            return injectArg
                    .setArgType(ArgType.VALUE)
                    .setValue(v);
        }

        if (autowired != null && autowired.value().length() > 0) {
            return injectArg
                    .setArgType(ArgType.REF_BEAN_NAME)
                    .setRefBeanName(autowired.value());
        }

        return injectArg
                .setArgType(ArgType.REF_CLASS_NAME);
    }
}
