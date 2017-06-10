[TOC]

# application.cpp

## LoadParameters
- config.h

## LoadData
- Predictor.cpp: PredictFunction
- dataset_loader.h: LoadFromFile
- metric.cpp: CreateMeric
- LoadFromFileAlignWithOtherDataset(validation data)

## InitTrain
- Network::Init
- init objective_fun
- init boosting
- AddValidDataset

## Train
- `num_iterations`
- TrainOneIter
- SaveModelToFile

# include

## feature.h
- `feature_index_`
- `bin_mapper_`：`BinMapper`
- `bin_data_`：`Bin`
- `is_sparse_`
- `CheckAlign`：比较index，调用`bin_mapper_->CheckAlign`

# io

## bin
- `NumericalBin`和`CategoricalBin`

### BinMapper
- feature value到bin
- `num_bin_`, `bin_upper_bound_`(`vector<double>`)：NumericalBin最后一个是Infinity，其他是两个bin
- `is_trival_`
- `sparse_rate_`：`cnt_in_bin[GetDefaultBin()] / num_data`
- `bin_type_`：`NumericalBin`或`CategoricalBin`
- `categorical_2_bin_`(`unordered_map<int, unsigned int>`), `bin_2_categorical_`
- `min_val_`, `max_val`
- `CopyTo(char* buffer)`：序列化
- `ValueToBin`：输入value
- `GetDefaultBin`：NumericalBin返回`ValueToBin(0)`，CategoricalBin返回0
- `FindBin`：输入非零values以及0的个数，修改对应feature的bins
 - 计算`distinct_values`和对应`counts`(用到排序)，包括0
 - 头尾为`min_val_`和`max_val_`
 - `NumericalBin`考虑`max_bin`和value数的大小关系(均分的规则比较tricky)，最后计算`bin_upper_bound_`
 - `CategoricalBin`：按count倒排，忽略占总数不到5%的分类，更新`bin_2_categorical_`和`categorical_2_bin_`
 - `num_bin_ <= 1`为`is_trival_`

### HistogramBinEntry
- `sum_gradients`, `sum_hessians`
- `cnt`：bin里的样本数
- `SumReducer`：dst对应的bins上叠加src对应的bins

### OrderedBin
- leaf的数据连续，只存非零的bin，但每次需要reorder，通常只用在sparse feature

#### ordered_sparse_bin.hpp
- `SparseBin`：
 - `deltas_`：下一个index跟当前的差，超过255记录余数，差除255继续计算`deltas_`和`vals_`
 - `vals_`：index差超过255，记一个`non_data_flag`
 - `NextNonzero`：返回boolean，修改传入的`i_delta`(`delta_`的下标)和`cur_pos`

- `SparseCategoricalBin`：
 - `Split`：查找bin index需要循环，比较threshold是否等于bin index

- `OrderedSparseBin`：
 - `ordered_pair_`(`SparsePair`)：row index，bin index
 - `leaf_start_[]`, `leaf_cnt_[]`：记录`ordered_pair_`的下标，表示leaf的数据
 - `bin_data_`：`SparseBin`数组
 - `Init`：填`ordered_pair_`
 - `ConstructHistogram`：`ordered_pair_`中取index，填写bin返回
 - `Split`：类似快排partition，注意`ordered_pair_`不按leaf index顺序。`leaf_start_`和`leaf_cnt_`为right_leaf赋值

### Bin
- data存储采用原始顺序，不需要重排序，更适合dense feature
- `Split`：根据threshold，返回两部分的data index数组

#### dense_bin.hpp
- `DenseBin`
 - `data_`存了每个sample对应的bin index
 - `ConstructHistogram`：给定leaf，gradients，hessians，返回`HistogramBinEntry`数组。只是简单通过`data_`查到下标计数&求和，输入都是连续的，用4-way unrolling加快计算速度
 - `Split`：输入的threshold是bin序号，查到`data_indices`的bin之后写入`lte_indices`或`gt_indices`
- `DenseCategoricalBin`：extends DenseBin
 - `Split`：等于threshold加入`gt_indices`，否则`lte_indices`

### sparse_bin.hpp

## dataset
- `used_feature_map_`输入的feature index到内部使用的feature index之间的映射关系，输入的feature index不一定覆盖所有小于`max_feature_idx_`的值
- `features_`：`vector<unique_ptr<Feature>>`
- `metadata_`
- `feature_names`, `num_features_`, `num_features_`, `num_data_`
- `label_idx_`：column index
- `CheckAlign`：简单校对两个dataset是否兼容，递归调用`Feature->CheckAlign`
- `PushOneRow`：每个feature加到对应的bin

