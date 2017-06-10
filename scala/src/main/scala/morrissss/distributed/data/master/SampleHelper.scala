package morrissss.distributed.data.master

class SampleHelper(val denseNum: Int,
                   val categoricalNum: Int,
                   val sparseNum: Int,
                   val hasWeight: Boolean) {
    def takeLabel(sample: Array[Short]): Short = sample(0)

    def takeWeight(sample: Array[Short]): Double = {
        if (hasWeight) {
            sample(1) / 1000.0
        } else {
            null
        }
    }

    def takeFeature(sample: Array[Short], idx: Int): Short = {
        val innerIdx = idx + 1 + (if (hasWeight) 1 else 0)
        val start
    }

}
