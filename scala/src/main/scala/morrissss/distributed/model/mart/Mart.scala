package morrissss.distributed.model.mart

import morrissss.base.feature.ModelKey
import morrissss.base.util.{KeyCompPair, MathUtils}
import morrissss.distributed.model.distributed.Model
import morrissss.online.util.Sampler
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.MartTrees
import org.apache.spark.mllib.tree.configuration.BoostingStrategy
import org.apache.spark.mllib.tree.model.{GradientBoostedTreesModel, Node}
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ArrayBuffer

class Mart(override val name: String,
           val sampler: Sampler,
           val sortedModelKeys: Array[ModelKey],
           val maxDepth: Int,
           val treeNum: Int,
           val learningRate: Double) extends Model[LabeledPoint, String] {

    val boostingStrategy = BoostingStrategy.defaultParams("regression")
    boostingStrategy.treeStrategy.setMaxDepth(maxDepth)
    boostingStrategy.setNumIterations(treeNum)
    boostingStrategy.setLearningRate(learningRate)

    var model: GradientBoostedTreesModel = null

    /**
      * @param dataSet each line contains all model key
      * @return
      */
    override def train(dataSet: RDD[LabeledPoint]): RDD[(Double, Double)] = {
        model = MartTrees.train(dataSet, boostingStrategy)
        dataSet.map(data => (sampler.calibrate(MathUtils.sigmoid(model.predict(data.features))),
                             data.label))
    }

    override def predict(testSet: RDD[LabeledPoint]): RDD[(Double, Double)] = {
        testSet.map(data => (sampler.calibrate(MathUtils.sigmoid(model.predict(data.features))),
                             data.label))
    }

    override def dump(): String = {
        def dumpGbrtTree(node: Node, buffer: ArrayBuffer[String], level: Int, trainingFraction: Double): Unit = {
            if (node.isLeaf) {
                buffer.append("\t" * level + node.predict.predict + " " + trainingFraction)
            } else {
                val stats = node.stats.get
                /**
                  * @see calculateGainForSplit in [[org.apache.spark.mllib.tree.DecisionTree]]
                  */
                val leftNormWeight = (stats.impurity - stats.gain - stats.rightImpurity) / (stats.leftImpurity - stats.rightImpurity)
                val rightNormWeight = 1 - leftNormWeight
                buffer.append("\t" * level + sortedModelKeys(node.split.get.feature) + " " + node.split.get.threshold +
                              " " + node.predict.predict + " " + trainingFraction)
                dumpGbrtTree(node.leftNode.get, buffer, level+1, trainingFraction * leftNormWeight)
                dumpGbrtTree(node.rightNode.get, buffer, level+1, trainingFraction * rightNormWeight)
            }
        }
        val featureKeys = sortedModelKeys.map(_.toString)
        val result: ArrayBuffer[String] = new ArrayBuffer[String]
        result.append(sampler.globalRatio + " " + sampler.negRatio)
        result.append(featureKeys.mkString(" "))
        result.append(treeNum.toString + " " + maxDepth.toString)
        for (i <- 0 until model.numTrees) {
            result.append(model.treeWeights(i).toString)
            dumpGbrtTree(model.trees(i).topNode, result, 0, 1.0)
        }
        result.toArray.mkString("\n")
    }

    override def load(weights: String): Unit = throw new UnsupportedOperationException

    override def featureImportance(limit: Int = 0): Array[(ModelKey, Double)] = {
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
        sortedModelKeys.zip(featureImportance(model, sortedModelKeys.length)).sortWith(_._2 > _._2)
    }

    override def dumpHdfs(sparkContext: SparkContext, dir: String): Unit = model.save(sparkContext, dir)

    override def loadHdfs(sparkContext: SparkContext, dir: String): Unit = { model = GradientBoostedTreesModel.load(sparkContext, dir) }
}
