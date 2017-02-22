package morrissss.distributed.evaluate

import morrissss.online.util.Sampler
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.rdd.RDD

trait Evaluator extends Serializable {
    def evaluate(evalData: RDD[(Double, Double)]): java.util.Map[String, Double]

    private[evaluate] def absError(pred: Double, label: Double): Double = Math.abs(pred - label)
    private[evaluate] def sqrError(pred: Double, label: Double): Double = (pred - label) * (pred - label)
}

class BinaryLabelEvaluator(sampler: Sampler) extends Evaluator {
    override def evaluate(evalData: RDD[(Double, Double)]): java.util.Map[String, Double] = {
        val result = new java.util.HashMap[String, Double]()
        result.put("auc", new BinaryClassificationMetrics(evalData).areaUnderROC())
        result.put("mae", evalData.map{case (pred, label) => absError(sampler.distort(pred), label)}.mean())
        result.put("mse", evalData.map{case (pred, label) => sqrError(sampler.distort(pred), label)}.mean())
        result.put("pctr", evalData.map(predLabel => sampler.distort(predLabel._1)).mean())
        result.put("rctr", evalData.map(_._2).mean())
        result
    }
}

class MultiLabelEvaluator(val falseNegativeWeights: Map[Int, Double],
                          val falsePositiveWeight: Double) extends Evaluator {
    override def evaluate(evalData: RDD[(Double, Double)]): java.util.Map[String, Double] = {
        evalData.cache()
        val result = new java.util.HashMap[String, Double]()
        falseNegativeWeights.keys.foreach(l => {
            result.put("auc_" + l, new BinaryClassificationMetrics(evalData.filter(filterLabel(l))).areaUnderROC())
            result.put("pctr_" + l, evalData.filter(filterLabel(l)).map(_._1).mean())
            result.put("rctr_" + l, evalData.filter(filterLabel(l)).map(_._2).mean())
        })
        val weight = evalData.map(getWeight).sum()
        result.put("wmae", evalData.map{case (pred, label) => absError(pred, label)*getWeight((pred, label))}.sum() / weight)
        result.put("wmse", evalData.map{case (pred, label) => sqrError(pred, label)*getWeight((pred, label))}.sum() / weight)
        evalData.unpersist()
        result
    }

    private def filterLabel(target: Int)(predLabel: (Double, Double)): Boolean =
        predLabel._2 == 0 || predLabel._2 == target.toDouble
    private def getWeight(predLabel: (Double, Double)): Double =
        if (predLabel._2 == 0.0) 1. else falseNegativeWeights.getOrElse(predLabel._2.toInt, 1.)
}
