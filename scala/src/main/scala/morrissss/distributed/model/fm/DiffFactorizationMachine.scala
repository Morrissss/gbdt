package morrissss.distributed.model.fm

import morrissss.base.feature.ModelKey
import org.apache.spark.SparkContext
import org.apache.spark.sql.hive.HiveContext

import scala.collection.mutable.Map

class DiffFactorizationMachine(override protected val name: String,
                               override val crossDimension: Int,
                               override val falseNegativeCost: java.util.Map[Int, Double],
                               override val falsePositiveCost: Double,
                               override val regularization: Double,
                               override val learningRate: Double,
                               override val randomWeight: Double = 0.0001,
                               override val initialNorm: Double = 10.0,
                               override val weightBound: Double = 10.0)
    extends FactorizationMachine(name, crossDimension, falseNegativeCost, falsePositiveCost, regularization,
                                 learningRate, randomWeight, initialNorm, weightBound) {

    override def dump(): java.util.Map[ModelKey, Array[Double]] = updateWeightWithG2s
}
