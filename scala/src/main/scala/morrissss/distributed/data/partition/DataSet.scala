package morrissss.distributed.data.partition

import morrissss.distributed.data.master.BinMapper
import morrissss.distributed.data.partition.column.{Column, SparseColumn}
import morrissss.distributed.data.partition.histogram.HistogramEntry

import scala.collection.mutable.ArrayBuffer

class DataSet {

    private val columns: Array[Column]
    private val binNums: Array[Int]
    private val binMappers: Array[BinMapper]

    // histogramEntries, orderedGradients, orderedHessians = {
    def constructHistograms(usedFeature: Array[Boolean], dataIndices: Array[Int], numData: Int,
                            leafIdx: Int, orderedBins: Array[SparseColumn],
                            gradients: Array[Float], hessians: Array[Float]): (Array[Array[HistogramEntry]], Array[Float], Array[Float]) = {
        val orderedGradients = Array[Float](numData)
        val orderedHessians = Array[Float](numData)
        for (i <- 0 until numData) {
            orderedGradients(i) = gradients(dataIndices(i))
            orderedHessians(i) = hessians(dataIndices(i))
        }
        val result = ArrayBuffer[Array[HistogramEntry]]()
        for ((numBin, column) <- binNums.zip(columns)) {
            result += column.constructHistogram(leafIdx, numBin, dataIndices, orderedGradients, orderedHessians)
        }
        (result.toArray, orderedGradients, orderedHessians)
    }

    def fixHistogram(featureIdx: Int, sumGradient: Double, sumHessian: Double, numData: Int,
                     data: Array[HistogramEntry]): Unit = {
//        val binMapper = binMappers(featureIdx)
//        val defaultBin: Short = binMapper.defaultBin()
//        if (defaultBin > 0) {
//
//        }
//
//        const int default_bin = bin_mapper->GetDefaultBin();
//        if (default_bin > 0) {
//            const int num_bin = bin_mapper->num_bin();
//            data[default_bin].sum_gradients = sum_gradient;
//            data[default_bin].sum_hessians = sum_hessian;
//            data[default_bin].cnt = num_data;
//            for (int i = 0; i < num_bin; ++i) {
//                if (i != default_bin) {
//                    data[default_bin].sum_gradients -= data[i].sum_gradients;
//                    data[default_bin].sum_hessians -= data[i].sum_hessians;
//                    data[default_bin].cnt -= data[i].cnt;
//                }
//            }
//        }
    }

}
