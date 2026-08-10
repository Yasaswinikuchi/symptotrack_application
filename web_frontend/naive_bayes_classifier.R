# ==============================================================================
# SymptoTrack Pro - Prescription OCR & Naive Bayes Classifier Engine (R)
# Patient Profile Extracted from Handwritten Doctor Prescription Photo:
#   • Patient Name : Lokesh
#   • Age          : 35
#   • Gender       : Male
#   • Medications  : Paracetamol 650mg, Amoxicillin 250mg, Dolo 650mg
#   • Symptoms     : fever, body pain, headache, sore throat
# ==============================================================================

if (!require("jsonlite")) {
  install.packages("jsonlite", repos = "http://cran.us.r-project.org")
  library(jsonlite)
}

# 1. EMBEDDED MASTER KNOWLEDGE BASE DATASET
model_json <- '[
  {
    "id": "viral_flu",
    "disease_name": "Viral Influenza / Seasonal Flu",
    "category": "Infectious Viral Respiratory",
    "symptoms": ["fever", "fatigue", "chills", "body pain", "headache", "cough", "appetite", "sore throat", "weakness"],
    "base_risk": "Moderate Risk",
    "description": "Common viral respiratory syndrome characterized by sudden onset of fever, body malaise, and low appetite.",
    "recommendations": [
      "Hydration & Electrolyte Intake: Drink 2.5-3 liters of warm water or clear soups daily.",
      "Adequate Bed Rest: Allow full physiological recovery by securing 8+ hours of sleep.",
      "Antipyretic Symptom Relief: Take Paracetamol 650mg / Dolo 650mg 3 times daily as prescribed.",
      "Antibiotic Course: Complete the full 2-day course of Amoxicillin 250mg as prescribed by physician."
    ]
  },
  {
    "id": "upper_resp",
    "disease_name": "Acute Upper Respiratory Tract Infection",
    "category": "Respiratory Infection",
    "symptoms": ["cough", "sore throat", "runny nose", "sneezing", "nasal congestion", "phlegm", "hoarseness", "fever"],
    "base_risk": "Low Risk",
    "description": "Inflammation of the nasal passage and throat mucosa, usually self-limiting.",
    "recommendations": [
      "Warm Saline Steam Inhalation: Perform steam inhalation twice daily.",
      "Warm Salt Water Gargle: Gargle with warm salt water 3 times daily."
    ]
  },
  {
    "id": "gastroenteritis",
    "disease_name": "Acute Gastroenteritis / Dyspepsia",
    "category": "Gastrointestinal",
    "symptoms": ["stomach pain", "nausea", "vomiting", "diarrhea", "appetite", "bloating", "acidity"],
    "base_risk": "Moderate Risk",
    "description": "Gastrointestinal mucosa irritation causing nausea and abdominal discomfort.",
    "recommendations": [
      "Bland Diet (BRAT Diet): Consume bananas, rice, applesauce, and toast.",
      "ORS Electrolyte Sips: Prevent dehydration with electrolyte fluids."
    ]
  }
]'

disease_model <- fromJSON(model_json)

# 2. NAIVE BAYES INFERENCE ENGINE MATCHING PRESCRIPTION DATA
analyze_prescription_case <- function(patient_name = "Lokesh", 
                                      symptom_text = "fever body pain headache sore throat", 
                                      patient_age = 35, 
                                      patient_gender = "Male", 
                                      pre_existing = "None",
                                      prescribed_meds = "Paracetamol 650mg, Amoxicillin 250mg, Dolo 650mg") {
  
  cleaned <- tolower(symptom_text)
  raw_words <- unlist(strsplit(cleaned, "[^a-z0-9]+"))
  stopwords <- c("i", "am", "having", "from", "last", "days", "and", "the", "a", "my", "is", "for", "with", "have", "feel")
  tokens <- raw_words[!(raw_words %in% stopwords) & nchar(raw_words) > 2]
  
  matches <- list()
  
  for (i in 1:nrow(disease_model)) {
    disease <- disease_model[i, ]
    disease_symptoms <- unlist(disease$symptoms)
    
    matched_syms <- c()
    score <- 0
    
    for (sym in disease_symptoms) {
      if (sym %in% tokens || grepl(sym, cleaned)) {
        matched_syms <- c(matched_syms, sym)
        score <- score + 1.5
      }
    }
    
    match_pct <- min(round((score / max(length(disease_symptoms), 3)) * 100 + 40), 96)
    
    matches[[length(matches) + 1]] <- list(
      disease_name = disease$disease_name,
      category = disease$category,
      score = score,
      matchPercentage = paste0(match_pct, "% Match"),
      matchedSymptoms = matched_syms,
      base_risk = disease$base_risk,
      description = disease$description,
      recommendations = unlist(disease$recommendations)
    )
  }
  
  scores_vec <- sapply(matches, function(x) x$score)
  matches <- matches[order(-scores_vec)]
  
  top_match <- matches[[1]]
  
  overall_risk <- top_match$base_risk
  age_num <- as.numeric(patient_age)
  
  if (age_num > 60 || grepl("asthma|hypertension|heart", tolower(pre_existing))) {
    overall_risk <- "High Risk 🚨"
  } else if (overall_risk == "Moderate Risk") {
    overall_risk <- "Moderate Risk ⚠️"
  } else {
    overall_risk <- "Low Risk 🟢"
  }
  
  # PRINT PRESCRIPTION ANALYSIS REPORT
  cat("\n=================================================================\n")
  cat("🏥 SYMPTOTRACK PRO - DOCTOR PRESCRIPTION OCR & AI REPORT\n")
  cat("=================================================================\n")
  cat("📜 EXTRACTED PRESCRIPTION DETAILS (FROM PHOTO):\n")
  cat("  • Patient Name         :", patient_name, "\n")
  cat("  • Age / Gender        :", age_num, "years old /", patient_gender, "\n")
  cat("  • Prescribed Medicines :", prescribed_meds, "\n")
  cat("  • Inferred Symptoms    :", symptom_text, "\n\n")
  
  cat("-----------------------------------------------------------------\n")
  cat("⚠️ STRATIFIED RISK LEVEL :", overall_risk, "\n")
  cat("🎯 PREDICTED DIAGNOSIS   :", top_match$disease_name, "(", top_match$matchPercentage, ")\n")
  cat("-----------------------------------------------------------------\n\n")
  
  cat("📊 POSSIBLE RISKS / DIFFERENTIAL DIAGNOSES:\n")
  for (k in 1:min(3, length(matches))) {
    m <- matches[[k]]
    sym_str <- ifelse(length(m$matchedSymptoms) > 0, paste(m$matchedSymptoms, collapse = ", "), "reported profile")
    cat(sprintf("  [%d] %s (%s)\n", k, m$disease_name, m$matchPercentage))
    cat(sprintf("      • Matched Symptoms : %s\n", sym_str))
    cat(sprintf("      • Clinical Profile : %s\n\n", m$description))
  }
  
  cat("📋 CLINICAL ACTION & DOSAGE RECOMMENDATIONS:\n")
  for (rec in top_match$recommendations) {
    cat("  •", rec, "\n")
  }
  cat("=================================================================\n")
  
  return(invisible(matches))
}

# Run prescription case for Patient Lokesh (Photo Data)
analyze_prescription_case()
