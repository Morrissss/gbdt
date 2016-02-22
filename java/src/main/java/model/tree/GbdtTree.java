package model.tree;

import instance.Instance;
import model.GbdtParams;
import model.Model;
import utils.Pair;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class GbdtTree implements Model {

    public GbdtTree(GbdtParams params) {
        this.params = params;
    }

    private final GbdtParams params;
    private GbdtNode root;

    @Override
    public void fit(List<Instance> samples) throws Exception {
        for (Instance sample : samples) {
            sample.target = params.getLoss().instanceNegGradient(sample.estimate, sample.label);
        }
        root = new GbdtNode(samples);
        params.getCriterion().reset(samples);
        ExecutorService executor = Executors.newFixedThreadPool(params.getThreadNum());
        executor.submit(new ThreadTrainer(executor, root));
        synchronized (executor) {
            executor.wait();
        }
        executor.shutdownNow();
    }

    @Override
    public double predict(Instance sample) {
        GbdtNode cur = root;
        int path = PathUtils.init();
        while (!cur.isLeaf()) {
            Pair<GbdtNode, Integer> nextNodePath = cur.next(sample, path);
            cur = nextNodePath.first;
            path = nextNodePath.second;
        }
        return cur.value;
    }

    @Override
    public String toString() {
        return root.toString();
    }

    private class ThreadTrainer implements Runnable {
        ThreadTrainer(ExecutorService executor, GbdtNode node) {
            this.executor = executor;
            this.node = node;
            this.depth = 0;
            this.threadNum = new AtomicInteger(1);
        }
        private ThreadTrainer(ExecutorService executor, GbdtNode node, int depth, AtomicInteger threadNum) {
            this.executor = executor;
            this.node = node;
            this.depth = depth;
            this.threadNum = threadNum;
        }
        private final ExecutorService executor;
        private final GbdtNode node;
        private final int depth;
        private final AtomicInteger threadNum;
        @Override
        public void run() {
            if (depth+1 <= params.getMaxDepth() && params.getSplitter().split(params, node)) {
                System.out.println(depth + ": " + node.featureKey + "@" + node.threshold);
                System.out.flush();
                threadNum.addAndGet(1);
                executor.submit(new ThreadTrainer(executor, node.greater, depth+1, threadNum));
                threadNum.addAndGet(1);
                executor.submit(new ThreadTrainer(executor, node.lessEqual, depth+1, threadNum));
            } else {
                node.value = params.getLoss().optimalEstimate(node.includedInstances);
            }
            if (threadNum.decrementAndGet() ==0) {
                synchronized (executor) {
                    executor.notifyAll();
                }
            }
        }
    }

}
