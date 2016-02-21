package criterion;

public class MseCriterion extends AbstractAdditiveCriterion {

    public static SplitCriterion getInstance() {
        return new MseCriterion();
    }

    private MseCriterion() {
        super();
    }

    @Override
    protected void updateImpurity(int beg, int end) {
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
}