### dataset_loader.cpp
- `LoadFromFile`：多台机器load数据，每台放全量数据，读取时用相同的随机序列，按模取自己的行
 - `use_two_round_loading=false`：先读`vector<string>`，采样一定个数`ConstructBinMappersFromTextData`构造dataset，`ExtractFeaturesFromMemory`把样本写到`Feature`
 - `use_two_round_loading=true`：节省内存，建bin之前只读采样过的数据，最后`ExtractFeaturesFromFile`重读一遍数据
- `LoadFromFileAlignWithOtherDataset`
- `ConstructBinMappersFromTextData`：在采样的`vector<string>`上建bin，返回`Dataset`，多机并发做：
 - parse出`vector<pair<int, double>>`和label
 - 非0值塞到`vector<vector<double>> sample_values`，此处`sample_values`的下标是样本文件中的feature index
 - 单机下：每个feature生成`BinMapper`调用`FindBin`，dataset中增加`Feature`
 - 多机下：每台机器计算不同feature(所以需要样本完全随机分布在不同机器上)，用`Allgather`拿到所有`BinMapper`，聚合成dataset中的`Feature`
 - 此时的`Feature`都是空的，只有`data_num_`。dataset中的feature index已经是内部的，与样本文件通过`used_feature_map_`转换
- ExtractFeaturesFromMemory：
 - 如果没设`predict_fun_`：parse每行，`PushData`到dataset的`Feature`，设置weight和query
 - 设了`predict_fun_`：用计算当前score写到`init_score`
 - 调用每个bin的`FinishLoad`

## metadata
- `num_data_`, `num_weights_`
- `label_`, `weights_`：`vector<float>`
- `query_boundaries`, `query_weights_`, `queries_`：用于lambdarank
- `init_score_`：默认全0
- `mutex_`

## tree.cpp
- 构造函数给定`max_leaves_`

# tree_learner.cpp
- `Init(train_data)`传入训练数据
- `Train(gradients, hessians)`返回一颗树
- `SetBaggingData(used_indices, num_data)`
- `AddPredictionToScore(out_score)`用最后一棵树计算score，然后加到`out_score`上

## data_partition.hpp
- 多线程
- `num_data_`, `num_leaves`：构造函数传入
- `indices_`：按leaf排序
- `leaf_begin_`, `leaf_count_`：每个leaf对应的`indices_`段
- `Init`：所有数据归root，`indices_`连续赋值，`leaf_count_=[num_data_, 0, ..., 0]`，`leaf_begin_[0]=0`。如果bagging使用`SetUsedDataIndices`传入的值
- `Split`：输入leaf index, right leaf index, feature bins, threshold
 - 对一个leaf上的sample，多线程分到`temp_left_indices_`和`temp_right_indices_`
 - 拷贝回`indices_`，更新`leaf_count_`和`leaf_begin_`

## leaf_splits.hpp
- 中间状态，在`DataPartition`上计算
- `best_split_per_feature_`：`vector<SplitInfo>`
- `leaf_index_`
- `num_data_in_leaf_`
- `num_data_`：training data总数
- `num_features_`, `sum_gradients_`, `sum_hessians_`
- `data_indices_`：`data_size_t*`

## split_info.hpp
- `feature`, `threshold`(`int`), `left_output`, `right_output`, `gain`, `left_count`, `right_count`, `left_sum_gradient`, `right_sum_gradient`, `right_sum_gradient`, `right_sum_hessian`
- `MaxReducer`：比较两个SplitInfo数组src和dst，将对应位置上较大的memcpy到dst
- `>`：选较大的gain，相同选较小的feature

## feature_histogram.cpp
- `Subtract`：`cnt`,`sum_gradients`,`sum_hessians`相减
- `FindBestThresholdForNumerical`：按顺序遍历bin，计算gain。样本数太少或者hessian和太小不做分隔
- `FindBestThresholdForCategorical`：选一个category做left
- `GetLeafSplitGain`：L1和L2约束直接加载叶子的score上，不同于xgboost的L1是叶子数(这个对应`min_gain_to_split`)
- `CalculateSplittedLeafOutput`

### HistogramPool
- LRU Cache若干数量的histogram，feature parallel的时候，`cache_size`小于`total_size`非常高效

