package splitter;

import model.GbdtParams;
import model.tree.GbdtNode;

public interface Splitter {

    /**
     * must be called before split()
     */
    void init(GbdtParams params, GbdtNode node);

    /**
     * not thread safe
     * @return split or not
     */
    boolean split();

}
