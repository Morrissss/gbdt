package morrissss.distributed.data.partition.column

import morrissss.distributed.data.partition.histogram.HistogramEntry

trait Column {

    def push(idx: Int, bin: Short): Unit

    def finishLoad(): Unit = {}

    def constructHistogram(leaf: Int, binNum: Int, dataIndices: Array[Int],
                           orderedGradients: Array[Float], orderedHessians: Array[Float]): Array[HistogramEntry]

    def constructHistogram(leaf: Int, binNum: Int,
                           orderedGradients: Array[Float], orderedHessians: Array[Float]): Array[HistogramEntry]

    // lteIndices, gtIndices
    def split(leaf: Int, threshold: Short, dataIndices: Array[Int]): (Array[Int], Array[Int])

}
