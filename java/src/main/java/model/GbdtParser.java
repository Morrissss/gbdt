package model;

/**
 * Model file should be saved using:
 * def save(model, filename, features):
 *     map_join = lambda f, arr, sp: sp.join(map(f, arr))
 *     f = open(filename, 'w')
 *     f.write(' '.join(features) + '\n')
 *     f.write(str(gbdt._init_decision_function(np.zeros((1, len(features))))[0, 0]) + '\n')
 *     f.write(str(model.learning_rate) + '\n')
 *     f.write(str(model.min_samples_leaf) + '\n')
 *     trees = [estimator.tree_ for estimator in model.estimators_[:,0]]
 *     f.write(str(len(trees)) + '\n')
 *     for tree in trees:
 *     f.write(str(tree.node_count) + '\n')
 *     f.write(map_join(str, tree.children_left, ' ') + '\n')
 *     f.write(map_join(str, tree.children_right, ' ') + '\n')
 *     f.write(map_join(lambda x: features[x] if x>=0 else '#', tree.feature, ' ') + '\n')
 *     f.write(map_join(str, tree.threshold, ' ') + '\n')
 *     f.write(map_join(lambda x: str(x[0,0]), tree.value, ' ') + '\n')
 *     f.close()
 */
public class GbdtParser {

}
