package me.shienpro.annotation;

import me.shienpro.bean.Bean;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Component {
    /**
     * @return bean name
     */
    String value() default "";

    /**
     * @return scope
     */
    int scope() default Bean.SINGLETON;
}
