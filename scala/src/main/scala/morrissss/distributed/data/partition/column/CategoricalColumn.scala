package morrissss.distributed.data.partition.column

import morrissss.distributed.data.partition.histogram.{HistogramEntry, HistogramEntryBuilder}

import scala.collection.mutable.ArrayBuffer

class CategoricalColumn(val num: Int) extends Column {

    private val data = Array.fill(num)(0)

    override def push(idx: Int, bin: Short): Unit = {
        data(idx) = bin
    }

    override def constructHistogram(leaf: Int, binNum: Int, dataIndices: Array[Int], orderedGradients: Array[Float],
                                    orderedHessians: Array[Float]): Array[HistogramEntry] = {
        val result = Array.fill(binNum)(new HistogramEntryBuilder)
        for (idx <- dataIndices) {
            val bin = data(idx)
            val entry = result(bin)
            entry.count += 1
            entry.sumGradients += orderedGradients(idx)
            entry.sumHessians += orderedHessians(idx)
        }
        result.map(_.build())
    }

    override def constructHistogram(leaf: Int, binNum: Int, orderedGradients: Array[Float],
                                    orderedHessians: Array[Float]): Array[HistogramEntry] = {
        val result = Array.fill(binNum)(new HistogramEntryBuilder)
        data.foreach(idx => {
            val bin = data(idx)
            val entry = result(bin)
            entry.count += 1
            entry.sumGradients += orderedGradients(idx)
            entry.sumHessians += orderedHessians(idx)
        })
        result.map(_.build())
    }

    override def split(leaf: Int, threshold: Short, dataIndices: Array[Int]): (Array[Int], Array[Int]) = {
        val eq = ArrayBuffer[Int]()
        val neq = ArrayBuffer[Int]()
        for (idx <- dataIndices) {
            val bin = data(idx)
            if (bin == threshold) {
                eq += idx
            } else {
                neq += idx
            }
        }
        (eq.toArray, neq.toArray)
    }
}
