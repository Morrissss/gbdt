package morrissss.distributed.data.master

import morrissss.distributed.data.partition.histogram.HistogramEntry
import morrissss.distributed.model.ModelConfig

import scala.collection.Searching.{Found, InsertionPoint, search}
import scala.collection.mutable.ArrayBuffer

abstract class BinMapper() extends Serializable {
    val numBins: Short
    def valueToBin(value: Double): Short
    def binToValue(bin: Short): Double
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

class DenseBinMapper(private val upperInclusive: Array[Double]) extends BinMapper {
    override val numBins: Short = upperInclusive.length.toShort

    override def valueToBin(value: Double): Short = {
        upperInclusive.search(value) match {
            case Found(idx) => idx.toShort
            case InsertionPoint(idx) => if (idx == numBins) (numBins-1).toShort else idx.toShort
        }
    }

    override def binToValue(bin: Short): Double = upperInclusive(bin)

    override def findOptimalSplit(entries: Array[HistogramEntry], modelConfig: ModelConfig): Option[SplitInfo] = {
        val sumGrad = entries.map(_.sumGradients).sum
        val sumHess = entries.map(_.sumHessians).sum
        var lteSumGrad = entries(0).sumGradients
        var lteSumHess = entries(0).sumHessians
        var gtSumGrad = sumGrad - lteSumGrad
        var gtSumHess = sumHess - gtSumHess

        var bestSplit = 0.toShort

        var bestSplitScore = leafSplitScore(lteSumGrad, lteSumHess, modelConfig) +
                             leafSplitScore(gtSumGrad, gtSumHess, modelConfig)

        for (i <- 1 until numBins-1) {
            lteSumGrad += entries(i).sumGradients
            lteSumHess += entries(i).sumHessians
            gtSumGrad = sumGrad - lteSumGrad
            gtSumHess = sumHess - lteSumHess
            val splitScore = leafSplitScore(lteSumGrad, lteSumHess, modelConfig) +
                             leafSplitScore(gtSumGrad, gtSumHess, modelConfig)
            if (splitScore > bestSplitScore) {
                bestSplit = i.toShort
                bestSplitScore = splitScore
            }
        }

        val leafScore = leafSplitScore(sumGrad, sumHess, modelConfig)
        if (bestSplitScore >= leafScore + modelConfig.minSplitGain) {
            Some(new DenseSplitInfo(bestSplit, bestSplitScore - leafScore))
        } else {
            None
        }
    }
}

class SparseBinMapper(private val upperInclusive: Array[Double]) extends BinMapper {
    override val numBins: Short = (upperInclusive.length + 1).toShort

    private val zeroBin = upperInclusive.length.toShort

    override def valueToBin(value: Double): Short = {
        if (value == 0.0) {
            zeroBin
        } else {
            upperInclusive.search(value) match {
                case Found(idx) => idx.toShort
                case InsertionPoint(idx) => if (idx == zeroBin) (zeroBin-1).toShort else idx.toShort
            }
        }
    }

    override def binToValue(bin: Short): Double = {
        if (bin == zeroBin) {
            0.0
        } else {
            upperInclusive(bin)
        }
    }

