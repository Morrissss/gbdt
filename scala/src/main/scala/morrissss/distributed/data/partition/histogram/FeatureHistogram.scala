package morrissss.distributed.data.partition.histogram

import morrissss.distributed.data.master.{BinMapper, FeatureMeta}
import morrissss.distributed.data.partition.SplitInfo

class FeatureHistogram(private val featureMeta: FeatureMeta,
                       private val lambdaL1: Double,
                       private val lambdaL2: Double,
                       private val splitGain: Double) {

    /**
      * loss diff = $\sum{G_leaf} \cdot w + \frac{1}{2}\sum{H_leaf} \cdot w^2 + \lambda_1 \cdot |w| + \lambda_2 \cdot w^2$
      * where $w$ is the optimal value of the leaf
      * if $\sum{G_leaf} < -\lambda_1$, then $w > 0$
      * if $\sum{G_leaf} > \lambda_1$, then $w < 0$
      * otherwise $w = 0$
      */
    def leafSplitGain(sumGradients: Double, sumHessians: Double): Double = {
        val absSumGradients = math.abs(sumGradients)
        val regAbsSumGradients = math.max(0.0, absSumGradients - lambdaL1)
        regAbsSumGradients * regAbsSumGradients / (sumHessians + lambdaL2)
    }

    def optimalValue(sumGradients: Double, sumHessians: Double): Double = {
        -sumGradients / (sumHessians + lambdaL2)
    }

    def findBestSplitNumerical(entries: Array[HistogramEntry], binMapper: BinMapper): SplitInfo = {
        val minGain = leafSplitGain() + splitGain

    }

    def findBestSplitCategorical(entries: Array[HistogramEntry]): SplitInfo = {

    }
}

object FeatureHistogram {
    def subtract(hist1: FeatureHistogram, hist2: FeatureHistogram): FeatureHistogram = {
        require(hist1.entries.length == hist2.entries.length)
        require(hist1.lambdaL1 == hist2.lambdaL1)
        require(hist1.lambdaL2 == hist2.lambdaL2)

        val resultEntries = for ((entry1, entry2) <- hist1.entries.zip(hist2.entries))
            yield new HistogramEntry(entry1.count - entry2.count,
                                     entry1.sumGradients - entry2.sumGradients,
                                     entry1.sumHessians - entry2.sumHessians)
        new FeatureHistogram(hist1.featureMeta, hist1.lambdaL1, hist1.lambdaL2, resultEntries)
    }
}