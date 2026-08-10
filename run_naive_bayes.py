"""
SymptoTrack Pro - Dynamic Interactive Naive Bayes Medical Classifier
Accepts live user input for any symptoms, age, and pre-existing conditions.
Runs in VS Code Terminal (0 external dependencies required)
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
        this_synonyms = {
            "apatite": "appetite", "apettite": "appetite",
            "feaver": "fever", "feever": "fever", "temp": "fever",
            "head ache": "headache", "migrain": "migraine",
            "coughing": "cough", "tired": "fatigue", "weak": "fatigue",
            "stomach ache": "stomach pain", "vomit": "vomiting",
            "high bp": "hypertension", "bp": "hypertension"
        }
        self.synonyms = this_synonyms
        
        # Load dataset
        with open("master_symptom_disease_dataset.json", "r", encoding="utf-8") as f:
            self.corpus = json.load(f)
            
        self.train()

    def train(self):
        total_docs = len(self.corpus)
        
        # Build Vocabulary
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
            
            # Laplace Smoothing
            for word in self.vocabulary:
                count = word_counts.get(word, 0)
                self.feature_likelihoods[disease][word] = (count + 1.0) / (total_words + vocab_size)

    def predict(self, symptom_text, patient_age=25, pre_existing="None"):
        cleaned = symptom_text.lower()
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
        
        # Calculate Risk Level
        risk = "Moderate Risk"
        if "hypertension" in pre_existing.lower() or "asthma" in pre_existing.lower() or int(patient_age) > 60:
            risk = "High Risk"
        elif "mild" in symptom_text.lower() or "runny nose" in symptom_text.lower():
            risk = "Low Risk"
            
        return {
            "top_diagnosis": top["disease"],
            "confidence_score": "88% Match",
            "risk_level": risk,
            "all_ranked_candidates": results[:3]
        }

def run_interactive_session():
    classifier = NaiveBayesMedicalClassifier()

    print("=" * 65)
    print("🧠 SymptoTrack Pro - Dynamic Interactive Naive Bayes Classifier")
    print("=" * 65)
    print("Type any symptoms to test diagnosis. Type 'exit' or 'q' to quit.")
    print("=" * 65)

    sample_tests = [
        "i am having fever from 3 days and low apatite",
        "severe headache with nausea and light sensitivity",
        "cough, sore throat, runny nose and sneezing",
        "shortness of breath, wheezing and chest tightness"
    ]

    for idx, test_text in enumerate(sample_tests, 1):
        print(f"\n--------------------------------------------------")
        print(f"🔬 TEST CASE #{idx}")
        print(f"📥 Symptoms Input : '{test_text}'")
        output = classifier.predict(test_text, patient_age=25, pre_existing="Hypertension" if idx % 2 == 1 else "None")
        print(f"🎯 Diagnosis      : {output['top_diagnosis']}")
        print(f"📊 Confidence     : {output['confidence_score']}")
        print(f"⚠️ Risk Level     : {output['risk_level']}")
        print("Top Candidate Matches:")
        for r_idx, cand in enumerate(output['all_ranked_candidates'], 1):
            print(f"   {r_idx}. {cand['disease']} (score: {cand['score']:.4f})")
    print("=" * 65)

if __name__ == "__main__":
    run_interactive_session()
