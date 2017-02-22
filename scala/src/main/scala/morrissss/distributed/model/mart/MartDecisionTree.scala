package morrissss.distributed.model.mart

import java.io.IOException

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.configuration.Algo._
import org.apache.spark.mllib.tree.configuration.{BoostingStrategy, Strategy}
import org.apache.spark.mllib.tree.impl._
import org.apache.spark.mllib.tree.model._
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable

class MartDecisionTree(private val metadata: DecisionTreeMetadata,
                       private val strategy: Strategy,
                       private val seed: Int) extends Serializable {
    strategy.assertValid()

    def run(input: RDD[LabeledPoint], splits: Array[Array[Split]], bins: Array[Array[Bin]]): RandomForestModel = {
        // Bin feature values (TreePoint representation).
        // Cache input RDD for speedup during multiple passes.
        val treeInput = TreePoint.convertToTreeRDD(input, bins, metadata)
        val baggedInput = BaggedPoint.convertToBaggedRDD(treeInput,
                                                         strategy.subsamplingRate, 1,
                                                         false, seed).persist(StorageLevel.MEMORY_AND_DISK)
        baggedInput.setName("baggedInput")

        // depth of the decision tree
        val maxDepth = strategy.maxDepth
        require(maxDepth <= 30,
                s"DecisionTree currently only supports maxDepth <= 30, but was given maxDepth = $maxDepth.")

        // Max memory usage for aggregates
        // TODO: Calculate memory usage more precisely.
        val maxMemoryUsage: Long = strategy.maxMemoryInMB * 1024L * 1024L
        val maxMemoryPerNode = {
            val featureSubset: Option[Array[Int]] = if (metadata.subsamplingFeatures) {
                // Find numFeaturesPerNode largest bins to get an upper bound on memory usage.
                Some(metadata.numBins.zipWithIndex.sortBy(- _._1)
                     .take(metadata.numFeaturesPerNode).map(_._2))
            } else {
                None
            }
            RandomForest.aggregateSizeForNode(metadata, featureSubset) * 8L
        }
        require(maxMemoryPerNode <= maxMemoryUsage,
                s"RandomForest/DecisionTree given maxMemoryInMB = ${strategy.maxMemoryInMB}," +
                " which is too small for the given features." +
                s"  Minimum value = ${maxMemoryPerNode / (1024L * 1024L)}")

        // FIFO queue of nodes to train: (treeIndex, node)
        val nodeQueue = new mutable.Queue[(Int, Node)]()

        val rng = new scala.util.Random()
        rng.setSeed(seed)

        // Allocate and queue root nodes.
        val topNodes: Array[Node] = Array.fill[Node](1)(Node.emptyNode(nodeIndex = 1))
        nodeQueue.enqueue((0, topNodes(0)))

        while (nodeQueue.nonEmpty) {
            // Collect some nodes to split, and choose features for each node (if subsampling).
            // Each group of nodes may come from one or multiple trees, and at multiple levels.
            val (nodesForGroup, treeToNodeToIndexInfo) =
            RandomForest.selectNodesToSplit(nodeQueue, maxMemoryUsage, metadata, rng)
            // Sanity check (should never occur):
            assert(nodesForGroup.size > 0,
                   s"RandomForest selected empty nodesForGroup.  Error for unknown reason.")

            // Choose node splits, and enqueue new nodes as needed.
            DecisionTree.findBestSplits(input = baggedInput, metadata = metadata, topNodes = topNodes,
                                        nodesForGroup = nodesForGroup, treeToNodeToIndexInfo = treeToNodeToIndexInfo,
                                        splits = splits, bins = bins, nodeQueue = nodeQueue, nodeIdCache = None)
        }

        baggedInput.unpersist()

        val trees = topNodes.map(topNode => new DecisionTreeModel(topNode, strategy.algo))
        new RandomForestModel(strategy.algo, trees)
    }
}

object MartDecisionTree {

    def buildMeta(numExamples: Int, numFeatures: Int, boostingStrategy: BoostingStrategy,
                  featureSubsetStrategy: String): DecisionTreeMetadata = {
        require(RandomForest.supportedFeatureSubsetStrategies.contains(featureSubsetStrategy),
                s"RandomForest given invalid featureSubsetStrategy: $featureSubsetStrategy." +
                s" Supported values: ${RandomForest.supportedFeatureSubsetStrategies.mkString(", ")}.")
        val treeStrategy = boostingStrategy.getTreeStrategy
        val numClasses = treeStrategy.algo match {
            case Classification => treeStrategy.numClasses
            case Regression => 0
        }

        val maxPossibleBins = math.min(treeStrategy.maxBins, numExamples)

        // We check the number of bins here against maxPossibleBins.
        // This needs to be checked here instead of in Strategy since maxPossibleBins can be modified
        // based on the number of training examples.
        if (treeStrategy.categoricalFeaturesInfo.nonEmpty) {
            val maxCategoriesPerFeature = treeStrategy.categoricalFeaturesInfo.values.max
            val maxCategory = treeStrategy.categoricalFeaturesInfo.find(_._2 == maxCategoriesPerFeature).get._1
            require(maxCategoriesPerFeature <= maxPossibleBins,
                    s"DecisionTree requires maxBins (= $maxPossibleBins) to be at least as large as the " +
                    s"number of values in each categorical feature, but categorical feature $maxCategory " +
                    s"has $maxCategoriesPerFeature values. Considering remove this and other categorical " +
                    "features with a large number of values, or add more training examples.")
        }

        val unorderedFeatures = new mutable.HashSet[Int]()
        val numBins = Array.fill[Int](numFeatures)(maxPossibleBins)

        // Set number of features to use per node (for random forests).
        val numFeaturesPerNode: Int = featureSubsetStrategy match {
            case "all" => numFeatures
            case "sqrt" => math.sqrt(numFeatures).ceil.toInt
            case "log2" => math.max(1, (math.log(numFeatures) / math.log(2)).ceil.toInt)
            case "onethird" => (numFeatures / 3.0).ceil.toInt
        }

        new DecisionTreeMetadata(numFeatures, numExamples, numClasses, numBins.max,
                                 treeStrategy.categoricalFeaturesInfo, unorderedFeatures.toSet, numBins,
                                 treeStrategy.impurity, treeStrategy.quantileCalculationStrategy, treeStrategy.maxDepth,
                                 treeStrategy.minInstancesPerNode, treeStrategy.minInfoGain, 1, numFeaturesPerNode)
    }

    def findSplitsBins(input: RDD[LabeledPoint],
                       metadata: DecisionTreeMetadata): (Array[Array[Split]], Array[Array[Bin]]) = {
        DecisionTree.findSplitsBins(input, metadata)
    }
}