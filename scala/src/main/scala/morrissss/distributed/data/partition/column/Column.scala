package morrissss.distributed.data.partition.column

import morrissss.distributed.data.partition.histogram.HistogramEntry

trait Column {

    def constructHistogram(binNum: Int, dataIndices: Array[Int],
                           orderedGradients: Array[Float], orderedHessians: Array[Float]): Array[HistogramEntry]

    def constructHistogram(binNum: Int, orderedGradients: Array[Float], orderedHessians: Array[Float]): Array[HistogramEntry]

    // lteIndices, gtIndices
    def spilt(minBin: Int, maxBin: Int, defaultBin: Short, threshold: Short, dataIndices: Array[Int]): (Array[Int], Array[Int])

}
