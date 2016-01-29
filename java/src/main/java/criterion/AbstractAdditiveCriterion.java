package criterion;

import instance.Instance;

import java.util.List;

public abstract class AbstractAdditiveCriterion implements SplitCriterion {

    protected List<Instance> samples;
    protected int rightBeg;

    protected int leftNum;
    protected int rightNum;
    protected double leftSum;
    protected double rightSum;
    protected double leftSquaredSum;
    protected double rightSquaredSum;
    protected double leftLoss;
    protected double rightLoss;

    @Override
    public double reset(List<Instance> samples) {
        this.samples = samples;
        this.rightBeg = 0;

        leftNum = 0;
        leftSum = 0;
        leftSquaredSum = 0;

        rightNum = 0;
        rightSum = 0;
        rightSquaredSum = 0;
        for (Instance sample : samples) {
            rightNum++;
            rightSum = sample.y;
            rightSquaredSum = sample.y * sample.y;
        }
        updateLoss(0, 0);
        return loss();
    }

    @Override
    public boolean moveLeft(int num) {
        int batchEnd = Math.min(samples.size(), rightBeg+num);
        if (batchEnd == rightBeg) {
            return false;
        }
        for (int i = rightBeg; i < batchEnd; i++) {
            leftNum++;
            rightNum--;
            double y = samples.get(i).y;
            leftSum += y;
            rightSum -= y;
            leftSquaredSum += y * y;
            rightSquaredSum -= y * y;
        }
        updateLoss(rightBeg, batchEnd);
        rightBeg = batchEnd;
        return true;
    }

    @Override
    public double loss() {
        return leftLoss + rightLoss;
    }

    protected abstract void updateLoss(int beg, int end);
}
