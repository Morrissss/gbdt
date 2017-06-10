package morrissss.distributed.cost

import org.apache.commons.math3.analysis.function.Sigmoid

/**
  * label should be -1 or 1
  */
object ClassicalLogLoss extends Serializable {

    private val sigmoid = new Sigmoid()

    def calcGradients(label: Short, sigmoidScale: Double, weight: Double, score: Double): (Double, Double) = {
        val sigmoid = new Sigmoid()
        val response = -label * sigmoidScale * sigmoid.value(label * sigmoidScale * score)
        val absResponse = Math.abs(response)
        val gradient = response * weight
        val hessian = absResponse * (sigmoidScale - absResponse) * weight
        (gradient, hessian)
    }
}
