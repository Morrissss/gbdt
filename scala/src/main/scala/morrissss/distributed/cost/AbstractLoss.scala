package morrissss.distributed.cost

abstract class AbstractLoss(private[cost] val labels: Seq[Int],
                            private[cost] val labelWeights: Array[Double]) {

    abstract def calcGradHessians(scores: Seq[Double]): Seq[(Double, Double)]
}
