"""
SymptoTrack Pro - Live Real-Time Webpage Input Listener & Naive Bayes Classifier
Continuously monitors and classifies EVERY SINGLE input submitted on symptoms.html in real-time!
"""

import sys
import time
import json
import os

if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8')

from run_naive_bayes import NaiveBayesMedicalClassifier

log_file = "live_webpage_inputs.json"
classifier = NaiveBayesMedicalClassifier()

print("=" * 70)
print("📡 LIVE REAL-TIME WEBPAGE SYMPTOM WATCHER & NAIVE BAYES CLASSIFIER")
print("=" * 70)
print("Listening for live submissions from 'Describe how you feel' webpage...")
print("=" * 70)

last_mtime = 0

while True:
    try:
        if os.path.exists(log_file):
            mtime = os.path.getmtime(log_file)
            if mtime > last_mtime:
                last_mtime = mtime
                with open(log_file, "r", encoding="utf-8") as f:
                    payload = json.load(f)
                    
                symptoms_text = payload.get("symptomsText", "")
                age = payload.get("patientAge", 25)
                gender = payload.get("patientGender", "Female")
                conditions = payload.get("preExistingConditions", "None")
                timestamp = payload.get("timestamp", "")
                
                output = classifier.predict(symptoms_text, age, gender, conditions)
                
                print(f"\n⚡ [NEW LIVE WEBPAGE INPUT CAPTURED @ {timestamp}]")
                print(f"  • Describe how you feel : '{symptoms_text}'")
                print(f"  • Patient Age           : {age}")
                print(f"  • Patient Gender        : {gender}")
                print(f"  • Pre-existing Conditions: {conditions}")
                print("-" * 55)
                print(f"🎯 Diagnosis : {output['top_diagnosis']} ({output['confidence_score']})")
                print(f"⚠️ Risk Level: {output['risk_level']}")
                print("=" * 70)
    except Exception as e:
        pass
    time.sleep(1)
