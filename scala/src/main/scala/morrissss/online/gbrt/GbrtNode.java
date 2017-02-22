package morrissss.online.gbrt;

import morrissss.base.feature.ModelKey;

class GbrtNode {

    static GbrtNode parse(String line) {
        String[] parts = line.trim().split(" ");
        if (parts.length == 4) {
            ModelKey modelKey = ModelKey.parse(parts[0]);
            double threshold = Double.parseDouble(parts[1]);
            double predict = Double.parseDouble(parts[2]);
            double trainingFraction = Double.parseDouble(parts[3]);
            return new GbrtNode(modelKey, threshold, predict, trainingFraction);
        } else {
            return new GbrtNode(null, Double.NaN, Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
        }
    }

    public ModelKey modelKey;
    public double threshold;
    public GbrtNode left;
    public GbrtNode right;
    public double predictValue;
    public double trainingFraction;

    boolean isLeaf() {
        return modelKey == null;
    }

    private GbrtNode(ModelKey modelKey, double threshold, double predictValue, double trainingFraction) {
        this.modelKey = modelKey;
        this.threshold = threshold;
        this.predictValue = predictValue;
        this.trainingFraction = trainingFraction;
    }
}
