package criterion;

public class MseCriterion extends AbstractSortedCriterion {

    @Override
    protected double initLoss() {
        return 0;
    }

    @Override
    protected void updateOthers(int beg, int end) {

    }

    @Override
    protected double calcLoss(int beg, int end) {
        return 0;
    }
}
