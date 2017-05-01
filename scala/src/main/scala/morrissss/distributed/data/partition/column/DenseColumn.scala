package morrissss.distributed.data.partition.column
import morrissss.distributed.data.partition.histogram.{HistogramEntry, HistogramEntryBuilder}

import scala.collection.mutable.ArrayBuffer

class DenseColumn(val data: Array[Short]) extends Column {

    override def constructHistogram(binNum: Int, dataIndices: Array[Int], orderedGradients: Array[Float],
                                    orderedHessians: Array[Float]): Array[HistogramEntry] = {
        val result = Array.fill(binNum)(new HistogramEntryBuilder)
        for (idx <- dataIndices) {
            val bin = data(idx)
            val entry = result(bin)
            entry.count += 1
            entry.sumGradients += orderedGradients(entry)
            entry.sumHessians += orderedHessians(entry)
        }
        result.map(_.build())
    }

    override def constructHistogram(binNum: Int, orderedGradients: Array[Float],
                                    orderedHessians: Array[Float]): Array[HistogramEntry] = {
        val result = Array.fill(binNum)(new HistogramEntryBuilder)
        data.foreach(idx => {
            val bin = data(idx)
            val entry = result(bin)
            entry.count += 1
            entry.sumGradients += orderedGradients(entry)
            entry.sumHessians += orderedHessians(entry)
        })
        result.map(_.build())
    }

    override def spilt(minBin: Int, maxBin: Int, defaultBin: Short, threshold: Short,
                       dataIndices: Array[Int]): (Array[Int], Array[Int]) = {
        val lte = ArrayBuffer[Int]()
        val gt = ArrayBuffer[Int]()
        for (idx <- dataIndices) {
            val bin = data(idx)
            if (bin <= threshold) {
                lte += idx
            } else {
                gt += idx
            }
        }
        (lte.toArray, gt.toArray)
    }
}