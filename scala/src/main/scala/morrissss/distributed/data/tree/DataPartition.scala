package morrissss.distributed.data.tree

import morrissss.distributed.data.partition.column.Column

class DataPartition(val idxInLeaf: Array[Int],
                    val leafBegin: Array[Int],
                    val leafCount: Array[Int]) {

    def split(column: Column, leaf: Int, feature: Int, threshold: Short): Unit = {
        val begin = leafBegin(leaf)
        val count = leafCount(leaf)

        val dataIndices: Array[Int] = idxInLeaf.slice(begin, begin+count)
        val (lteIndices, gtIndices) = column.split(leaf, threshold, dataIndices)

        val leftCnt = lteIndices.length

        Array.copy(lteIndices, 0, idxInLeaf, begin, lteIndices.length)
        Array.copy(gtIndices, 0, idxInLeaf, begin+lteIndices.length, gtIndices.length)

        leafCount(leaf) = 0
        val newLeftIdx = (leaf+1) * 2 - 1
        val newRightIdx = newLeftIdx + 1
        leafBegin(newLeftIdx) = begin
        leafCount(newLeftIdx) = lteIndices.length
        leafBegin(newRightIdx) = begin + lteIndices.length
        leafCount(newRightIdx) = gtIndices.length
    }
}
