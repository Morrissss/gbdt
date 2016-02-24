import pandas as pd
import numpy as np

data = pd.read_csv('/home/morris/github/gbdt/test.csv')
x = data[['feature0', 'feature1', 'feature2', 'feature3', 'feature4', 'feature5', 'feature6']]
y = data['clicked']

from sklearn.ensemble import GradientBoostingClassifier
gbdt = GradientBoostingClassifier(n_estimators=5, max_depth=2, min_samples_leaf=5, learning_rate=0.5)
gbdt.fit(x, y)

gbdt.init_.predict(x.ix[0])

regs = gbdt.estimators_[:, 0]
for i, reg in enumerate(regs):
    print("tree", i, ":")
    for f, th, v in zip(reg.tree_.feature, reg.tree_.threshold, reg.tree_.value.flatten()):
        print(f, th, v)

from sklearn.metrics import roc_auc_score
roc_auc_score(y, gbdt.predict_proba(x)[:, 1])

def save(model, filename, features):
    '''\
    dump scikit-learn gbdt model to file with more readable style
    '''
    map_join = lambda f, arr, sp: sp.join(map(f, arr))
    f = open(filename, 'w')
    f.write(' '.join(features) + '\n')
    f.write(str(gbdt._init_decision_function(np.zeros((1, len(features))))[0, 0]) + '\n')
    f.write(str(model.learning_rate) + '\n')
    f.write(str(model.min_samples_leaf) + '\n')
    trees = [estimator.tree_ for estimator in model.estimators_[:,0]]
    f.write(str(len(trees)) + '\n')
    for tree in trees:
        f.write(str(tree.node_count) + '\n')
        f.write(map_join(str, tree.children_left, ' ') + '\n')
        f.write(map_join(str, tree.children_right, ' ') + '\n')
        f.write(map_join(lambda x: features[x] if x>=0 else '#', tree.feature, ' ') + '\n')
        f.write(map_join(str, tree.threshold, ' ') + '\n')
        f.write(map_join(lambda x: str(x[0,0]), tree.value, ' ') + '\n')
        f.close()
