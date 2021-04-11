package me.shienpro;

import me.shienpro.core.Context;
import me.shienpro.core.impl.ContextImpl;
import me.shienpro.core.impl.PackageScanBeanLoader;
import me.shienpro.excepiton.MainClassNotFoundException;

import java.util.Objects;

public class IocRunner {
    public static Context run(Class<?> mainClass) {
        return new ContextImpl(PackageScanBeanLoader.fromMainClass(mainClass));
    }

    public static Context run() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement mainStack = stackTrace[stackTrace.length - 1];
        if (!Objects.equals("main", mainStack.getMethodName())) {
            throw new MainClassNotFoundException();
        }

        Class<?> mainClass = null;
        try {
            mainClass = Class.forName(mainStack.getClassName());
        } catch (ClassNotFoundException ignore) {
        }

        return run(mainClass);
    }
}
