package morrissss.distributed.cost

import org.apache.commons.math3.analysis.function.Sigmoid

/**
  * labels should be -1 or 1
  */
class LogLoss(private val labels: Seq[Int],
              private val labelWeights: Array[Double],
              private val sigmoidWeight: Double,
              private val weights: Option[Array[Double]]) extends AbstractLoss(labels, labelWeights) {

    override def calcGradHessians(scores: Seq[Double]): Seq[(Double, Double)] = {
        val sigmoid = new Sigmoid()
        weights match {
            case None => {
                for ((score, label) <- scores.zip(labels)) yield {
                    val labelWeight = labelWeights(label)
                    val response = -label * sigmoidWeight * sigmoid.value(label * sigmoidWeight * score)
                    val absResponse = Math.abs(response)
                    val gradient = response * labelWeight
                    val hessian = absResponse * (sigmoidWeight - absResponse) * labelWeight
                    (gradient, hessian)
                }
            }
            case Some(weights) => {
                for (((score, label), weight) <- scores.zip(labels).zip(weights)) yield {
                    val labelWeight = labelWeights(label)
                    val response = -label * sigmoidWeight * sigmoid.value(label * sigmoidWeight * score)
                    val absResponse = Math.abs(response)
                    val gradient = response * labelWeight * weight
                    val hessian = absResponse * (sigmoidWeight - absResponse) * labelWeight * weight
                    (gradient, hessian)
                }
            }
        }
    }
}
