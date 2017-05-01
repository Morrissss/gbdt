package morrissss.distributed.data.master

class SplitInfo(val gain: Double)

class DenseSplitInfo(val threshold: Short, override val gain: Double) extends SplitInfo(gain)

class SparseSplitInfo(val threshold: Short, val zeroToLte: Boolean, override val gain: Double) extends SplitInfo(gain)

class CategoricalSplitInfo(val lteCategory: Short, override val gain: Double) extends SplitInfo(gain)
