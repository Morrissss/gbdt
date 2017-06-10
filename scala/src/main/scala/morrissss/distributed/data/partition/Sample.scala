package morrissss.distributed.data.partition

/**
  * dense, categorical, sparse, label
  */

class SampleStatus(val predict: Double, val gradient: Float, val hessian: Float, val leafIdx: Int)

class SampleMeta(val numDenseFeature: Int, val categoryNums: Map[Int, Short], val numSparseFeature: Int) {
    def apply(idx: Int): FeatureType = {
        if (idx < numDenseFeature) {
            DenseFeature
        } else if (idx < numDenseFeature + categoryNums.size) {
            CategoricalFeature
        } else {
            SparseFeature
        }
    }
    val featureNum = numDenseFeature + categoryNums.size + numSparseFeature
    val denseIndices = 0 until numDenseFeature
    val categoricalIndices = numDenseFeature until (numDenseFeature+ categoryNums.size)
    val sparseIndices = (numDenseFeature+ categoryNums.size) until featureNum
}