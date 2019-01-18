package me.shienpro.bean;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class InjectArg<T> {
    private String name;
    private Class<T> argClass;
    private T value;
    private Bean refBean;
    private boolean useSetter;
}
