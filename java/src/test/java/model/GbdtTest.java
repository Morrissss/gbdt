package model;

import instance.CsvReader;
import instance.FeatureIndex;
import instance.Instance;
import model.GbdtParams.GbdtParamsBuilder;
import org.junit.Test;
import utils.MathUtils;
import utils.Pair;

import java.util.List;

public class GbdtTest {

    @Test
    public void testFit() throws Exception {
        Pair<FeatureIndex, List<Instance>> p =
                    new CsvReader("/home/morris/github/gbdt/test.csv", ",").read();
        FeatureIndex featureIndex = p.first;
        List<Instance> samples = p.second;

        GbdtClassifier model = new GbdtClassifier(
                                    new GbdtParamsBuilder(featureIndex).setTreeNum(5)
                                                                       .setDepth(2)
                                                                       .setLeafMinNum(5)
                                                                       .setLearningRate(0.5)
                                                                       .setLoss("log")
                                                                       .setCriterion("mse")
                                                                       .setSplitter("sort")
                                                                       .setThreadNum(4)
        );

        model.fit(samples);

        System.out.println(MathUtils.auc(samples, model));
    }
}
