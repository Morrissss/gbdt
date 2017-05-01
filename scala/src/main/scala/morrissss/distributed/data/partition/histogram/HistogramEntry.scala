package morrissss.distributed.data.partition.histogram

class HistogramEntry(val count: Int,
                     val sumGradients: Double,
                     val sumHessians: Double) {
    def +(that: HistogramEntry): HistogramEntry = {
        new HistogramEntry(this.count + that.count,
                           this.sumGradients + that.sumGradients,
                           this.sumHessians + that.sumHessians)
    }
}

class HistogramEntryBuilder {
    var count = 0
    var sumGradients = 0.0
    var sumHessians = 0.0
    def build() = new HistogramEntry(count, sumGradients, sumHessians)
}
