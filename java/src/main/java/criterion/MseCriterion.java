package criterion;

import instance.Instance;

public class MseCriterion extends AbstractAdditiveCriterion {

    public static SplitCriterion getInstance() {
        return new MseCriterion();
    }

    private MseCriterion() {
        super();
    }

    @Override
    protected void updateImpurity() {
        if (leftNum == 0) {
            leftImpurity = 0;
        } else {
            leftImpurity = leftSquaredSum / leftNum - (leftSum / leftNum) * (leftSum / leftNum);
        }
        if (rightNum == 0) {
            rightImpurity = 0;
        } else {
            rightImpurity = rightSquaredSum / rightNum - (rightSum / rightNum) * (rightSum / rightNum);
        }
    }

    @Override
    protected void calcStatics(int feature, double threshold) {
        leftNum = rightNum = 0;
        leftSum = rightSum = 0;
        leftSquaredSum = rightSquaredSum = 0;
        for (Instance sample : samples) {
            if (sample.x[feature] <= threshold) {
                leftNum++;
                leftSum += sample.target;
                leftSquaredSum += sample.target * sample.target;
            } else {
                rightNum++;
                rightSum += sample.target;
                rightSquaredSum += sample.target * sample.target;
            }
        }
    }
}
