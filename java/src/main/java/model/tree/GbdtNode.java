package model.tree;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import instance.Instance;
import utils.Pair;

import java.util.List;

public class GbdtNode {

    public GbdtNode(List<Instance> trainSet) {
        this.includedInstances = trainSet;
    }

    @JSONField(serialize = false)
    public int featureIdx;
    @JSONField(ordinal = 0)
    public String featureKey;
    @JSONField(ordinal = 1)
    public double threshold;
    @JSONField(ordinal = 2)
    public double value;
    @JSONField(ordinal = 3)
    public GbdtNode lessEqual;
    @JSONField(ordinal = 4)
    public GbdtNode greater;
    @JSONField(serialize = false)
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
        return JSON.toJSONString(this, true);
    }
}
