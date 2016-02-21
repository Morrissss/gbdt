package criterion;

import utils.AbstractNameFactory;

public class CriterionFactory extends AbstractNameFactory<SplitCriterion> {

    public static SplitCriterion fetchCriterion(String name) {
        return INSTANCE.fetch(name);
    }

    private static final CriterionFactory INSTANCE = new CriterionFactory();
    private CriterionFactory() {
        super();
    }

    @Override
    protected SplitCriterion fetch(String name) throws IllegalArgumentException {
        if ("mse".equals(name)) {
            return MseCriterion.getInstance();
        } else {
            throw new IllegalArgumentException("Nonexistent criterion");
        }
    }
}
