package criterion;

public class MseCriterion extends AbstractAdditiveCriterion {

    @Override
    protected void updateLoss(int beg, int end) {
        leftLoss = leftSquaredSum / leftNum - (leftSum / leftNum) * (leftSum / leftNum);
        rightLoss = rightSquaredSum / rightNum - (rightSum / rightNum) * (rightSum / rightNum);
    }
}
