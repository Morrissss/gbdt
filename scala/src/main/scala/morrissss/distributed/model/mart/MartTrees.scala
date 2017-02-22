package morrissss.distributed.model.mart

import org.apache.spark.{Logging, SparkContext}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.impl.PeriodicCheckpointer
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.configuration.BoostingStrategy
import org.apache.spark.mllib.tree.impl.TimeTracker
import org.apache.spark.mllib.tree.loss.Loss
import org.apache.spark.mllib.tree.model.{Bin, DecisionTreeModel, GradientBoostedTreesModel, Split}
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.util.Utils

import scala.reflect.ClassTag

object MartTrees extends Logging {

    def train(input: RDD[LabeledPoint], boostingStrategy: BoostingStrategy): GradientBoostedTreesModel = {
        // Initialize gradient boosting parameters
        boostingStrategy.setLoss(ClassicalLogLoss)
        val numIterations = boostingStrategy.numIterations
        val baseLearners = new Array[DecisionTreeModel](numIterations)
        val baseLearnerWeights = new Array[Double](numIterations)
        val loss = boostingStrategy.loss
        val learningRate = boostingStrategy.learningRate
        val treeStrategy = boostingStrategy.treeStrategy
        treeStrategy.setMaxMemoryInMB(1000)
        treeStrategy.setMaxBins(1000)
        treeStrategy.setCheckpointInterval(10) // default 10
        treeStrategy.assertValid()

        input.setName("input")
        val numSamples = input.count().toInt
        val numFeatures = input.first().features.size
        val initMeta = MartDecisionTree.buildMeta(numSamples, numFeatures, boostingStrategy, "all")

        val (splits, bins) = MartDecisionTree.findSplitsBins(input, initMeta)
        val bSplits: Broadcast[Array[Array[Split]]] = input.sparkContext.broadcast(splits)
        val bBins: Broadcast[Array[Array[Bin]]] = input.sparkContext.broadcast(bins)

        // Prepare periodic checkpointers
        val predErrorCheckpointer = new PeriodicRDDCheckpointer[(Double, Double)](treeStrategy.getCheckpointInterval,
                                                                                  input.sparkContext)

        // Initialize tree
        val rf = new MartDecisionTree(initMeta, treeStrategy, Utils.random.nextInt())
        val rfModel = rf.run(input, bSplits.value, bBins.value)
        val firstTreeModel = rfModel.trees(0)

        val firstTreeWeight = 1.0
        baseLearners(0) = firstTreeModel
        baseLearnerWeights(0) = firstTreeWeight

        var predError = GradientBoostedTreesModel.computeInitialPredictionAndError(input, firstTreeWeight,
                                                                                   firstTreeModel, loss)
        predError.setName("predError 0")
        predErrorCheckpointer.update(predError)
        printEval(predError, 0)

        var m = 1
        while (m < numIterations) {
            if (m % 10 == 0) {
                printEval(predError, m)
            }
            // Update data with pseudo-residuals
            val data = input.zip(predError).map { case (point, (pred, _)) =>
                LabeledPoint(-loss.gradient(pred, point.label), point.features) }
            data.setName("data " + m)

            val meta = MartDecisionTree.buildMeta(numSamples, numFeatures, boostingStrategy, "sqrt")
            val rf = new MartDecisionTree(meta, treeStrategy, Utils.random.nextInt())
            val rfModel = rf.run(data, bSplits.value, bBins.value)
            val model = rfModel.trees(0)

            // Update partial model
            baseLearners(m) = model
            baseLearnerWeights(m) = learningRate

            predError = GradientBoostedTreesModel.updatePredictionError(input, predError, baseLearnerWeights(m),
                                                                        baseLearners(m), loss)
            predError.setName("predError " + m)
            predErrorCheckpointer.update(predError)
            m += 1
        }
        printEval(predError, m)

        predErrorCheckpointer.deleteAllCheckpoints()
        new GradientBoostedTreesModel(boostingStrategy.treeStrategy.algo, baseLearners, baseLearnerWeights)
    }

    private def printEval(predError: RDD[(Double, Double)], m: Int): Unit = {
        val mae = predError.values.map(e => 1 - math.exp(-e)).mean()
        val mse = predError.values.map(e => {
            val t = 1 - math.exp(-e);t * t
        }).mean()
        val error = predError.values.mean()
        println(s"gbt $m:\tmae=$mae\tmse=$mse\terror=$error")
    }

    class PeriodicRDDCheckpointer[T](checkpointInterval: Int, sc: SparkContext)
            extends PeriodicCheckpointer[RDD[T]](checkpointInterval, sc) {
        override protected def checkpoint(data: RDD[T]): Unit = data.checkpoint()
        override protected def isCheckpointed(data: RDD[T]): Boolean = data.isCheckpointed
        override protected def persist(data: RDD[T]): Unit = {
            if (data.getStorageLevel == StorageLevel.NONE) {
                data.cache()
            }
        }
        override protected def unpersist(data: RDD[T]): Unit = data.unpersist(blocking = false)
        override protected def getCheckpointFiles(data: RDD[T]): Iterable[String] = {
            data.getCheckpointFile.map(x => x)
        }
    }

    /**
      * label is 0, 1
      */
    object ClassicalLogLoss extends Loss {

        override def gradient(prediction: Double, label: Double): Double = {
            val posNeg = 1 - 2 * label
            posNeg / (1 + math.exp(-posNeg * prediction))
        }

        override def computeError(prediction: Double, label: Double): Double = {
            val margin = (1 - 2 * label) * prediction
            MLUtils.log1pExp(margin)
        }
    }
}
