package me.shienpro.bean;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class Bean {
    public final static int SINGLETON = 0;
    public final static int PROTOTYPE = 1;

    private int scope = SINGLETON;
    private String beanName;
    private Class<?> beanClass;
    private List<ConstructorArg> constructorArgs;
    private List<InjectArg> injectArgs;
}
