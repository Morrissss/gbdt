package morrissss.online.gbrt;

import morrissss.base.feature.ModelKey;
import morrissss.base.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GbrtTree {

    /**
     * @return (tree, begLine of next tree)
     */
    static Pair<GbrtTree, Integer> parse(List<String> lines, int begLine) {
        double weight = Double.parseDouble(lines.get(begLine));
        Pair<GbrtNode, Integer> root = parseHelper(lines, begLine+1);
        return Pair.of(new GbrtTree(weight, root.fst), root.snd);
    }

    /**
     * @return (node of curLineIdx, next line to this subtree)
     */
    private static Pair<GbrtNode, Integer> parseHelper(List<String> lines, int curLineIdx) {
        GbrtNode cur = GbrtNode.parse(lines.get(curLineIdx));
        if (cur.isLeaf()) {
            return Pair.of(cur, curLineIdx+1);
        } else {
            Pair<GbrtNode, Integer> left = parseHelper(lines, curLineIdx+1);
            cur.left = left.fst;
            Pair<GbrtNode, Integer> right = parseHelper(lines, left.snd);
            cur.right = right.fst;
            return Pair.of(cur, right.snd);
        }
    }

    final double weight;
    final GbrtNode root;

    private GbrtTree(double weight, GbrtNode root) {
        this.weight = weight;
        this.root = root;
    }

    double predict(Map<ModelKey, Double> features) {
        GbrtNode cur = root;
        while (!cur.isLeaf()) {
            double value = features.get(cur.modelKey);
            if (value <= cur.threshold) {
                cur = cur.left;
            } else {
                cur = cur.right;
            }
        }
        return cur.predictValue * weight;
    }

    Map<ModelKey, Double> featureDiff(Map<ModelKey, Double> features) {
        Map<ModelKey, Double> result = new HashMap<>();
        GbrtNode cur = root;
        double rootPredict = root.predictValue;
        while (!cur.isLeaf()) {
            GbrtNode next;
            double value = features.get(cur.modelKey);
            if (value <= cur.threshold) {
                next = cur.left;
            } else {
                next = cur.right;
            }
            result.put(cur.modelKey, next.predictValue - rootPredict);
            cur = next;
        }
        return result;
    }
}
