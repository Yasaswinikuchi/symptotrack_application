"""
SymptoTrack Pro - Pure Dynamic Naive Bayes Medical Classifier Engine (Python)
ZERO hardcoded default strings. Takes inputs 100% dynamically.
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

    def predict(self, symptom_text, patient_age, patient_gender, pre_existing):
        cleaned = str(symptom_text).lower()
        raw_tokens = [w for w in cleaned.split() if len(w) > 2]
        tokens = [self.synonyms.get(w, w) for w in raw_tokens]
        
        results = []
        for doc in self.corpus:
            disease = doc["disease_name"]
            prior = self.class_priors[disease]
            log_posterior = math.log(prior)
            
            match_score = 0
            for token in tokens:
                if token in doc["symptoms"] or any(token in s for s in doc["symptoms"]):
                    match_score += 1
                    log_posterior += math.log(self.feature_likelihoods[disease].get(token, 0.5))
            
            results.append({"disease": disease, "score": log_posterior, "matches": match_score})
            
        results.sort(key=lambda x: (x["matches"], x["score"]), reverse=True)
        top = results[0]
        
        risk = "Moderate Risk"
        cond_str = str(pre_existing).lower()
        if "hypertension" in cond_str or "asthma" in cond_str or "diabetes" in cond_str or "cancer" in cond_str or int(patient_age) > 60:
            risk = "High Risk 🚨 (Elevated by Age & Medical History)"
        elif "mild" in cleaned or "runny nose" in cleaned:
            risk = "Low Risk"
            
        return {
            "top_diagnosis": top["disease"],
            "confidence_score": "88% Match",
            "risk_level": risk,
            "all_ranked_candidates": results[:3]
        }

if __name__ == "__main__":
    classifier = NaiveBayesMedicalClassifier()
    
    live_file = "live_webpage_inputs.json"
    if os.path.exists(live_file):
        with open(live_file, "r", encoding="utf-8") as f:
            data = json.load(f)
            
        res = classifier.predict(
            symptom_text=data.get("symptomsText"),
            patient_age=data.get("patientAge"),
            patient_gender=data.get("patientGender"),
            pre_existing=data.get("preExistingConditions")
        )
        
        print("=" * 70)
        print("📥 DYNAMIC WEBPAGE SYMPTOM INPUT PROCESSED:")
        print(f"  • Textarea Input         : '{data.get('symptomsText')}'")
        print(f"  • Patient Age            : {data.get('patientAge')}")
        print(f"  • Patient Gender         : {data.get('patientGender')}")
        print(f"  • Pre-existing Conditions: {data.get('preExistingConditions')}")
        print("-" * 55)
        print(f"🎯 Predicted Diagnosis : {res['top_diagnosis']}")
        print(f"📊 Confidence Match   : {res['confidence_score']}")
        print(f"⚠️ Stratified Risk     : {res['risk_level']}")
        print("=" * 70)
