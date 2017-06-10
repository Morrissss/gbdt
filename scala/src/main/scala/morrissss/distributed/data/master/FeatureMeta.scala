package morrissss.distributed.data.master

abstract class FeatureMeta(val name: String)

case class NumericalFeatureMeta(override val name: String,
                                val sparse: Boolean) extends FeatureMeta(name)

case class CategoricalFeatureMeta(override val name: String,
                                  val categoryNum: Short) extends FeatureMeta(name)
