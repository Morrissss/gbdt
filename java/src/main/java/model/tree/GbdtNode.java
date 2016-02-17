package model.tree;

import instance.Instance;
import utils.Pair;

import java.util.List;

public class GbdtNode {

    public GbdtNode(List<Instance> trainSet) {
        this.includedInstances = trainSet;
    }

    public int featureIdx;
    public String featureKey;
    public double threshold;
    public double value;
    public GbdtNode lessEqual, greater;

    public List<Instance> includedInstances;

    public boolean isLeaf() {
        return lessEqual == null && greater == null;
    }

    /**
     * @throws UnsupportedOperationException if this node is a leaf
     */
    public Pair<GbdtNode, Integer> next(Instance instance, int currPath) throws UnsupportedOperationException {
        double feature = instance.x[featureIdx];
        if (isLeaf()) {
            throw new UnsupportedOperationException("no next node for a leaf");
        } else if (feature > threshold) {
            return Pair.of(greater, PathUtils.toRight(currPath));
        } else {
            return Pair.of(lessEqual, PathUtils.toLeft(currPath));
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GbdtNode{");
        sb.append("size=").append(includedInstances.size());
        if (!isLeaf()) {
            sb.append(", featureKey=").append(featureKey);
            sb.append(", threshold=").append(threshold);
            sb.append(", lessEqual=").append(lessEqual);
            sb.append(", greater=").append(greater);
        } else {
            sb.append(", value=").append(value);
        }
        sb.append('}');
        return sb.toString();
    }
}
