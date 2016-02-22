package splitter;

import criterion.SplitCriterion;
import instance.FeatureIndex;
import instance.Instance;
import model.GbdtParams;
import model.tree.GbdtNode;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RandomSplitter implements Splitter {

    public static Splitter getInstance() {
        return new RandomSplitter();
    }

    private RandomSplitter() {
        bestSplitFeatureIdx = -1;
        bestSplitThreshold = Double.NaN;
        greatestImprovement = 0;
    }

    private static final int SAMPLE_PER_FEATURE = 100;

    private volatile int bestSplitFeatureIdx;
    private volatile double bestSplitThreshold;
    private volatile double greatestImprovement;

    @Override
    public boolean split(GbdtParams params, GbdtNode node) {
        FeatureIndex featureIndex = params.getFeatureIndex();
        List<Instance> samples = node.includedInstances;

        ExecutorService executor = Executors.newFixedThreadPool(featureIndex.size());
        CountDownLatch latch = new CountDownLatch(featureIndex.size());
        for (int i = 0; i < featureIndex.size(); i++) {
            executor.submit(new ThreadSplitter(latch, samples, params, i));
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        executor.shutdownNow();

        if (bestSplitFeatureIdx < 0) {
            return false;
        }
        int firstRightIdx = 0;
        for (int i = firstRightIdx; i < samples.size(); i++) {
            if (samples.get(i).x[bestSplitFeatureIdx] <= bestSplitThreshold) {
                Collections.swap(samples, firstRightIdx++, i);
            }
        }
        node.featureIdx = bestSplitFeatureIdx;
        node.featureKey = featureIndex.name(bestSplitFeatureIdx);
        node.threshold = bestSplitThreshold;

        node.lessEqual = new GbdtNode(samples.subList(0, firstRightIdx));
        node.greater = new GbdtNode(samples.subList(firstRightIdx, samples.size()));

        return true;
    }

    private class ThreadSplitter implements Runnable {
        private final CountDownLatch latch;
        private final List<Instance> samples;
        private final GbdtParams params;
        private final int feature;
        private ThreadSplitter(CountDownLatch latch, List<Instance> samples, GbdtParams params, int feature) {
            this.latch = latch;
            this.samples = samples;
            this.params = params;
            this.feature = feature;
        }
        @Override
        public void run() {
            Random random = new Random();
            SplitCriterion criterion = params.getCriterion();
            double lastLoss = criterion.reset(samples);
            for (int i = 0; i < SAMPLE_PER_FEATURE; i++) {
                int idx = random.nextInt(samples.size());
                double curThreshold = samples.get(idx).x[feature];
                criterion.splitWithThreshold(feature, curThreshold);
                if (criterion.leftSize() >= params.getLeafMinNum() &&
                    criterion.rightSize() >= params.getLeafMinNum()) {
                    double curLoss = criterion.impurity();
                    if (lastLoss - curLoss > greatestImprovement) {
                        synchronized (RandomSplitter.this) {
                            if (lastLoss - curLoss > greatestImprovement) {
                                greatestImprovement = lastLoss - curLoss;
                                bestSplitFeatureIdx = feature;
                                bestSplitThreshold = curThreshold;
                            }
                        }
                    }
                }
            }
            latch.countDown();
        }
    }
}
