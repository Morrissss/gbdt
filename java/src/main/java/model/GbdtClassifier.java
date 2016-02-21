package model;

import instance.Instance;
import model.GbdtParams.GbdtParamsBuilder;
import model.tree.GbdtTree;

import java.util.ArrayList;
import java.util.List;

public class GbdtClassifier implements Model {

    public GbdtClassifier(GbdtParamsBuilder params) {
        this.params = params.build();
    }

    private final GbdtParams params;
    private List<GbdtTree> forest;
    private double initEstimate;

    @Override
    public void fit(List<Instance> samples) throws Exception {
        initSampleEstimates(samples);
        forest = new ArrayList<>(params.getTreeNum());
        for (int n = 0; n < params.getTreeNum(); n++) {
            calcSampleTargets(samples);
            GbdtTree tree = new GbdtTree(params);
            tree.fit(samples);
            calcSampleEstimates(samples, tree);
            forest.add(tree);
            System.out.println("Tree " + forest.size() + " built");
        }
    }

    @Override
    public double predict(Instance sample) {
        double estimate = initEstimate;
        for (GbdtTree tree : forest) {
            estimate += params.getLearningRate() * tree.predict(sample);
        }
        return params.getLoss().estimateToProb(estimate);
    }

    private void initSampleEstimates(List<Instance> samples) {
        initEstimate = params.getLoss().initEstimate(samples);
        for (Instance sample : samples) {
            sample.estimate = initEstimate;
        }
    }

    private void calcSampleTargets(List<Instance> samples) {
        for (Instance sample : samples) {
            sample.target = params.getLoss().instanceNegGradient(sample.estimate, sample.label);
        }
    }

    private void calcSampleEstimates(List<Instance> samples, GbdtTree tree) {
        for (Instance sample : samples) {
            sample.estimate += params.getLearningRate() * tree.predict(sample);
        }
    }
}
