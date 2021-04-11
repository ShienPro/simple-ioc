package me.shienpro;

import me.shienpro.core.Context;
import me.shienpro.core.impl.ContextImpl;
import me.shienpro.core.impl.PackageScanBeanLoader;
import me.shienpro.excepiton.MainClassNotFoundException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class IocRunner {
    public static Context run(Class<?> mainClass) {
        return new ContextImpl(PackageScanBeanLoader.fromMainClass(mainClass));
    }

    public static Context run() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement mainStack = null;

        for (StackTraceElement stack : stackTrace) {
            if (Objects.equals(stack.getMethodName(), "main")) {
                mainStack = stack;
            }
        }
        if (mainStack == null) {
            throw new MainClassNotFoundException();
        }

        Class<?> mainClass;
        try {
            mainClass = Class.forName(mainStack.getClassName());
            Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
            if (!Modifier.isStatic(mainMethod.getModifiers())) {
                throw new NoSuchMethodException();
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new MainClassNotFoundException();
        }

        return run(mainClass);
    }
}
