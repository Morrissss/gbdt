package utils;

public abstract class AbstractNameFactory<T> {

    protected AbstractNameFactory() {
        // empty;
    }

    public abstract T fetch(String name) throws IllegalArgumentException;
}
