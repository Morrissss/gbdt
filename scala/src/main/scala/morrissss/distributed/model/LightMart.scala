package morrissss.distributed.model

import morrissss.distributed.cost.ClassicalLogLoss
import morrissss.distributed.data.master.{BinMapper, CategoricalFeatureMeta, FeatureMeta, NumericalFeatureMeta}
import morrissss.distributed.data.partition.column.{CategoricalColumn, DenseColumn, SparseColumn}
import morrissss.distributed.data.partition.{PartitionInfo, SampleMeta, SampleStatus}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.MartTrees.PeriodicRDDCheckpointer
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

case class ModelConfig(val treeNum: Int = 100,
                       val treeDepth: Int = 6,
                       val learningRate: Double = 0.01,
                       val maxBinNum: Short = 100,
                       val minSplitGain: Double,
                       val lambdaL1: Double,
                       val lambdaL2: Double,
                       val sampleSize: Int) extends Serializable

class LightMart {

//    private val sampleMeta: SampleMeta
//    private val modelConfig: ModelConfig

    /**
      * @param input
      * @param featureMetas already sorted as: Dense --> Categorical --> Sparse
      */
    def train(input: RDD[(Array[Double], Short)], featureMetas: Array[FeatureMeta], modelConfig: ModelConfig): Unit = {
        val sc = input.sparkContext
        val featureNum = featureMetas.length
        val bFeatureMetas = sc.broadcast(featureMetas)
        val binMappersRDD: RDD[Option[(Int, BinMapper)]] = input.mapPartitionsWithIndex((idx, iter) => {
            val result: Option[(Int, BinMapper)] = if (idx < featureNum) {
                None
            } else {
                val values: Array[Double] = iter.map(_._1(idx)).toArray
                val binMapper = bFeatureMetas.value(idx) match {
                    case CategoricalFeatureMeta(name, num) => BinMapper.constructCategorical(num)
                    case _ => BinMapper.constructNumeric(values, modelConfig.maxBinNum)
                }
                Some((idx, binMapper))
            }
            Array(result).iterator
        })
        val binMappers: Array[BinMapper] = binMappersRDD.filter(_.isDefined).map(_.get).collect().sorted.map(_._2)
        val bBinMappers = sc.broadcast(binMappers)

        // label, dense, categorical, sparse
        val binSamples: RDD[Array[Short]] = input.map{case (features, label) => {
            val result = new ArrayBuffer[Short]
            result += label
            result ++= bBinMappers.value.zip(features).map(Function.tupled{_.valueToBin(_)})
            result.toArray
        }}

        val partitionInfo: RDD[PartitionInfo] = binSamples.mapPartitions(iter => {
            val data: Array[Array[Short]] = iter.toArray
            val totalNum = data.length
            val columns = for (featureIdx <- 0 until featureNum) {
                val featureMeta = bFeatureMetas.value(featureIdx)
                featureMeta match {
                    case NumericalFeatureMeta(name, false) => new DenseColumn(data.map(_(featureIdx+1)))
                    case NumericalFeatureMeta(name, true) => {
                        val defaultBin = bBinMappers.value(featureIdx).valueToBin(0)
                        val pairs = for {
                            (labelFeatures, idx) <- data.zipWithIndex
                            if labelFeatures(featureIdx+1) != defaultBin
                        } yield (idx, labelFeatures(featureIdx+1))
                        new SparseColumn(pairs, totalNum-pairs.length, defaultBin, modelConfig.treeDepth)
                    }
                    case CategoricalFeatureMeta(name, num) => new CategoricalColumn(data.map(_(featureIdx+1)))
                }
            }

            null
        })

        binSamples.zipPartitions(partitionInfo)((sampleIter, partitionInfoIter) => {
            val data: Array[Array[Short]] = sampleIter.toArray
            val partitionInfo = partitionInfoIter.next()
            val gradients = Array.fill(data.length)(0.0)
            val hessians = Array.fill(data.length)(0.0)
            for (((labelFeature, score), i) <- data.zip(partitionInfo.scores).zipWithIndex) {
                (gradients(i), hessians(i)) = ClassicalLogLoss.calcGradients(labelFeature(0), 1, 1, score)
            }
            column.constructHistogram

            null
        })

        val predErrorCheckpointer = new PeriodicRDDCheckpointer[SampleStatus](10, input.sparkContext)

        val usedFeature = Array.fill(featureMeta.length)(false)
        for (i <- Random.shuffle(0 until featureMeta.length).take(math.sqrt(featureMeta.length).toInt)) {
            usedFeature(i) = true
        }
        val bUsedFeature = input.sparkContext.broadcast(usedFeature)

        input.mapPartitions(iter => {
            val samples: Array[(Array[Short], SampleStatus)] = iter.toArray
            null
        })

//        val prediction: RDD[Double]
//        input.zipPartitions()
    }


    def findSplitsBins(input: RDD[Array[Double]],
                       modelConfig: ModelConfig): Array[BinMapper] = {
        val seed = new Random().nextLong()
        val requiredSample = math.max(modelConfig.maxBinNum * modelConfig.maxBinNum, 10000)
        val fraction = requiredSample.toDouble / modelConfig.sampleSize
        val sampledInput = input.sample(false, fraction, seed)
        val numericMappers =
            sampledInput.flatMap(lp => for (i <- 0 until lp.features.size) yield (i, Array(lp.features(i))))
                        .filter(t => sampleMeta(t._1) match {
                            case CategoricalFeature => false
                            case SparseFeature => t._2(0) != 0.0
                            case _ => true
                        })
                        .reduceByKey(_++_)
                        .map { case (idx: Int, values: Array[Double]) => {
                            sampleMeta(idx) match {
                                case DenseFeature => (idx, BinMapper.constructDense(values, modelConfig.maxBinNum))
                                case SparseFeature => (idx, BinMapper.constructSparse(values, modelConfig.maxBinNum))
                          }
                        }}.collectAsMap()
        val categoricalMappers = sampleMeta.categoryNums.map(cn => (cn._1, BinMapper.constructCategorical(cn._2)))

        for (i <- 0 until sampleMeta.featureNum) yield numericMappers.applyOrElse(i, categoricalMappers(_))
    }

    def constructSamples(input: RDD[LabeledPoint], modelConfig: ModelConfig, binMappers: Array[BinMapper]): RDD[Array[Short]] = {
        input.map(lp => for ((binMapper, value) <- binMappers.zip(lp.features.toArray)) yield binMapper.valueToBin(value))
    }

    def buildMeta() = {

    }
}
