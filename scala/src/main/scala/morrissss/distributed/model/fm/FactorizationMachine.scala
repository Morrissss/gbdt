package morrissss.distributed.model.fm

import java.util.concurrent.ThreadLocalRandom

import morrissss.base.feature.ModelKey
import morrissss.base.util.{KeyCompPair, MathUtils}
import morrissss.distributed.model.distributed.Model
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ArrayBuffer

class FactorizationMachine(override protected val name: String,
                           protected val crossDimension: Int,
                           protected val falseNegativeCost: java.util.Map[Int, Double],
                           protected val falsePositiveCost: Double,
                           protected val regularization: Double,
                           protected val learningRate: Double,
                           protected val randomWeight: Double = 0.0001,
                           protected val initialNorm: Double = 10.0,
                           protected val weightBound: Double = 10.0)
    extends Model[(java.util.List[KeyCompPair[ModelKey, Double]], Int), java.util.Map[ModelKey, Array[Double]]] {

    protected var originWeightWithG2s: java.util.Map[ModelKey, Array[Double]] = null
    protected var updateWeightWithG2s: java.util.Map[ModelKey, Array[Double]] = null

    override def train(dataSet: RDD[(java.util.List[KeyCompPair[ModelKey, Double]], Int)]): RDD[(Double, Double)] = {
        val count = dataSet.count().toInt
        val weights = Array.fill[Double](count / 100000 + 1)(1.0)
        val evalData = new ArrayBuffer[RDD[(Double, Double)]](weights.length)
//        for (sampleSplit: RDD[(java.util.List[KeyCompPair[ModelKey, Double]], Int)] <- dataSet.randomSplit(weights)) {
//            val eval = new ArrayBuffer[(Double, Double)]
//            for (sample <- sampleSplit.collect()) {
//                val pctr: Double = predictAndUpdateWeights(sample._1, sample._2)
//                eval.append((pctr, sample._2))
//            }
//            evalData.append(sparkContext.makeRDD(eval))
//        }
        System.out.println("Updated Weights: " + updateWeightWithG2s.size)
        dataSet.sparkContext.union(evalData)
    }

    override def predict(testSet: RDD[(java.util.List[KeyCompPair[ModelKey, Double]], Int)]): RDD[(Double, Double)] = {
        val count = testSet.count().toInt
        val weights = Array.fill[Double](count / 100000 + 1)(1.0)
        val evalData = new ArrayBuffer[RDD[(Double, Double)]](weights.length)
//        for (sampleSplit: RDD[(java.util.List[KeyCompPair[ModelKey, Double]], Int)] <- testSet.randomSplit(weights)) {
//            val eval = new ArrayBuffer[(Double, Double)]
//            for (sample <- sampleSplit.collect()) {
//                val pctr: Double = calculatePctrAndNegGradient(sample._1, sample._2)._1
//                eval.append((pctr, sample._2))
//            }
//            evalData.append(sparkContext.makeRDD(eval))
//        }
        System.out.println("Updated Weights: " + updateWeightWithG2s.size)
        testSet.sparkContext.union(evalData)
    }

    override def dump(): java.util.Map[ModelKey, Array[Double]] = originWeightWithG2s

    override def load(weights: java.util.Map[ModelKey, Array[Double]]): Unit = {
        originWeightWithG2s = weights
        updateWeightWithG2s = new java.util.HashMap()
    }

    override def featureImportance(limit: Int): Array[(ModelKey, Double)] = Array.empty

    override def dumpHdfs(sparkContext: SparkContext, dir: String): Unit = throw new UnsupportedOperationException

    override def loadHdfs(sparkContext: SparkContext, dir: String): Unit = throw new UnsupportedOperationException

    private[fm] def predictAndUpdateWeights(features: java.util.List[KeyCompPair[ModelKey, Double]], label: Int): Double = {
        val pctrNegGrad = calculatePctrAndNegGradient(features, label)
        val pctr: Double = pctrNegGrad._1
        val iter = pctrNegGrad._2.entrySet().iterator()
        while (iter.hasNext) {
            val negGrads = iter.next()
            val featureKey: ModelKey = negGrads.getKey
            val negGrad: Array[Double] = negGrads.getValue
            val weights: Array[Double] = updateWeightWithG2s.get(featureKey)
            for (i <- 0 until weights.length / 2) {
                val ng: Double = negGrad(i)
                val diff: Double = learningRate * ng * Math.sqrt(initialNorm / (initialNorm + weights(i * 2 + 1)))
                weights(i * 2) = bound(weights(i * 2) + diff)
                weights(i * 2 + 1) += ng * ng
            }
        }
        pctr
    }

    /**
      * @return without G2
      */
    private[fm] def calculatePctrAndNegGradient(features: java.util.List[KeyCompPair[ModelKey, Double]], label: Int): (Double, java.util.Map[ModelKey, Array[Double]]) = {
        var wSum = 0.0
        val vSum = new Array[Double](crossDimension) // vSum = \sum{v_i \times x_i}
        var v2Sum = 0.0 // v2Sum = \sum{v_i^T v_i \times x_i^2}

        val iter1 = features.iterator
        while (iter1.hasNext) {
            val pair = iter1.next
            val modelKey = pair.fst
            val value: Double = pair.snd
            val weight = if (updateWeightWithG2s.containsKey(modelKey)) {
                updateWeightWithG2s.get(modelKey)
            } else {
                val temp = originWeightWithG2s.getOrDefault(modelKey, initWeight)
                updateWeightWithG2s.put(modelKey, temp)
                temp
            }
            wSum += value * weight(0)
            for (i <- 1 until crossDimension+1) {
                val vw: Double = value * weight(2 * i)
                vSum(i - 1) += vw
                v2Sum += vw * vw
            }
        }
        val crossSum: Double = vSum.map(v => v*v).sum
        val margin: Double = wSum + (crossSum - v2Sum) / 2
        val pctr: Double = MathUtils.sigmoid(margin)
        val negExpMargin: Double = Math.exp(-margin)
        val globalNegGrad: Double = if (label == 0) {
            -falsePositiveCost / (1 + negExpMargin)
        } else {
            falseNegativeCost.get(label) * (1 - 1 / (1 + negExpMargin)) // inf / inf = NaN
        }

        val negGradients = new java.util.HashMap[ModelKey, Array[Double]]()
        val iter2 = features.iterator
        while (iter2.hasNext) {
            val feature = iter2.next
            val modelKey = feature.fst
            val value = feature.snd
            val weight: Array[Double] = updateWeightWithG2s.get(modelKey)
            val negGradient: Array[Double] = if (negGradients.containsKey(modelKey)) {
                negGradients.get(modelKey)
            } else {
                val temp = Array.fill(crossDimension+1)(0.0)
                negGradients.put(modelKey, temp)
                temp
            }
            val wDiff: Double = globalNegGrad * value
            negGradient(0) += wDiff - regularization * negGradient(0)
            for (i <- 1 until crossDimension + 1) {
                val vDiff: Double = globalNegGrad * value * (vSum(i - 1) - value * weight(2 * i))
                negGradient(i) += vDiff - regularization * negGradient(i)
            }
        }
        (pctr, negGradients)
    }

    private def initWeight: Array[Double] = {
        val result: Array[Double] = new Array[Double](2 * crossDimension + 2)
        for (i <- 0 until crossDimension+1) {
            result(2 * i) = ThreadLocalRandom.current.nextDouble(-randomWeight, randomWeight)
            result(2 * i + 1) = 0
        }
        result
    }

    private def bound(value: Double): Double = if (value > weightBound) weightBound
                                               else if (value < -weightBound) -weightBound
                                               else value
}
