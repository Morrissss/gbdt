package morrissss.distributed

import java.util.Collections

import org.apache.spark.sql.Row

trait RowConverter extends Serializable {
    def functionMapping(row: Row): Array[Double]
    def possibleModelKeys(): Array[ModelKey]
}

class TreeModelRowConverter(val functions: Array[FeatureFunction]) extends RowConverter {

    override def functionMapping(row: Row): Array[Double] = {
        val features = new java.util.HashMap[String, Object]
        for ((name, _) <- RawFeatureMeta.FEATURE_METAS) {
            features.put(name, row.getAs(name))
        }
        val featureMap: java.util.Map[ModelKey, java.lang.Double] = new java.util.HashMap[ModelKey, java.lang.Double]()
        functions.foreach(f => f.extract(features, featureMap))
        val iter = featureMap.entrySet().iterator()

        val featureList = new java.util.ArrayList[KeyCompPair[ModelKey, Double]]()
        while (iter.hasNext) {
            val next: java.util.Map.Entry[ModelKey, java.lang.Double] = iter.next
            featureList.add(new KeyCompPair[ModelKey, Double](next.getKey, next.getValue))
        }
        Collections.sort(featureList)
        val result = new Array[Double](featureList.size())
        for (i <- 0 until featureList.size()) {
            result(i) = featureList.get(i).snd
        }
        result
    }

    def debug(row: Row): java.util.ArrayList[KeyCompPair[ModelKey, Double]] = {
        val features = new java.util.HashMap[String, Object]
        for ((name, _) <- RawFeatureMeta.FEATURE_METAS) {
            features.put(name, row.getAs(name))
        }
        val featureMap: java.util.Map[ModelKey, java.lang.Double] = new java.util.HashMap[ModelKey, java.lang.Double]()
        functions.foreach(f => f.extract(features, featureMap))
        val iter = featureMap.entrySet().iterator()

        val featureList = new java.util.ArrayList[KeyCompPair[ModelKey, Double]]()
        while (iter.hasNext) {
            val next: java.util.Map.Entry[ModelKey, java.lang.Double] = iter.next
            featureList.add(new KeyCompPair[ModelKey, Double](next.getKey, next.getValue))
        }
        Collections.sort(featureList)
        featureList
    }

    override val possibleModelKeys: Array[ModelKey] = {
        val tmp = new java.util.ArrayList[ModelKey]()
        functions.foreach(f => {
            val iter = f.possibleKeys().iterator
            while (iter.hasNext) {
                tmp.add(iter.next)
            }
        })
        Collections.sort(tmp)
        val result = new Array[ModelKey](tmp.size())
        for (i <- 0 until tmp.size()) {
            result(i) = tmp.get(i)
        }
        result
    }
}
