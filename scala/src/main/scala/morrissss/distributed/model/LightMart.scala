package morrissss.distributed.model

import morrissss.distributed.data.feature.{CategoricalFeature, DenseFeature, SparseFeature}
import morrissss.distributed.data.SampleMeta
import morrissss.distributed.data.master.BinMapper
import morrissss.distributed.data.partition.{SampleMeta, SampleStatus}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.MartTrees.PeriodicRDDCheckpointer
import org.apache.spark.rdd.RDD

import scala.util.Random

case class ModelConfig(val treeNum: Int = 100,
                       val learningRate: Double = 0.01,
                       val maxBinNum: Short = 100,
                       val minSplitGain: Double,
                       val lambdaL1: Double,
                       val lambdaL2: Double,
                       val sampleSize: Int) extends Serializable

class LightMart {

    private val sampleMeta: SampleMeta
    private val modelConfig:

    def train(input: RDD[(Array[Short], SampleStatus)], binMappers: Array[BinMapper]): Unit = {
        val bBinMappers = input.sparkContext.broadcast(binMappers)

        val predErrorCheckpointer = new PeriodicRDDCheckpointer[SampleStatus](10, input.sparkContext)

        input.mapPartitions(iter => {
            val samples: Array[(Array[Short], SampleStatus)] = iter.toArray
            null
        })

        val prediction: RDD[Double]
        input.zipPartitions()
    }


    def findSplitsBins(input: RDD[LabeledPoint],
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
