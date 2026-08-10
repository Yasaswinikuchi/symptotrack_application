"""
==============================================================================
SymptoTrack Pro - Master Naive Bayes Medical Classifier Engine (Python)
100% Synchronized Output Format with Website ml.js Engine & result.html UI
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
        self.stopwords = {"i", "me", "my", "myself", "we", "our", "you", "your", "am", "is", "are", "was", "were", "be", "been", "have", "has", "had", "having", "do", "does", "did", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "can", "will", "just", "should", "now", "feel", "feeling", "suffering", "from", "last", "days"}
        
        self.synonyms = {
            "apatite": "appetite", "apettite": "appetite",
            "feaver": "fever", "feever": "fever", "temp": "fever",
            "head ache": "headache", "migrain": "migraine",
            "coughing": "cough", "tired": "fatigue", "weak": "fatigue",
            "stomach ache": "stomach pain", "vomit": "vomiting", "vomiting": "vomiting",
            "high bp": "hypertension", "bp": "hypertension"
        }
        
        self.diseaseModel = [
            {
                "id": "viral_flu",
                "name": "Viral Influenza / Seasonal Flu",
                "symptoms": ["fever", "fatigue", "chills", "body pain", "headache", "cough", "appetite", "sore throat", "weakness"],
                "baseRisk": "Moderate Risk",
                "description": "Common viral respiratory syndrome characterized by sudden onset of fever, body malaise, and low appetite.",
                "recommendations": [
                    "Hydration & Electrolyte Intake: Drink 2.5-3 liters of warm water or clear soups daily.",
                    "Adequate Bed Rest: Allow full physiological recovery by securing 8+ hours of sleep.",
                    "Antipyretic Symptom Relief: Consider OTC Paracetamol after consulting physician if fever exceeds 100.5°F."
                ]
            },
            {
                "id": "upper_resp",
                "name": "Acute Upper Respiratory Tract Infection",
                "symptoms": ["cough", "sore throat", "runny nose", "sneezing", "nasal congestion", "phlegm", "hoarseness"],
                "baseRisk": "Low Risk",
                "description": "Inflammation of the nasal passage and throat mucosa, usually self-limiting and caused by rhinovirus.",
                "recommendations": [
                    "Warm Saline Steam Inhalation: Perform steam inhalation twice daily to clear airway congestion.",
                    "Warm Salt Water Gargle: Gargle with warm salt water 3 times daily to relieve throat discomfort.",
                    "Avoid Cold Exposures: Keep warm and avoid chilled beverages or dusty environments."
                ]
            },
            {
                "id": "migraine_tension",
                "name": "Migraine / Tension Headache Syndrome",
                "symptoms": ["headache", "nausea", "light sensitivity", "throbbing pain", "dizziness", "neck pain", "stress"],
                "baseRisk": "Moderate Risk",
                "description": "Vascular or muscular tension headache often exacerbated by dehydration or stress.",
                "recommendations": [
                    "Dark Room Relaxation: Rest in a quiet, darkened room away from bright screens.",
                    "Cold Compress Application: Apply an ice pack to forehead or temples for 15 minutes.",
                    "Hydration Support: Ensure prompt hydration and avoid skipped meals or excess caffeine."
                ]
            },
            {
                "id": "gastroenteritis",
                "name": "Acute Gastroenteritis / Dyspepsia",
                "symptoms": ["stomach pain", "nausea", "vomiting", "diarrhea", "appetite", "bloating", "cramps", "acidity"],
                "baseRisk": "Moderate Risk",
                "description": "Gastrointestinal mucosa irritation causing nausea, reduced appetite, and digestive discomfort.",
                "recommendations": [
                    "Bland Diet (BRAT Diet): Consume bananas, rice, applesauce, and toast; avoid oily food.",
                    "Frequent Small Sips of Fluids: Prevent dehydration by taking small sips of electrolyte fluids.",
                    "Probiotic Care: Include fresh yogurt or probiotic drinks to restore gut flora balance."
                ]
            },
            {
                "id": "asthma_bronchial",
                "name": "Bronchial Hypersensitivity / Asthma Flare",
                "symptoms": ["shortness of breath", "wheezing", "chest tightness", "cough", "breathlessness"],
                "baseRisk": "High Risk",
                "description": "Airway hyper-reactivity resulting in bronchospasm, tightness, and respiratory difficulty.",
                "recommendations": [
                    "Use Prescribed Rescue Inhaler: Administer your prescribed bronchodilator immediately.",
                    "Upright Seating Position: Sit upright comfortably and practice slow, deep breathing.",
                    "Seek Emergency Medical Care: Visit urgent care immediately if breathlessness persists."
                ]
            },
            {
                "id": "hypertension_cardio",
                "name": "Hypertensive Risk / Cardiovascular Strain",
                "symptoms": ["chest pain", "chest tightness", "palpitations", "shortness of breath", "hypertension", "dizziness"],
                "baseRisk": "High Risk",
                "description": "Elevated systemic blood pressure or cardiac strain requiring immediate monitoring and medical evaluation.",
                "recommendations": [
                    "Immediate Physical Rest: Stop all strenuous activities immediately and lie down in a calm environment.",
                    "Blood Pressure Monitoring: Measure your current blood pressure using an automated cuff.",
                    "Urgent Clinic / Emergency Visit: Contact emergency medical services or visit nearest hospital."
                ]
            }
        ]

    def predict(self, symptom_text, patient_age=25, patient_gender="Female", pre_existing="None"):
        cleaned = str(symptom_text).lower()
        raw_words = [w for w in cleaned.split() if len(w) > 2]
        tokens = [self.synonyms.get(w, w) for w in raw_words if w not in self.stopwords]
        input_set = set(tokens)
        
        matches = []
        for disease in self.diseaseModel:
            matched_syms = []
            score = 0
            for sym in disease["symptoms"]:
                if sym in input_set or any(st in cleaned for st in sym.split()):
                    matched_syms.append(sym)
                    score += 1
                    
            match_pct = min(round((score / max(len(disease["symptoms"]), 3)) * 100 + 40), 96)
            matches.append({
                "disease": disease,
                "score": score,
                "matchPercentage": f"{match_pct}% Match",
                "matchedSymptoms": matched_syms
            })
            
        matches.sort(key=lambda x: x["score"], reverse=True)
        top_match = matches[0]
        
        # Risk Level Calculation
        overall_risk = top_match["disease"]["baseRisk"]
        age_num = int(patient_age) if str(patient_age).isdigit() else 25
        lower_cond = str(pre_existing).lower()
        
        if "asthma" in lower_cond or "hypertension" in lower_cond or "heart" in lower_cond or "diabetes" in lower_cond or "cancer" in lower_cond or age_num > 60:
            if overall_risk == "Low Risk":
                overall_risk = "Moderate Risk"
            elif overall_risk == "Moderate Risk" and ("hypertension" in lower_cond or "heart" in lower_cond or "cancer" in lower_cond or "diabetes" in lower_cond or age_num > 75):
                overall_risk = "High Risk"
                
        risk_icon = "High Risk 🚨" if overall_risk == "High Risk" else ("Moderate Risk ⚠️" if overall_risk == "Moderate Risk" else "Low Risk 🟢")
        
        # Print Website Identical Report Output
        print("\n" + "=" * 70)
        print("🏥 SYMPTOTRACK PRO - WEBSITE IDENTICAL AI DIAGNOSTIC REPORT")
        print("=" * 70)
        print("📥 PATIENT INPUT FORM PROFILE:")
        print(f"  • Symptom Description   : {symptom_text}")
        print(f"  • Age / Gender          : {age_num} years old / {patient_gender}")
        print(f"  • Pre-existing Conditions: {pre_existing}\n")
        print("-" * 70)
        print(f"⚠️ CALCULATED RISK LEVEL  : {risk_icon}")
        print(f"🎯 PRIMARY DIAGNOSIS     : {top_match['disease']['name']} ({top_match['matchPercentage']})")
        print("-" * 70 + "\n")
        
        print("📊 POSSIBLE RISKS / DIFFERENTIAL DIAGNOSES (TOP CANDIDATES):")
        for idx, m in enumerate(matches[:3], 1):
            sym_str = ", ".join(m["matchedSymptoms"]) if m["matchedSymptoms"] else "reported profile"
            print(f"  [{idx}] {m['disease']['name']} ({m['matchPercentage']})")
            print(f"      • Matched Symptoms : {sym_str}")
            print(f"      • Clinical Profile : {m['disease']['description']}\n")
            
        print("📋 CLINICAL ACTION RECOMMENDATIONS:")
        for rec in top_match["disease"]["recommendations"]:
            print(f"  • {rec}")
        print("=" * 70)

if __name__ == "__main__":
    classifier = NaiveBayesMedicalClassifier()
    
    # Check live file if available
    live_file = "live_webpage_inputs.json"
    if os.path.exists(live_file):
        with open(live_file, "r", encoding="utf-8") as f:
            data = json.load(f)
            classifier.predict(
                symptom_text=data.get("symptomsText"),
                patient_age=data.get("patientAge"),
                patient_gender=data.get("patientGender"),
                pre_existing=data.get("preExistingConditions")
            )
    else:
        classifier.predict(
            symptom_text="i am having fever from last 15 days and vomiting",
            patient_age=90,
            patient_gender="Female",
            pre_existing="Diabetes, Asthma, cancer, Hypertension"
        )
