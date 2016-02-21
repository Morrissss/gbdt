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
