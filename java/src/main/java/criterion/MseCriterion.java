package criterion;

public class MseCriterion extends AbstractAdditiveCriterion {

    @Override
    protected double initLoss() {
        return 0;
    }

    @Override
    protected void updateOthers(int beg, int end) {
        leftLoss = leftSquaredSum / leftNum - (leftSum / leftNum) * (leftSum / leftNum);
        rightLoss = rightSquaredSum / rightNum - (rightSum / rightNum) * (rightSum / rightNum);
    }
}
