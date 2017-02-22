package morrissss.distributed.model

import morrissss.base.feature.ModelKey
import morrissss.base.util.KeyCompPair
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

trait Model[D, S] extends Serializable {

    protected val name: String

    /**
      * @param dataSet
      * @return (prediction, label)
      */
    def train(dataSet: RDD[D]): RDD[(Double, Double)]
    /**
      * @param testSet
      * @return (prediction, label)
      */
    def predict(testSet: RDD[D]): RDD[(Double, Double)]

    def dump(): S
    def load(weights: S): Unit
    def featureImportance(limit: Int): Array[(ModelKey, Double)]

    def dumpHdfs(sparkContext: SparkContext, dir: String): Unit
    def loadHdfs(sparkContext: SparkContext, dir: String): Unit
}
