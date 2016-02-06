package criterion;

public class MseCriterion extends AbstractAdditiveCriterion {

    public static SplitCriterion getInstance() {
        return INSTANCE;
    }

    private static final SplitCriterion INSTANCE = new MseCriterion();
    protected MseCriterion() {
        super();
    }

    @Override
    protected void updateLoss(int beg, int end) {
        if (leftNum == 0) {
            leftLoss = 0;
        } else {
            leftLoss = leftSquaredSum / leftNum - (leftSum / leftNum) * (leftSum / leftNum);
        }
        if (rightNum == 0) {
            rightLoss = 0;
        } else {
            rightLoss = rightSquaredSum / rightNum - (rightSum / rightNum) * (rightSum / rightNum);
        }
    }
}
