package model.tree;

import instance.Instance;
import model.GbdtParams;
import model.Model;
import splitter.SortSplitter;
import splitter.Splitter;
import splitter.SplitterFactory;
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
        AtomicInteger threadNum = new AtomicInteger(1);
        executor.submit(new ThreadTrainer(executor, root, 1, threadNum));
        while (threadNum.get() != 0) {
            Thread.sleep(1000);
        }
        executor.shutdownNow();
        System.out.println(root);
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

    private class ThreadTrainer implements Runnable {
        ThreadTrainer(ExecutorService executor, GbdtNode node, int depth, AtomicInteger threadNum) {
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
            if (depth+1 <= params.getMaxDepth() && split(node)) {
                threadNum.addAndGet(1);
                executor.submit(new ThreadTrainer(executor, node.greater, depth+1, threadNum));
                threadNum.addAndGet(1);
                executor.submit(new ThreadTrainer(executor, node.lessEqual, depth+1, threadNum));
            } else {
                node.value = params.getLoss().optimalEstimate(node.includedInstances);
            }
            threadNum.decrementAndGet();
        }
    }

    private boolean split(GbdtNode node) {
        Splitter splitter = params.getSplitter();
        splitter.init(params, node);
        return splitter.split();
    }
}
