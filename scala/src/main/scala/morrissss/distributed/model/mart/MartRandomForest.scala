package morrissss.distributed.model.mart

case class BinnedPoint(label: Double,
                       prediction: Double,
                       features: Array[Int],
                       nodeIdx: Int) {
    override def toString: String = {
        s"($label,$prediction,$features,$nodeIdx)"
    }
}

class MartRandomForest {

}
