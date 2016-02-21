package utils;

public abstract class AbstractNameFactory<T> {

    protected AbstractNameFactory() {
        // empty;
    }

    protected abstract T fetch(String name) throws IllegalArgumentException;
}
