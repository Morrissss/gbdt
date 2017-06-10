package morrissss.distributed.data.partition.column

import morrissss.distributed.data.partition.histogram.{HistogramEntry, HistogramEntryBuilder}

import scala.collection.mutable.ArrayBuffer

// sparseIdxValues
// only return non-zero indices
class SparseColumn(val defaultNum: Int, val defaultIdx: Short,
                   val maxDepth: Int) extends Column {
    private var sortedDataBuffer = new ArrayBuffer[(Int, Short)]

    private var sortedData = Array.empty[(Int, Short)]
    private val leafNum = (1 << maxDepth) - 1
    private var leafBegin: Array[Int] = Array.empty
    private var leafCount: Array[Int] = Array.empty

    override def push(idx: Int, bin: Short): Unit = {
        sortedDataBuffer += Tuple2(idx, bin)
    }

    override def finishLoad(): Unit = {
        sortedData = sortedDataBuffer.toArray
        sortedDataBuffer = new ArrayBuffer
        leafBegin = Array(0) ++ Array.fill(leafNum-1)(sortedData.length)
        leafCount = Array(sortedData.length) ++ Array.fill(leafNum - 1)(0)
    }

    override def constructHistogram(leaf: Int, binNum: Int, dataIndices: Array[Int], orderedGradients: Array[Float],
                                    orderedHessians: Array[Float]): Array[HistogramEntry] = {
        val result = Array.fill(binNum)(new HistogramEntryBuilder)
        val beg = leafBegin(leaf)
        val end = beg + leafCount(leaf)
        for (idx <- beg until end) {
            val bin = sortedData(idx)._2
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
        val beg = leafBegin(leaf)
        val end = beg + leafCount(leaf)
        for (idx <- beg until end) {
            val bin = sortedData(idx)._2
            val entry = result(bin)
            entry.count += 1
            entry.sumGradients += orderedGradients(idx)
            entry.sumHessians += orderedHessians(idx)
        }
        result.map(_.build())
    }

    override def split(leaf: Int, threshold: Short, dataIndices: Array[Int]): (Array[Int], Array[Int]) = {
        val leftBeg = leafBegin(leaf)
        val rightEnd = leafCount(leaf)
        def swapLeafData(leftCursor: Int, rightCursor: Int): Int = {
            if (leftCursor > rightCursor) return leftCursor
            val bin = sortedData(leftCursor)._2
            if (bin <= threshold) {
                swapLeafData(leftCursor+1, rightCursor)
            } else {
                val tmp = sortedData(leftCursor)
                sortedData(leftCursor) = sortedData(rightCursor)
                sortedData(rightCursor) = tmp
                swapLeafData(leftCursor, rightCursor-1)
            }
        }
        val leftEnd: Int = swapLeafData(leftBeg, rightEnd-1)

        leafCount(leaf) = 0
        val newLeftIdx = (leaf+1) * 2 - 1
        val newRightIdx = newLeftIdx + 1
        leafBegin(newLeftIdx) = leftBeg
        leafCount(newLeftIdx) = leftEnd - leftBeg
        leafBegin(newRightIdx) = leftEnd
        leafCount(newRightIdx) = rightEnd - leftEnd

        java.util.Arrays.sort(sortedData, leftBeg, leftEnd, scala.math.Ordering.Tuple2[Int, Short])
        java.util.Arrays.sort(sortedData, leftEnd, rightEnd, scala.math.Ordering.Tuple2[Int, Short])

        (sortedData.slice(leftBeg, leftEnd).map(_._1), sortedData.slice(leftEnd, rightEnd).map(_._1))
    }
}
