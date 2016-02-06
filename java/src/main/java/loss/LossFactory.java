package loss;

import utils.AbstractNameFactory;

public class LossFactory extends AbstractNameFactory<Loss> {

    public static LossFactory getInstance() {
        return INSTANCE;
    }

    private static final LossFactory INSTANCE = new LossFactory();
    private LossFactory() {
        super();
    }

    @Override
    public Loss fetch(String name) throws IllegalArgumentException {
        if ("exp".equals(name)) {
            return ExponentialLoss.getInstance();
        } else if ("log".equals(name)) {
            return LogLoss.getInstance();
        } else {
            throw new IllegalArgumentException("Nonexistent loss");
        }
    }
}
