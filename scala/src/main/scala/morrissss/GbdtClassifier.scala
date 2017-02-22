package morrissss

import morrissss.base.util.MathUtils
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.configuration.Algo._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.tree.MartTrees
import org.apache.spark.mllib.tree.configuration.BoostingStrategy
import org.apache.spark.mllib.tree.impurity.Variance
import org.apache.spark.mllib.tree.loss.Loss
import org.apache.spark.mllib.tree.model.{GradientBoostedTreesModel, Node}

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object GbdtClassifier {

    def main(args: Array[String]): Unit = {
        val conf = new SparkConf()
        conf.setAppName("Homefeed GbrtTree Model")
        val sc = new SparkContext(conf)
        sc.setLogLevel("WARN")

        val samples: ArrayBuffer[LabeledPoint] = ArrayBuffer()
        for (i <- 1 to 10000) {
            val sb: StringBuilder = new StringBuilder(Random.nextInt(2)+",")
            for (j <- 1 to 5) {
                sb.append(Random.nextDouble() + " ")
            }
            val labeledPoint = LabeledPoint.parse(sb.toString)
            samples.append(labeledPoint)
        }
        val data = sc.makeRDD(samples)

        val boostingStrategy = BoostingStrategy.defaultParams("regression")
        boostingStrategy.setNumIterations(10)
        boostingStrategy.setLoss(ClassicalLogLoss)
        boostingStrategy.setLearningRate(0.2)
        boostingStrategy.treeStrategy.setMaxDepth(3)
        boostingStrategy.treeStrategy.setSubsamplingRate(0.8)
        boostingStrategy.treeStrategy.setAlgo(Regression)
        boostingStrategy.treeStrategy.setImpurity(Variance)

        val model: GradientBoostedTreesModel = MartTrees.train(data, boostingStrategy)

        val predictAndLabels = data.map { point => {
            val prediction = MathUtils.sigmoid(model.predict(point.features))
            (prediction, point.label)
        }}
        predictAndLabels.max()
        predictAndLabels.min()

        val mse = predictAndLabels.map { case (p, v) => math.pow(p-v, 2) }.mean()
        val metrics = new BinaryClassificationMetrics(predictAndLabels)
        val auc = metrics.areaUnderROC()

        val featureKeys = Array[String]("a$VAL$", "b$VAL$", "c$VAL$", "d$VAL$", "e$VAL$")
        val modelStr = dumpGbrtModel(model, featureKeys)

        sc.makeRDD(modelStr, 1).saveAsTextFile("/user/rmao/gbdt.out")
    }

    def featureImportance(model: GradientBoostedTreesModel, featureNum: Int): Array[Double] = {
        def aggregateGains(node: Node, nodeWeight: Double, treeWeight: Double, aggr: Array[Double]): Unit = {
            if (!node.isLeaf) {
                aggr(node.split.get.feature) += node.stats.get.gain * nodeWeight * treeWeight
                val stats = node.stats.get
                val leftNormWeight = (stats.impurity - stats.gain - stats.rightImpurity) / (stats.leftImpurity - stats.rightImpurity)
                val rightNormWeight = 1 - leftNormWeight
                aggregateGains(node.leftNode.get, nodeWeight*leftNormWeight, treeWeight, aggr)
                aggregateGains(node.rightNode.get, nodeWeight*rightNormWeight, treeWeight, aggr)
            }
        }

        val result: Array[Double] = new Array[Double](featureNum)
        for (i <- 0 until model.numTrees) {
            aggregateGains(model.trees(i).topNode, model.treeWeights(i), 1.0, result)
        }
        val total: Double = result.sum
        result.map(d => d/total)
    }

    def dumpGbrtModel(model: GradientBoostedTreesModel, featureKeys: Array[String]): Array[String] = {
        def dumpGbrtTree(node: Node, buffer: ArrayBuffer[String], level: Int, featureKeys: Array[String], trainingFraction: Double): Unit = {
            if (node.isLeaf) {
                buffer.append("\t" * level + node.predict.predict + " " + trainingFraction)
            } else {
                val stats = node.stats.get
                val leftNormWeight = (stats.impurity - stats.gain - stats.rightImpurity) / (stats.leftImpurity - stats.rightImpurity)
                val rightNormWeight = 1 - leftNormWeight
                buffer.append("\t" * level + featureKeys(node.split.get.feature) + " " + node.split.get.threshold +
                              " " + node.predict.predict + " " + trainingFraction)
                dumpGbrtTree(node.leftNode.get, buffer, level+1, featureKeys, trainingFraction * leftNormWeight)
                dumpGbrtTree(node.rightNode.get, buffer, level+1, featureKeys, trainingFraction * rightNormWeight)
            }
        }

        val result: ArrayBuffer[String] = new ArrayBuffer[String]
        result.append(featureKeys.mkString(" "))
        result.append(model.numTrees.toString)
        for (i <- 0 until model.numTrees) {
            result.append(model.treeWeights(i).toString)
            dumpGbrtTree(model.trees(i).topNode, result, 0, featureKeys, 1.0)
        }
        result.toArray
    }
}

object ClassicalLogLoss extends Loss {

    override def gradient(prediction: Double, label: Double): Double = {
        val posNeg = 1 - 2 * label
        posNeg / (1 + math.exp(-posNeg * prediction))
    }

    override def computeError(prediction: Double, label: Double): Double = {
        val margin = (1 - 2 * label) * prediction
        log1pExp(margin)
    }

    /**
      * When `x` is positive and large, computing `math.log(1 + math.exp(x))` will lead to arithmetic
      * overflow. This will happen when `x > 709.78` which is not a very large number.
      * It can be addressed by rewriting the formula into `x + math.log1p(math.exp(-x))` when `x > 0`.
      *
      * @param x a floating-point value as input.
      * @return the result of `math.log(1 + math.exp(x))`.
      */
    private def log1pExp(x: Double): Double = {
        if (x > 0) {
            x + math.log1p(math.exp(-x))
        } else {
            math.log1p(math.exp(x))
        }
    }
}
