package morrissss.base.util;

import java.io.Serializable;

public class FullCompPair<K extends Comparable<? super K>, V extends Comparable<? super V>>
        extends KeyCompPair<K, V> implements Serializable {

    private static final long serialVersionUID = -3830366963760176723L;

    public static <K extends Comparable<? super K>, V extends Comparable<? super V>>
    FullCompPair of(K fst, V snd) {
        return new FullCompPair<>(fst, snd);
    }

    @Override
    public int compareTo(Pair<K, V> o) {
        return this.fst.compareTo(o.fst);
    }

    public FullCompPair(K fst, V snd) {
        super(fst, snd);
    }
}
