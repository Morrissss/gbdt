package instance;

import utils.Pair;

import java.io.IOException;
import java.util.List;

public interface InstanceReader {

    Pair<FeatureIndex, List<Instance>> read() throws IOException;
}
