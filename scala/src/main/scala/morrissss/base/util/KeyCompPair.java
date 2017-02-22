package morrissss.base.util;

import java.io.Serializable;

public class KeyCompPair<K extends Comparable<? super K>, V>
        extends Pair<K, V> implements Comparable<Pair<K, V>>, Serializable {

    private static final long serialVersionUID = -7235690786482188010L;

    public static <K extends Comparable<? super K>, V> KeyCompPair of(K fst, V snd) {
        return new KeyCompPair<>(fst, snd);
    }

    @Override
    public int compareTo(Pair<K, V> o) {
        return this.fst.compareTo(o.fst);
    }

    public KeyCompPair(K fst, V snd) {
        super(fst, snd);
    }
}
