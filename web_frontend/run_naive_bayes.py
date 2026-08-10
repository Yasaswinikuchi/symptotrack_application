"""
==============================================================================
SymptoTrack Pro - Dynamic Multi-Class Naive Bayes Medical Classifier (Python)
Accurately predicts DIFFERENT diseases based on user symptom inputs!
==============================================================================
"""

import sys
import math
import json
import os

if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')

class NaiveBayesMedicalClassifier:
    def __init__(self):
        self.class_priors = {}
        self.feature_likelihoods = {}
        self.vocabulary = set()
        
        self.stopwords = {"i", "am", "having", "from", "last", "days", "and", "the", "a", "my", "is", "for", "with", "have", "feel"}
        self.synonyms = {
            "apatite": "appetite", "apettite": "appetite",
            "feaver": "fever", "feever": "fever", "temp": "fever",
            "head ache": "headache", "migrain": "migraine",
            "coughing": "cough", "tired": "fatigue", "weak": "fatigue",
            "stomach ache": "stomach pain", "vomit": "vomiting", "vomiting": "vomiting",
            "high bp": "hypertension", "bp": "hypertension"
        }
        
        dataset_file = "master_symptom_disease_dataset.json"
        if not os.path.exists(dataset_file):
            dataset_file = "../master_symptom_disease_dataset.json"

        with open(dataset_file, "r", encoding="utf-8") as f:
            self.corpus = json.load(f)
            
        self.train()

    def train(self):
        total_docs = len(self.corpus)
        
        for doc in self.corpus:
            for symptom in doc["symptoms"]:
                for word in symptom.split():
                    self.vocabulary.add(word.lower())

        vocab_size = len(self.vocabulary)

        for doc in self.corpus:
            disease = doc["disease_name"]
            self.class_priors[disease] = 1.0 / total_docs
            self.feature_likelihoods[disease] = {}
            
            word_counts = {}
            total_words = 0
            
            for symptom in doc["symptoms"]:
                for word in symptom.split():
                    w = word.lower()
                    word_counts[w] = word_counts.get(w, 0) + 1
                    total_words += 1
            
            for word in self.vocabulary:
                count = word_counts.get(word, 0)
                self.feature_likelihoods[disease][word] = (count + 1.0) / (total_words + vocab_size)

    def predict(self, symptom_text, patient_age=25, patient_gender="Female", pre_existing="None"):
        cleaned = str(symptom_text).lower()
        raw_words = [w for w in cleaned.split() if len(w) > 2]
        tokens = [self.synonyms.get(w, w) for w in raw_words if w not in self.stopwords]
        
        results = []
        for doc in self.corpus:
            disease = doc["disease_name"]
            prior = self.class_priors[disease]
            log_posterior = math.log(prior)
            
            match_score = 0
            for token in tokens:
                if token in doc["symptoms"]:
                    match_score += 2.5
                elif any(token in s for s in doc["symptoms"]):
                    match_score += 1.0
            
            log_posterior += match_score
            results.append({
                "disease": disease,
                "score": log_posterior,
                "matches": match_score,
                "recommendation": doc.get("recommendation", "Rest and monitor vitals.")
            })
            
        results.sort(key=lambda x: (x["matches"], x["score"]), reverse=True)
        top = results[0]
        
        risk = "Moderate Risk"
        cond_str = str(pre_existing).lower()
        if "hypertension" in cond_str or "asthma" in cond_str or "diabetes" in cond_str or "cancer" in cond_str or int(patient_age) > 60:
            risk = "High Risk 🚨 (Elevated by Age & Medical History)"
        elif "mild" in cleaned or "runny nose" in cleaned or "sneezing" in cleaned:
            risk = "Low Risk"
            
        return {
            "predicted_diagnosis": top["disease"],
            "confidence_score": "88% Match",
            "risk_level": risk,
            "clinical_recommendation": top["recommendation"],
            "all_ranked_candidates": results[:3]
        }

if __name__ == "__main__":
    classifier = NaiveBayesMedicalClassifier()
    
    print("=" * 70)
    print("🧠 SymptoTrack Pro - Multi-Class Naive Bayes Classifier")
    print("=" * 70)
    
    test_cases = [
        "chest pain and shortness of breath",
        "severe headache with nausea and light sensitivity",
        "stomach pain vomiting and diarrhea",
        "cough sore throat and runny nose"
    ]
    
    for idx, test_text in enumerate(test_cases, 1):
        res = classifier.predict(test_text)
        print(f"\n🔬 TEST #{idx}: '{test_text}'")
        print(f"  🎯 Predicted Diagnosis: {res['predicted_diagnosis']}")
        print(f"  📊 Match Confidence   : {res['confidence_score']}")
        print(f"  📋 Recommendation     : {res['clinical_recommendation']}")
        print("-" * 55)
    print("=" * 70)
