package morrissss.online.gbrt;

import morrissss.base.feature.ModelKey;
import morrissss.base.util.MathUtils;
import morrissss.base.util.Pair;
import morrissss.online.util.Sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GbrtForest {

    public static GbrtForest parse(List<String> lines) {
        String[] ratios = lines.get(0).split("\\s+");
        Sampler sampler = new Sampler(Double.parseDouble(ratios[0]), Double.parseDouble(ratios[1]));

        String[] keys = lines.get(1).split("\\s+");
        ModelKey[] modelKeys = new ModelKey[keys.length];
        for (int i = 0; i < keys.length; i++) {
            modelKeys[i] = ModelKey.parse(keys[i]);
        }

        String[] treeNumDepth = lines.get(2).split("\\s+");
        int treeNum = Integer.parseInt(treeNumDepth[0]);
        GbrtTree[] trees = new GbrtTree[treeNum];

        int begLine = 3;
        for (int i = 0; i < treeNum; i++) {
            Pair<GbrtTree, Integer> pair = GbrtTree.parse(lines, begLine);
            trees[i] = pair.fst;
            begLine = pair.snd;
        }
        assert begLine == lines.size();
        return new GbrtForest(modelKeys, trees, sampler);
    }

    public double predict(Map<ModelKey, Double> features) {
        double weightedSum = 0;
        for (GbrtTree tree : trees) {
            weightedSum += tree.predict(features);
        }
        return sampler.calibrate(MathUtils.sigmoid(weightedSum));
    }

    public List<Pair<ModelKey, Double>> featureContribution(Map<ModelKey, Double> features) {
        Map<ModelKey, Double> weightedDiff = new HashMap<>();
        for (GbrtTree tree : trees) {
            for (Entry<ModelKey, Double> entry : tree.featureDiff(features).entrySet()) {
                ModelKey key = entry.getKey();
                double oldValue = weightedDiff.getOrDefault(key, 0.0);
                weightedDiff.put(key, oldValue + tree.weight*entry.getValue());
            }
        }
        List<Pair<ModelKey, Double>> result = new ArrayList<>(weightedDiff.size());
        weightedDiff.entrySet().forEach(entry -> result.add(Pair.of(entry.getKey(), entry.getValue())));
        Collections.sort(result, (p1, p2) -> -Double.compare(Math.abs(p1.snd), Math.abs(p2.snd)));
        return result;
    }

    public final ModelKey[] modelKeys;
    final GbrtTree[] trees;
    final Sampler sampler;

    private GbrtForest(ModelKey[] modelKeys, GbrtTree[] trees, Sampler sampler) {
        this.modelKeys = modelKeys;
        this.trees = trees;
        this.sampler = sampler;
    }
}
