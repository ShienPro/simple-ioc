package me.shienpro.bean;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Bean {
    private String beanName;
    private Class<?> beanClass;
    private List<Arg> constructorArgs;
    private List<Arg> injectArgs;
}
