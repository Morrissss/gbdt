package morrissss.distributed.util

import org.apache.commons.math3.special.Gamma
import org.apache.spark.rdd.RDD

class BetaCtrSmoother {

    /**
      * @param numDenom
      * @return a, b: (a+numerator) / (b+denominator)
      */
    def train(numDenom: RDD[(Double, Double)]): (Double, Double) = {
        numDenom.cache()
        val size = numDenom.count()

        var currA: Double = Double.MaxValue
        var currB: Double = Double.MaxValue
        var nextA: Double = numDenom.map(_._1).mean()
        var nextB: Double = numDenom.map(_._2).mean() - nextA
//        var nextA = 2.
//        var nextB = 20.

        (0 to 10000).toStream.takeWhile(_ => math.abs(currA-nextA)>1e-3 || math.abs(currB-nextB)>1e-3).foreach(_ => {
            currA = nextA
            currB = nextB
            val digammaA = Gamma.digamma(currA)
            val digammaB = Gamma.digamma(currB)
            val digammaAB = Gamma.digamma(currA+currB)

            val (sumA, sumB, sumAB) = numDenom.map { case (numerator, denominator) => {
                val a = Gamma.digamma(numerator + currA) - digammaA
                val b = Gamma.digamma(denominator - numerator + currB) - digammaB
                val ab = Gamma.digamma(denominator + currA + currB) - digammaAB
                (a, b, ab)
            }}.reduce { case (x, y) => (x._1+y._1, x._2+y._2, x._3+y._3) }
            nextA = currA * sumA / sumAB
            nextB = currB * sumB / sumAB
            println((sumA, sumB, sumAB, nextA, nextB))
        })
        numDenom.unpersist()
        (nextA, nextA+nextB)
    }
}
