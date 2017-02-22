package morrissss.online.gbrt;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

public class GbrtDebug {

    public GbrtDebug() {
        this.evalData = new ArrayList<>();
        this.evalResult = new ArrayList<>();
    }

    public List<String> evalData;
    public List<Double> evalResult;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
