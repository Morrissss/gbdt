package morrissss.distributed.data.feature

trait FeatureType
case object DenseFeature extends FeatureType
case object SparseFeature extends FeatureType
case object CategoricalFeature extends FeatureType