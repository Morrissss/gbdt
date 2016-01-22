package impurity;

import java.util.List;

public interface SplitCriterion<T> {

    double calc(List<T> labels);
}
