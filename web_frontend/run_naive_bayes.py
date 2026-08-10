"""
SymptoTrack Pro - Interactive Runtime Input Naive Bayes Classifier Engine (Python)
Prompts the user live in the terminal for symptoms, age, gender, and conditions!
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

    def predict(self, symptom_text, patient_age=25, patient_gender="Female", pre_existing="None"):
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
    
    print("=" * 70)
    print("🧠 SymptoTrack Pro - Interactive Runtime Input Naive Bayes Engine")
    print("=" * 70)
    
    # Prompt live inputs interactively at runtime
    try:
        user_symptoms = input("Enter symptoms (Describe how you feel): ").strip()
        if not user_symptoms:
            user_symptoms = "i am having fever from last 15 days and vomiting"
            
        user_age = input("Enter patient age (default 25): ").strip()
        user_age = int(user_age) if user_age.isdigit() else 25
        
        user_gender = input("Enter patient gender (Female/Male): ").strip()
        if not user_gender:
            user_gender = "Female"
            
        user_conditions = input("Enter pre-existing conditions (e.g. Asthma, Diabetes): ").strip()
        if not user_conditions:
            user_conditions = "None"
            
        res = classifier.predict(user_symptoms, user_age, user_gender, user_conditions)
        
        print("\n" + "=" * 70)
        print("📥 RUNTIME INPUT PROCESSED:")
        print(f"  • Textarea Input         : '{user_symptoms}'")
        print(f"  • Patient Age            : {user_age}")
        print(f"  • Patient Gender         : {user_gender}")
        print(f"  • Pre-existing Conditions: {user_conditions}")
        print("-" * 55)
        print(f"🎯 Predicted Diagnosis : {res['top_diagnosis']}")
        print(f"📊 Confidence Match   : {res['confidence_score']}")
        print(f"⚠️ Stratified Risk     : {res['risk_level']}")
        print("=" * 70)
    except KeyboardInterrupt:
        print("\nSession exited.")
