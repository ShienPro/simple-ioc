package me.shienpro;

import me.shienpro.annotation.Autowired;
import me.shienpro.annotation.Component;
import me.shienpro.annotation.Value;
import me.shienpro.core.Context;
import me.shienpro.entity.RefEntity;

public class Main {
    public static void main(String[] args) {
        Context context = IocRunner.run();

        A a = context.getBeanInstance(A.class);
        System.out.println(a);

        RefEntity refEntity = context.getBeanInstance(RefEntity.class);
        System.out.println(refEntity);
    }
}

@Component
class A {
    @Autowired
    private B b;

    @Override
    public String toString() {
        return "A{" +
                "b=" + b +
                '}';
    }
}

@Component
class B {
    @Value("我是B")
    private String name;

    @Override
    public String toString() {
        return "B{" +
                "name='" + name + '\'' +
                '}';
    }
}