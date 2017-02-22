package morrissss.online.gbrt;

import com.alibaba.fastjson.JSON;

import java.util.List;

public class GbrtMetaInfo {

    public GbrtMetaInfo() {
    }

    public int treeNum;
    public int maxDepth;
    public double globalSampleRate;
    public double negSampleRate;
    public double learningRate;

    public List<String> modelKeys;

    public long posSampleNum;
    public long negSampleNum;

    public double trainPctr;
    public double trainRctr;
    public double testPctr;
    public double testRctr;
    public double trainAuc;
    public double testAuc;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
