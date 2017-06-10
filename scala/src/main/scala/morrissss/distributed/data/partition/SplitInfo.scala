package morrissss.distributed.data.partition

class SplitInfo(val feature: Int,
                var threshold: Short,
                //var defaultBin: Short,
                var gain: Double,
                var leftOutput: Double, var rightOutput: Double,
                var leftCount: Int, var rightCount: Int,
                var leftSumGrad: Double, var rightSumGrad: Double,
                var leftSumHess: Double, var rightSumHess: Double) extends Ordered[SplitInfo] with Serializable {

    override def compare(that: SplitInfo): Int = {
        if (this.gain > that.gain) {
            1
        } else if (this.gain < that.gain) {
            -1
        } else if (this.feature < that.feature) {
            1
        } else if (this.feature > that.feature) {
            -1
        } else {
            0
        }
    }
}
