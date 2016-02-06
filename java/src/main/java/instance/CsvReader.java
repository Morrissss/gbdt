package instance;

import utils.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvReader implements InstanceReader {

    public CsvReader(String path, String sep) {
        this.path = path;
        this.sep = sep;
    }
    private String path;
    private String sep;

    public Pair<FeatureIndex, List<Instance>> read() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            String[] entries = line.split(sep);
            String[] featureNames = new String[entries.length-1];
            for (int i = 1; i < entries.length; i++) {
                featureNames[i-1] = entries[i];
            }
            FeatureIndex featureIndex = new FeatureIndex(featureNames);
            List<Instance> samples = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                entries = line.split(sep);
                int label = Integer.parseInt(entries[0]);
                double x[] = new double[featureIndex.size()];
                for (int i = 0; i < x.length; i++) {
                    x[i] = Double.parseDouble(entries[i+1]);
                }
                samples.add(new Instance(x, label));
            }
            return Pair.of(featureIndex, samples);
        }
    }
}
