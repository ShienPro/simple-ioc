package me.shienpro.excepiton;

public class BeanNameRepeatException extends RuntimeException {
    public BeanNameRepeatException(String beanName) {
        super(String.format("The name of the bean '%s' is repeated", beanName));
    }
}
