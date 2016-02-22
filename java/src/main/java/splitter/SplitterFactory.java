package splitter;

import utils.AbstractNameFactory;

public class SplitterFactory extends AbstractNameFactory<Splitter> {

    public static Splitter fetchSplitter(String name) {
        return INSTANCE.fetch(name);
    }

    private static SplitterFactory INSTANCE = new SplitterFactory();
    private SplitterFactory() {
        super();
    }

    @Override
    protected Splitter fetch(String name) throws IllegalArgumentException {
        if ("sort".equals(name)) {
            return SortSplitter.getInstance();
        } else if ("random".equals(name)) {
            return RandomSplitter.getInstance();
        } else {
            throw new IllegalArgumentException("Nonexistent splitter");
        }
    }
}
