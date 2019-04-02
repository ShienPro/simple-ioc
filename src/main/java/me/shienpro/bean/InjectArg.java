package me.shienpro.bean;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class InjectArg<T> {
    private int argType;
    private String name;
    private Class<T> argClass;
    private T value;
    private boolean useSetter;
    private String refBeanName;
}
