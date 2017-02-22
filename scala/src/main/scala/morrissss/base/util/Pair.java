package morrissss.base.util;

import java.io.Serializable;
import java.util.Objects;

public class Pair<K, V> implements Serializable {

    private static final long serialVersionUID = 2756090277123215532L;

    public final K fst;
    public final V snd;
    public static <K, V> Pair of(K fst, V snd) {
        return new Pair<>(fst, snd);
    }

    public Pair(K fst, V snd) {
        this.fst = fst;
        this.snd = snd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(fst, pair.fst) &&
               Objects.equals(snd, pair.snd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fst, snd);
    }

    @Override
    public String toString() {
        return "(" + fst + ", " + snd + ")";
    }
}
