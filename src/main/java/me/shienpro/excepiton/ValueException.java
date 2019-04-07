package me.shienpro.excepiton;

public class ValueException extends RuntimeException {
    public ValueException(String valueName) {
        super(String.format("Value of %s can not be empty.", valueName));
    }

    public ValueException() {
        super("@Value annotation only supports primitive types and String");
    }
}
