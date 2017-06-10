package morrissss.distributed.data.master

import morrissss.distributed.data.partition.SplitInfo
import morrissss.distributed.data.partition.histogram.HistogramEntry
import morrissss.distributed.model.ModelConfig

import scala.collection.Searching.search
import scala.collection.mutable.ArrayBuffer

abstract class BinMapper() extends Serializable {
    val numBins: Short
    def valueToBin(value: Double): Short
    def findOptimalSplit(entries: Array[HistogramEntry], modelConfig: ModelConfig): Option[SplitInfo]

    /**
      * loss diff = $\sum{G_leaf} \cdot w + \frac{1}{2}\sum{H_leaf} \cdot w^2 + \lambda_1 \cdot |w| + \lambda_2 \cdot w^2$
      * where $w$ is the optimal value of the leaf
      * if $\sum{G_leaf} < -\lambda_1$, then $w > 0$
      * if $\sum{G_leaf} > \lambda_1$, then $w < 0$
      * otherwise $w = 0$
      */
    def leafSplitScore(sumGradients: Double, sumHessians: Double, modelConfig: ModelConfig): Double = {
        val absSumGradients = math.abs(sumGradients)
        val regAbsSumGradients = math.max(0.0, absSumGradients - modelConfig.lambdaL1)
        regAbsSumGradients * regAbsSumGradients / (2 * (sumHessians + modelConfig.lambdaL2))
    }

    def optimalValue(sumGradients: Double, sumHessians: Double, modelConfig: ModelConfig): Double = {
        -sumGradients / (sumHessians + modelConfig.lambdaL2)
    }
}

class NumericBinMapper(private val upperInclusive: Array[Double],
                       val zeroIdx: Int) extends BinMapper {
    override val numBins: Short = upperInclusive.length.toShort

    override def valueToBin(value: Double): Short = upperInclusive.search(value).insertionPoint.toShort

    override def findOptimalSplit(entries: Array[HistogramEntry], modelConfig: ModelConfig): Option[SplitInfo] = {
        val sumCount = entries.map(_.count).sum
        val sumGrad = entries.map(_.sumGradients).sum
        val sumHess = entries.map(_.sumHessians).sum
        var lteCount = entries(0).count
        var lteSumGrad = entries(0).sumGradients
        var lteSumHess = entries(0).sumHessians
        var gtCount = sumCount - lteCount
        var gtSumGrad = sumGrad - lteSumGrad
        var gtSumHess = sumHess - lteSumHess

        var bestSplit = 0.toShort
        var bestSplitScores = (leafSplitScore(lteSumGrad, lteSumHess, modelConfig),
                               leafSplitScore(gtSumGrad, gtSumHess, modelConfig))
        var bestSplitValues = (optimalValue(lteSumGrad, lteSumHess, modelConfig),
                               optimalValue(gtSumGrad, gtSumHess, modelConfig))
        var bestLeftStats = (lteCount, lteSumGrad, lteSumHess)

        for (i <- 1 until numBins-1) {
            lteCount += entries(i).count
            lteSumGrad += entries(i).sumGradients
            lteSumHess += entries(i).sumHessians
            gtCount = sumCount - lteCount
            gtSumGrad = sumGrad - lteSumGrad
            gtSumHess = sumHess - lteSumHess
            val leftScore = leafSplitScore(lteSumGrad, lteSumHess, modelConfig)
            val rightScore = leafSplitScore(gtSumGrad, gtSumHess, modelConfig)
            val splitScore = leftScore + rightScore
            if (splitScore > bestSplitScores._1+bestSplitScores._2) {
                bestSplit = i.toShort
                bestSplitScores = (leftScore, rightScore)
                bestSplitValues = (optimalValue(lteSumGrad, lteSumHess, modelConfig),
                                   optimalValue(gtSumGrad, gtSumHess, modelConfig))
                bestLeftStats = (lteCount, lteSumGrad, lteSumHess)
            }
        }

        val leafScore = leafSplitScore(sumGrad, sumHess, modelConfig)
        if (bestSplitScores._1+bestSplitScores._2 >= leafScore + modelConfig.minSplitGain) {
            Some(new SplitInfo(bestSplit, bestSplitScores._1 + bestSplitScores._2 - leafScore,
                               bestSplitValues._1, bestSplitValues._2,
                               bestLeftStats._1, sumCount-bestLeftStats._1,
                               bestLeftStats._2, sumGrad-bestLeftStats._2,
                               bestLeftStats._3, sumHess-bestLeftStats._3))
        } else {
            None
        }
    }
}

// 0 -> numBins-1
class CategoricalBinMapper(override val numBins: Short) extends BinMapper {
    override def valueToBin(value: Double): Short = value.toShort

