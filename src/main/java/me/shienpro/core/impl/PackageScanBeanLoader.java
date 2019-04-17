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

    private Class mainClass;
    private Map<String, Integer> beanNameCache = new HashMap<>();

    public static PackageScanBeanLoader fromMainClass(Class mainClass) {
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
            Integer count = beanNameCache.get(simpleName);
            if (count == null) count = 0;
            beanName = simpleName + count;
            beanNameCache.put(simpleName, ++count);
        }

        Bean bean = new Bean().setBeanClass(c).setBeanName(beanName).setScope(component.scope());

        Constructor<?> ctor = Arrays.stream(c.getConstructors())
                .filter(constructor -> constructor.getAnnotation(Autowired.class) != null)
                .findFirst().orElseGet(() -> {
                    try {
                        return c.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new ContextInitializationException(e);
                    }
                });

        List<ConstructorArg> constructorArgs = Arrays.stream(ctor.getParameters())
                .map(p -> {
                    Class<?> type = p.getType();

                    Value value = p.getAnnotation(Value.class);
                    if (value != null) {
                        return convertValueToConstructorArg(value, type);
                    }

                    Autowired autowired = p.getAnnotation(Autowired.class);
                    return convertAutowiredToArg(autowired, type);
                }).collect(toList());

        List<InjectArg> injectArgs = Stream.concat(
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

    @SuppressWarnings("unchecked")
    private ConstructorArg convertValueToConstructorArg(Value value, Class<?> type) {
        ConstructorArg constructorArg = new ConstructorArg()
                .setArgType(ArgType.VALUE)
                .setArgClass(type);

        Object injectValue = getValueOfValueAnnotation(value, type);
        return constructorArg.setValue(injectValue);
    }

    private Object getValueOfValueAnnotation(Value value, Class<?> type) {
        String v = value.value();
        if (v.length() == 0) {
            throw new ValueException(type.getName());
        }

        if (!type.isPrimitive() && !TypeUtils.isWrapper(type) && !type.equals(String.class)) {
            throw new ValueException();
        }

        if (type.equals(String.class)) {
            return v;
        }

        Object injectValue = null;

        Class referenceType = TypeUtils.primitiveToReference(type);

        Method parseMethod = TypeUtils.getParseMethod(referenceType);
        if (parseMethod != null) {
            try {
                injectValue = parseMethod.invoke(null, v);
            } catch (IllegalAccessException | InvocationTargetException ignore) {
            }
        } else {
            injectValue = v.charAt(0);
        }

        return injectValue;
    }

    @SuppressWarnings("unchecked")
    private ConstructorArg convertAutowiredToArg(Autowired autowired, Class<?> type) {
        ConstructorArg constructorArg = new ConstructorArg()
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

    private InjectArg convertFieldToArg(Field field) {
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

    private InjectArg convertMethodToArg(Method method) {
        String methodName = method.getName();
        String name = methodName.length() == 4 ?
                String.valueOf(Character.toLowerCase(methodName.charAt(3))) :
                Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        Class type = method.getParameters()[0].getType();
        return convertParamToArg(
                name,
                type,
                method.getAnnotation(Value.class),
                method.getAnnotation(Autowired.class),
                true
        );
    }

    @SuppressWarnings("unchecked")
    private InjectArg convertParamToArg(String name, Class type, Value value, Autowired autowired, boolean useSetter) {
        InjectArg injectArg = new InjectArg()
                .setName(name)
                .setArgClass(type)
                .setUseSetter(useSetter);

        if (value != null) {
            Object v = getValueOfValueAnnotation(value, type);
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
