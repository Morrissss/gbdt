package splitter;

import model.GbdtParams;
import model.tree.GbdtNode;

public interface Splitter {

    /**
     * not thread safe
     * @return split or not
     */
    boolean split(GbdtParams params, GbdtNode node);

}