    override def findOptimalSplit(entries: Array[HistogramEntry], modelConfig: ModelConfig): Option[SplitInfo] = {
        val sumCount = entries.map(_.count).sum
        val sumGrad = entries.map(_.sumGradients).sum
        val sumHess = entries.map(_.sumHessians).sum

        var lteSumGrad = entries(0).sumGradients
        var lteSumHess = entries(0).sumHessians

        var bestSplit = 0.toShort
        var bestSplitScores = (leafSplitScore(lteSumGrad, lteSumHess, modelConfig),
                               leafSplitScore(sumGrad - lteSumGrad, sumHess - lteSumHess, modelConfig))
        var bestSplitValues = (optimalValue(lteSumGrad, lteSumHess, modelConfig),
                               optimalValue(sumGrad - lteSumGrad, sumHess - lteSumHess, modelConfig))

        for (i <- 1 until numBins-1) {
            lteSumGrad = entries(i).sumGradients
            lteSumHess = entries(i).sumHessians
            val leftScore = leafSplitScore(lteSumGrad, lteSumHess, modelConfig)
            val rightScore = leafSplitScore(sumGrad-lteSumGrad, sumHess-lteSumHess, modelConfig)
            val splitScore = leftScore + rightScore
            if (splitScore > bestSplitScores._1+bestSplitScores._2) {
                bestSplit = i.toShort
                bestSplitScores = (leftScore, rightScore)
                bestSplitValues = (optimalValue(lteSumGrad, lteSumHess, modelConfig),
                                   optimalValue(sumGrad - lteSumGrad, sumHess - lteSumHess, modelConfig))
            }
        }

        val leafScore = leafSplitScore(sumGrad, sumHess, modelConfig)
        if (bestSplitScores._1+bestSplitScores._2 >= leafScore + modelConfig.minSplitGain) {
            Some(new SplitInfo(bestSplit, bestSplitScores._1 + bestSplitScores._2 - leafScore,
                               bestSplitValues._1, bestSplitValues._2,
                               entries(bestSplit).count, sumCount-entries(bestSplit).count,
                               entries(bestSplit).sumGradients, sumGrad-entries(bestSplit).sumGradients,
                               entries(bestSplit).sumHessians, sumHess-entries(bestSplit).sumHessians))
        } else {
            None
        }
    }
}

object BinMapper {
    def constructNumeric(values: Array[Double], maxBin: Short): BinMapper = {
        val sorted = values.sorted
        val leftValues = new ArrayBuffer[Double]
        val leftCounts = new ArrayBuffer[Int]
        val rightValues = new ArrayBuffer[Double]
        val rightCounts = new ArrayBuffer[Int]
        var zeroCount = 0

        var curCount = 0
        for (value <- sorted) {
            if (value < -1e-6) {
                if (leftValues.isEmpty || leftValues.last < value) {
                    leftValues += value
                    leftCounts += curCount
                    curCount = 1
                } else {
                    curCount += 1
                }
            } else if (value > 1e-6) {
                if (rightValues.isEmpty || rightValues.last < value) {
                    rightValues += value
                    rightCounts += curCount
                    curCount = 1
                } else {
                    curCount += 1
                }
            } else {
                zeroCount += 1
            }
        }
        val leftTotal = leftCounts.sum.toDouble
        val leftMaxBin = (leftTotal / (values.length - zeroCount) * (maxBin-1)).toShort
        val upperBounds = calcUpperBounds(leftValues.toArray, leftCounts.toArray, leftMaxBin, 0)
        val zeroIdx = if (zeroCount != 0) {
            val result = upperBounds.length
            upperBounds += -1e-6
            upperBounds += 1e-6
            result
        } else {
            -1
        }
        upperBounds ++= calcUpperBounds(rightValues.toArray, rightCounts.toArray, maxBin-1-leftMaxBin, 0)
        new NumericBinMapper(upperBounds.toArray, zeroIdx)
    }

    def constructCategorical(maxCategory: Short): BinMapper = new CategoricalBinMapper(maxCategory)

    private[BinMapper] def calcUpperBounds(values: Array[Double], counts: Array[Int],
                                           expectedMaxBin: Int, minBinSize: Int): ArrayBuffer[Double] = {
        val total = counts.sum
        val thresholds = ArrayBuffer[Double]()
        if (values.length <= expectedMaxBin) {
            var curBinSize = 0
            for (i <- 0 until values.length-1) {
                val curValue = values(i)
                val curCount = counts(i)
                curBinSize += curCount
                if (curBinSize >= minBinSize) {
                    thresholds += (curValue + values(i+1)) / 2
                    curBinSize = 0
                }
            }
            thresholds += Double.PositiveInfinity
        } else {
            val maxBin = math.max(1, math.min(expectedMaxBin, total / minBinSize))
            var expectBinSize = total.toDouble / maxBin

            var restBinCount = maxBin
            var restSampleCount = total
            val isHighFreq = for (i <- 0 until counts.length) yield {
                val count = counts(i)
                if (count >= expectBinSize) {
                    restBinCount -= 1
                    restSampleCount -= count
                    true
                } else {
                    false
                }
            }
            expectBinSize = restSampleCount.toDouble / restBinCount

            val lowerBounds = ArrayBuffer[Double]()
            val upperBounds = ArrayBuffer[Double]()
            lowerBounds += values(0)
            var curBinSize = 0
            for (i <- 0 until values.length-1) {
                val count = counts(i)
                if (!isHighFreq(i)) {
                    restSampleCount -= count
                }
                curBinSize += count
                // need a new bin
                if (isHighFreq(i) || curBinSize >= expectBinSize ||
                    (isHighFreq(i+1) && curBinSize >= math.max(1.0, expectBinSize * 0.5))) {
                    upperBounds += values(i)
                    lowerBounds += values(i+1)
                    curBinSize = 0
                    if (!isHighFreq(i)) {
                        restBinCount -= 1
                        expectBinSize = restSampleCount / restBinCount.toDouble
                    }
                }
            }
            for (i <- 0 until math.min(upperBounds.length, maxBin-1)) {
                thresholds += (upperBounds(i) + lowerBounds(i+1)) / 2.0
            }
        }
        thresholds
    }
}