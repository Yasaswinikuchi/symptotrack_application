"""
SymptoTrack Pro - Python Scikit-Learn Naive Bayes Training Script
Trains a Multinomial Naive Bayes (MultinomialNB) model on the master symptom dataset
"""

import json
from sklearn.feature_extraction.text import CountVectorizer, TfidfTransformer
from sklearn.naive_bayes import MultinomialNB
from sklearn.pipeline import Pipeline

# 1. Load Master Dataset
dataset_path = "master_symptom_disease_dataset.json"
with open(dataset_path, "r", encoding="utf-8") as f:
    data = json.load(f)

# 2. Extract Training Samples
X_train = []
y_train = []

for item in data:
    symptom_str = " ".join(item["symptoms"])
    X_train.append(symptom_str)
    y_train.append(item["disease_name"])

# 3. Build Machine Learning Pipeline (Bag-of-Words -> TF-IDF -> Multinomial Naive Bayes)
ml_pipeline = Pipeline([
    ('vect', CountVectorizer()),
    ('tfidf', TfidfTransformer()),
    ('clf', MultinomialNB(alpha=1.0))
])

# 4. Train Naive Bayes Classifier
ml_pipeline.fit(X_train, y_train)
print("✅ Multinomial Naive Bayes Model Trained Successfully!")

# 5. Run Test Prediction
sample_input = ["fever cough body pain low appetite"]
predicted_disease = ml_pipeline.predict(sample_input)[0]
probabilities = ml_pipeline.predict_proba(sample_input)

print(f"\nTest Input: {sample_input[0]}")
print(f"Predicted Diagnosis: {predicted_disease}")