    override def findOptimalSplit(entries: Array[HistogramEntry], modelConfig: ModelConfig): Option[SplitInfo] = {
        val zeroGrad = entries(zeroBin).sumGradients
        val zeroHess = entries(zeroBin).sumHessians
        val sumGrad = entries.map(_.sumGradients).sum - zeroGrad
        val sumHess = entries.map(_.sumHessians).sum - zeroHess

        var lteSumGrad = 0
        var lteSumHess = 0
        var gtSumGrad = sumGrad
        var gtSumHess = sumHess

        var bestZeroToLte = true
        var bestSplit: Short = -1
        var bestSplitScore = leafSplitScore(lteSumGrad+zeroGrad, lteSumHess+zeroHess, modelConfig) +
                             leafSplitScore(gtSumGrad, gtSumHess, modelConfig)

        for (i <- 0 until zeroBin-1) {
            lteSumGrad += entries(i).sumGradients
            lteSumHess += entries(i).sumHessians
            gtSumGrad = sumGrad - lteSumGrad
            gtSumHess = sumHess - lteSumHess
            val zeroToLeftSplitScore = leafSplitScore(lteSumGrad+zeroGrad, lteSumHess+zeroHess, modelConfig) +
                                       leafSplitScore(gtSumGrad, gtSumHess, modelConfig)
            if (zeroToLeftSplitScore > bestSplitScore) {
                bestZeroToLte = true
                bestSplit = i.toShort
                bestSplitScore = zeroToLeftSplitScore
            }
            val zeroToRightSplitScore = leafSplitScore(lteSumGrad, lteSumHess, modelConfig) +
                                        leafSplitScore(gtSumGrad+zeroGrad, gtSumHess+zeroHess, modelConfig)
            if (zeroToRightSplitScore > bestSplitScore) {
                bestZeroToLte = false
                bestSplit = i.toShort
                bestSplitScore = zeroToRightSplitScore
            }
        }

        val splitScore = leafSplitScore(lteSumGrad, lteSumHess, modelConfig) +
                         leafSplitScore(gtSumGrad+zeroGrad, gtSumHess+zeroHess, modelConfig)
        if (splitScore > bestSplitScore) {
            bestZeroToLte = false
            bestSplit = (zeroBin-1).toShort
            bestSplitScore = splitScore
        }

        val leafScore = leafSplitScore(sumGrad, sumHess, modelConfig)
        if (bestSplitScore >= leafScore + modelConfig.minSplitGain) {
            Some(new SparseSplitInfo(bestSplit, bestZeroToLte, bestSplitScore - leafScore))
        } else {
            None
        }
    }
}

// 0 -> numBins-1
class CategoricalBinMapper(override val numBins: Short) extends BinMapper {
    override def valueToBin(value: Double): Short = value.toShort
    override def binToValue(bin: Short): Double = bin.toDouble

    override def findOptimalSplit(entries: Array[HistogramEntry], modelConfig: ModelConfig): Option[SplitInfo] = {

    }
}

object BinMapper {
    def constructDense(values: Array[Double], maxBin: Short): BinMapper = {
        val sorted: Array[Double] = values.sorted
        new DenseBinMapper(constructNumeric(sorted, maxBin))
    }

    def constructSparse(nonZeroValues: Array[Double], maxBin: Short): BinMapper = {
        val sorted = nonZeroValues.sorted
        // one extra for 0
        val arr = constructNumeric(sorted, (maxBin - 1).toShort)
        new SparseBinMapper(arr)
    }

    private[bin] def constructNumeric(sorted: Array[Double], maxBin: Short): Array[Double] = {
        val distinctValues = ArrayBuffer[Double](sorted(0))
        val valueCount = ArrayBuffer[Int](1)
        var lastValue: Double = null
        sorted.drop(1).foreach(value => {
            if (value != lastValue) {
                distinctValues += value
            }
            valueCount(valueCount.length-1) += 1
        })
        // save one extra bin for (max, +inf)
        if (distinctValues.length < maxBin) {
            distinctValues.sliding(2).map(t => (t(0)+t(1))/2).toArray
        } else {
            // stride between splits
            val stride = sorted.length.toDouble / (maxBin-1)
            // iterate `valueCount` to find splits
            val splitsBuilder = Array.newBuilder[Double]
            var index = 1
            var lastSum = valueCount(0)
            var targetSum = stride
            for (index <- 1 until distinctValues.length) {
                val currSum = lastSum + valueCount(index)
                val previousGap = math.abs(lastSum - targetSum)
                val currentGap = math.abs(currSum - targetSum)
                if (previousGap < currentGap) {
                    splitsBuilder += (distinctValues(index - 1) + distinctValues(index)) / 2
                    targetSum += stride
                }
                lastSum = currSum
            }
            splitsBuilder.result
        }
    }

    def constructCategorical(maxCategory: Short): BinMapper = new CategoricalBinMapper(maxCategory)
}