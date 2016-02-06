package model;

import instance.CsvReader;
import instance.FeatureIndex;
import instance.Instance;
import org.junit.Test;
import utils.Pair;

import java.util.List;

public class GbdtTest {

    @Test
    public void testFit() throws Exception {
        Pair<FeatureIndex, List<Instance>> p =
                new CsvReader("/home/morris/Github/gbdt/test", ",").read();
        FeatureIndex featureIndex = p.first;
        List<Instance> samples = p.second;

        GbdtClassifier model = new GbdtClassifier(new GbdtParams.GbdtParamsBuilder(featureIndex).setTreeNum(5)
                                                                                                .setDepth(3)
                                                                                                .setLeafMinNum(1));
        model.fit(samples);
        for (Instance sample : samples) {
            System.out.println(sample + " " + model.predict(sample));
        }
    }
}
