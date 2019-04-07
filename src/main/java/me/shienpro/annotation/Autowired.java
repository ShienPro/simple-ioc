package me.shienpro.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Documented
public @interface Autowired {
    /**
     * @return bean name
     */
    String value() default "";
}
