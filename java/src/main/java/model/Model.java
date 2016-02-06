package model;

import instance.Instance;

import java.util.List;

public interface Model {

    void fit(List<Instance> samples) throws Exception;

    /**
     * @param sample not modified
     */
    double predict(Instance sample);
}
