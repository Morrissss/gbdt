package criterion;

import utils.AbstractNameFactory;

public class CriterionFactory extends AbstractNameFactory<SplitCriterion> {

    public static CriterionFactory getInstance() {
        return INSTANCE;
    }

    private static final CriterionFactory INSTANCE = new CriterionFactory();
    private CriterionFactory() {
        super();
    }

    @Override
    public SplitCriterion fetch(String name) throws IllegalArgumentException {
        if ("mse".equals(name)) {
            return MseCriterion.getInstance();
        } else {
            throw new IllegalArgumentException("Nonexistent criterion");
        }
    }
}
