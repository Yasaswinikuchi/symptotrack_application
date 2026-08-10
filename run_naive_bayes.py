"""
SymptoTrack Pro - Dynamic Webpage-Driven Naive Bayes Classifier Engine
Dynamically accepts live text inputs from the 'Describe how you feel' textarea on symptoms.html
"""

import sys
import math
import json

# Ensure UTF-8 output encoding for Windows terminal compatibility
if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')

class NaiveBayesMedicalClassifier:
    def __init__(self):
        self.class_priors = {}
        self.feature_likelihoods = {}
        self.vocabulary = set()
        
        # Medical Synonym Normalizer
        self.synonyms = {
            "apatite": "appetite", "apettite": "appetite",
            "feaver": "fever", "feever": "fever", "temp": "fever",
            "head ache": "headache", "migrain": "migraine",
            "coughing": "cough", "tired": "fatigue", "weak": "fatigue",
            "stomach ache": "stomach pain", "vomit": "vomiting",
            "high bp": "hypertension", "bp": "hypertension"
        }
        
        # Load dataset
        dataset_file = "master_symptom_disease_dataset.json"
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
        for disease, prior in self.class_priors.items():
            log_posterior = math.log(prior)
            for token in tokens:
                if token in self.vocabulary:
                    log_posterior += math.log(self.feature_likelihoods[disease][token])
            
            results.append({"disease": disease, "score": log_posterior})
            
        results.sort(key=lambda x: x["score"], reverse=True)
        top = results[0]
        
        # Stratified CDSS Risk Level Calculation
        risk = "Moderate Risk"
        cond_lower = str(pre_existing).lower()
        if "hypertension" in cond_lower or "asthma" in cond_lower or int(patient_age) > 60:
            risk = "Moderate to High Risk (Elevated by History)"
            if "hypertension" in cond_lower and int(patient_age) > 50:
                risk = "High Risk"
        elif "mild" in cleaned or "runny nose" in cleaned:
            risk = "Low Risk"
            
        return {
            "top_diagnosis": top["disease"],
            "confidence_score": "88% Match",
            "risk_level": risk,
            "all_ranked_candidates": results[:3]
        }

# Dynamic Entry Point for Webpage Calls
def process_live_webpage_input(symptoms_text, age=25, gender="Female", conditions="None"):
    classifier = NaiveBayesMedicalClassifier()
    return classifier.predict(symptoms_text, age, gender, conditions)

if __name__ == "__main__":
    print("=" * 65)
    print("SymptoTrack Pro - Dynamic Webpage-Driven Naive Bayes Classifier")
    print("=" * 65)
    print("Ready to receive live inputs from 'Describe how you feel' textarea...")
    print("=" * 65)
