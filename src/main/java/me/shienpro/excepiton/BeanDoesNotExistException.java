package me.shienpro.excepiton;

public class BeanDoesNotExistException extends RuntimeException {
    public BeanDoesNotExistException(String argName) {
        super(String.format("The bean corresponding to the arg named '%s' does not exist.", argName));
    }

    public BeanDoesNotExistException(Class argClass) {
        super(String.format("The bean corresponding to the arg of class '%s' does not exist.", argClass.getName()));
    }
}