## SerialTreeLearner
- `ResetTrainingData`：sparse用`ordered_bins_`，设置`smaller_leaf_splits``larger_leaf_splits_``data_partition`的大小为`num_data_`
- `Train`：
 - `BeforeTrain`：采样feature设置`is_feature_used_`，`ptr_to_ordered_gradients_smaller_leaf_``ptr_to_ordered_hessians_smaller_leaf_`指到计算好的数组上
 - 从root开始循环，直到`num_leaves_`或者gain<0
 - `BeforeFindBestSplit`：较小的赋给`smaller_leaf_histogram_array_``ptr_to_ordered_gradients_smaller_leaf_`，parent赋给`larger_leaf_histogram_array_``ptr_to_ordered_gradients_larger_leaf_`。算的是上次s`Split`出来的两个叶子
 - `FindBestThresholds`：计算smaller的histogram(`Bin`中存了所有sample的bin序号)，larger=larger-smaller，在smaller和larger上分别计算best split和gain
 - `FindBestSplitsForLeaves`：smaller和larger上分别`FindBestSplitForLeaf`，结果写到`vector<SplitInfo> best_split_per_leaf_`。注意这里不只写smaller和larger中gain大的那个，为了下一步一定能挑出最优
 - 选出最大的gain
 - `Split`：`tree->Split`更新各种tree里的数组，返回之前的`num_leaves_`作为right序号；`data_partition_->Split`重排`DataPartition->indices_`；把数量较小的设为smaller

## FeatureParallelTreeLearner
- 小数据量，每台机器都是全量数据，只需要两个SplitInfo的input buffer和同样大小的output buffer
- `BeforeTrain`：`SerialTreeLearner->BeforeTrain`，每台机器用同样的算法算出对应的feature，根据rank设置`is_feature_used_`
- `FindBestSplitsForLeaves`：
 - `FindBestThresholds`中只计算`is_feature_used_`的feature
 - 分别计算smaller和larger中的最大gain
 - 通过buffer做Allreduce(MaxReducer)
 - 写到`best_split_per_leaf_`

## DataParallelTreeLearner
- 大数据量少量feature
- `Init`：初始化各种MPI buffer
- `BeforeTrain`：每台机器需要算的feature记到`is_feature_aggregated_`，聚合所有机器上`smaller_leaf_splits`
- `FindBestThresholds`：
 - 聚合所有smaller并ReduceScatter到每台机器
 - 因为parent也是全量，所以larger依然可以减出来
 - 计算`is_feature_aggregated_`的feature中的best split和gain
- `FindBestSplitsForLeaves`：Allreduce写到`best_split_per_leaf_`
- `Split`：额外更新一下`global_data_count_in_leaf_`

## VotingParallelTreeLearner
- 大数据量大量feature，每台机器全量feature
- `BeforeTrain`：基本同`DataParallelTreeLearner`
- `BeforeFindBestSplit`：需要初始化`smaller_leaf_splits_`和`larger_leaf_splits_`
- `FindBestThresholds`：选出smaller和larger中的topK，Allgather后
- `FindBestSplitsForLeaves`
- `Split`

# objective_function.cpp

## RegressionL1Loss

## RegressionHuberLoss

## RegressionFairLoss

## BinaryLogloss
- `Init`：`label_weights`根据正负样本数计算，然后真样本上再乘`scale_pos_weight_`
- `GetGradients`：`label_weights`乘以`weights_`计算

## LambdarankNDCG

## MulticlassLogloss

# gbdt.cpp

## ResetTrainingData
- CheckAlign：保证feature配置和bin的映射都是一样的
- `tree_learner_->Init`:
- push `training_metrics`
- `train_score_updater_->AddScore`: reset score and create score racker。多分类问题每个iteration，每个class对应一棵树
- gradients & hessians：`num_data_ * num_class_`
- `feature_infos_`中记录`bin_info`(所有category或者最大最小值)
- 如果需要bagging：每个线程开辟临时变量空间，`bag_data_cnt_ = bagging_fraction * num_data_`。`average_bag_rate = bagging_fraction / bagging_freq`如果小于0.5初始化`tmp_subset_`并设置`is_use_subset_=true`

## AddValidDataset
- 可以有多个validation set，每个set上可以有不同metric

## Bagging
- 每`bagging_freq`次迭代进行一次
- `inner_size = num_data_ / num_threads_`
- 每个线程对应一段数据，按照`bagging_fraction`把index分成两部分，汇总之后left在前right在后
- 如果`is_use_subset_`，只把left赋给`tree_learner_`

## TrainOneIter
- `Boosting`并更新gradient和hessian
- `Bagging`，如果采用subset，重新采样并调整gradient和hessian向量顺序
- 对每个class：
 - `tree_learner_->Train`
 - shrinkage
 - UpdateScore
 - UpdateScoreOutOfBag：补上bagging之外的数据的score
 - 如果某个validation set[i]的某个metric[j]上`iter - best_iter_[i][j] > early_stopping_round`则停止(`early_stopping_round`个迭代没有进步)

## Boosting
- `object_function_->GetGradients`更新gradient和hessian

## FeatureImportance
- split次数计数

## SaveModelToString

## others
- `num_init_iteration_`支持增量学习

# score_updater.cpp
- 没有设置init_score则默认都是0.0
- score指当前该training sample对应的值(所有tree的和)

