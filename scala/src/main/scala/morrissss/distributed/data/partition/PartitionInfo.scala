package morrissss.distributed.data.partition

import morrissss.distributed.data.partition.column.Column
import morrissss.distributed.data.partition.histogram.HistogramEntry

class PartitionInfo {

    val scores: Array[Double]
    val weights: Array[Double]
    val gradients: Array[Double]
    val hessians: Array[Double]
    val usedFeatureIdx: Set[Int]
    val columns: Array[Column]
    val splits: Map[Int, SplitInfo] // not necessarily equal to usedFeatureIdx
    val histograms: Map[Int, Array[HistogramEntry]] // not necessarily equal to usedFeatureIdx
}
