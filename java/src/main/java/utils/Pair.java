package utils;

public class Pair<U, V> {

    public static <U, V> Pair of(U first, V second) {
        return new Pair(first, second);
    }

    private final U first;
    private final V second;

    private Pair(U first, V second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return first + "," + second;
    }

}
