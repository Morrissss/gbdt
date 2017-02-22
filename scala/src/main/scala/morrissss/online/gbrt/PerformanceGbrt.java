package morrissss.online.gbrt;

import morrissss.base.feature.ModelKey;
import morrissss.base.util.MathUtils;
import morrissss.online.util.Sampler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PerformanceGbrt {

    public static PerformanceGbrt parse(List<String> lines) {
        String[] ratios = lines.get(0).split("\\s+");
        Sampler sampler = new Sampler(Double.parseDouble(ratios[0]), Double.parseDouble(ratios[1]));

        String[] keys = lines.get(1).split("\\s+");
        Map<ModelKey, Integer> keyIdx = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            keyIdx.put(ModelKey.parse(keys[i]), i);
        }

        String[] treeNumDepth = lines.get(2).split("\\s+");
        int treeNum = Integer.parseInt(treeNumDepth[0]);
        int treeDepth = Integer.parseInt(treeNumDepth[1]);
        int treeNodeNum = (1<<(treeDepth+1)) - 1;

        PerformanceGbrt result = new PerformanceGbrt(treeNum, treeNodeNum, keyIdx, sampler);
        int lineIdx = 3;
        for (int treeIdx = 0; treeIdx < treeNum; treeIdx++) {
            result.treeWeights[treeIdx] = Double.parseDouble(lines.get(lineIdx++));
            lineIdx = result.parseHelper(lines, lineIdx, treeIdx, 0);
        }
        return result;
    }

    /**
     * @return next line
     */
    private int parseHelper(List<String> lines, int curLineIdx, int treeIdx, int nodeIdx) {
//        System.out.println(treeIdx + " " + nodeIdx);
        if (parseNode(lines.get(curLineIdx), calcGlobalIdx(treeIdx, nodeIdx))) {
            return curLineIdx+1;
        } else {
            int next = parseHelper(lines, curLineIdx+1, treeIdx, calcLeftIdx(nodeIdx));
            return parseHelper(lines, next, treeIdx, calcRightIdx(nodeIdx));
        }
    }

    // return isLeaf
    private boolean parseNode(String line, int globalIdx) {
        String[] parts = line.trim().split(" ");
        if (parts.length == 4) {
            ModelKey modelKey = ModelKey.parse(parts[0]);
            double threshold = Double.parseDouble(parts[1]);
            double predict = Double.parseDouble(parts[2]);
            double trainingFraction = Double.parseDouble(parts[3]);
            this.featureIndices[globalIdx] = keyIdx.get(modelKey);
            this.thresPreds[globalIdx] = threshold;
            return false;
        } else {
            double predict = Double.parseDouble(parts[0]);
            double trainingFraction = Double.parseDouble(parts[1]);
            this.featureIndices[globalIdx] = -1;
            this.thresPreds[globalIdx] = predict;
            return true;
        }
    }

    private PerformanceGbrt(int treeNum, int treeNodeNum, Map<ModelKey, Integer> keyIdx, Sampler sampler) {
        this.treeNum = treeNum;
        this.treeNodeNum = treeNodeNum;
        this.keyIdx = keyIdx;
        this.sampler = sampler;

        this.treeWeights = new double[treeNum];
        this.featureIndices = new int[treeNum * treeNodeNum];
        Arrays.fill(this.featureIndices, -1);
        this.thresPreds = new double[treeNum * treeNodeNum];
    }

    public final int treeNum;
    public final int treeNodeNum;
    public final Map<ModelKey, Integer> keyIdx;
    public final Sampler sampler;
    public final double[] treeWeights;

    // trees
    int[] featureIndices;
    double[] thresPreds;

    public double predict(Map<ModelKey, Double> featureMap) {
        double[] features = new double[keyIdx.size()];
        for (Entry<ModelKey, Double> entry : featureMap.entrySet()) {
            ModelKey modelKey = entry.getKey();
            features[keyIdx.get(modelKey)] = featureMap.get(modelKey);
        }
        double sum = 0;
        for (int treeIdx = 0; treeIdx < treeNum; treeIdx++) {
            int nodeIdx = 0;
            while (true) {
                int globalIdx = calcGlobalIdx(treeIdx, nodeIdx);
                int featureIdx = featureIndices[globalIdx];
                double thresPred = thresPreds[globalIdx];
                if (featureIdx < 0) {
                    sum += thresPred * treeWeights[treeIdx];
                    break;
                } else {
                    double feature = features[featureIdx];
                    if (feature <= thresPred) {
                        nodeIdx = calcLeftIdx(nodeIdx);
                    } else {
                        nodeIdx = calcRightIdx(nodeIdx);
                    }
                }
            }
        }
        return sampler.calibrate(MathUtils.sigmoid(sum));
    }

    private int calcGlobalIdx(int treeIdx, int nodeIdx) {
        return nodeIdx + treeIdx * treeNodeNum;
    }
    private int calcLeftIdx(int idx) {
        return ((idx + 1) << 1) - 1;
    }
    private int calcRightIdx(int idx) {
        return (idx + 1) << 1;
    }
}
