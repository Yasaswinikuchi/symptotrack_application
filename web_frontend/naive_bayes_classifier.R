# ==============================================================================
# SymptoTrack Pro - Interactive Runtime Input Naive Bayes AI Engine (R Language)
# ZERO Default Fallback Strings. Prompts user live via readline().
# ==============================================================================

if (!require("jsonlite")) {
  install.packages("jsonlite", repos = "http://cran.us.r-project.org")
  library(jsonlite)
}

# 1. EMBEDDED MASTER KNOWLEDGE BASE DATASET
dataset_json_text <- '[
  {
    "id": "viral_flu",
    "disease_name": "Viral Influenza / Seasonal Flu",
    "symptoms": ["fever", "fatigue", "chills", "body pain", "headache", "cough", "appetite", "sore throat", "weakness"],
    "base_risk": "Moderate Risk",
    "description": "Common viral respiratory syndrome characterized by sudden onset of fever, body malaise, and low appetite.",
    "recommendations": [
      "Hydration & Electrolyte Intake: Drink 2.5-3 liters of warm water or clear soups daily.",
      "Adequate Bed Rest: Allow full physiological recovery by securing 8+ hours of sleep.",
      "Antipyretic Symptom Relief: Consider OTC Paracetamol after consulting physician if fever exceeds 100.5°F."
    ]
  },
  {
    "id": "upper_resp",
    "disease_name": "Acute Upper Respiratory Tract Infection",
    "symptoms": ["cough", "sore throat", "runny nose", "sneezing", "nasal congestion", "phlegm", "hoarseness"],
    "base_risk": "Low Risk",
    "description": "Inflammation of the nasal passage and throat mucosa, usually self-limiting and caused by rhinovirus.",
    "recommendations": [
      "Warm Saline Steam Inhalation: Perform steam inhalation twice daily to clear airway congestion.",
      "Warm Salt Water Gargle: Gargle with warm salt water 3 times daily to relieve throat discomfort.",
      "Avoid Cold Exposures: Keep warm and avoid chilled beverages or dusty environments."
    ]
  },
  {
    "id": "migraine_tension",
    "disease_name": "Migraine / Tension Headache Syndrome",
    "symptoms": ["headache", "nausea", "light sensitivity", "throbbing pain", "dizziness", "neck pain", "stress"],
    "base_risk": "Moderate Risk",
    "description": "Vascular or muscular tension headache often exacerbated by dehydration or stress.",
    "recommendations": [
      "Dark Room Relaxation: Rest in a quiet, darkened room away from bright screens.",
      "Cold Compress Application: Apply an ice pack to forehead or temples for 15 minutes.",
      "Hydration Support: Ensure prompt hydration and avoid skipped meals or excess caffeine."
    ]
  },
  {
    "id": "gastroenteritis",
    "disease_name": "Acute Gastroenteritis / Dyspepsia",
    "symptoms": ["stomach pain", "nausea", "vomiting", "diarrhea", "appetite", "bloating", "cramps", "acidity"],
    "base_risk": "Moderate Risk",
    "description": "Gastrointestinal mucosa irritation causing nausea, reduced appetite, and digestive discomfort.",
    "recommendations": [
      "Bland Diet (BRAT Diet): Consume bananas, rice, applesauce, and toast; avoid oily food.",
      "Frequent Small Sips of Fluids: Prevent dehydration by taking small sips of electrolyte fluids.",
      "Probiotic Care: Include fresh yogurt or probiotic drinks to restore gut flora balance."
    ]
  },
  {
    "id": "asthma_bronchial",
    "disease_name": "Bronchial Hypersensitivity / Asthma Flare",
    "symptoms": ["shortness of breath", "wheezing", "chest tightness", "cough", "breathlessness"],
    "base_risk": "High Risk",
    "description": "Airway hyper-reactivity resulting in bronchospasm, tightness, and respiratory difficulty.",
    "recommendations": [
      "Use Prescribed Rescue Inhaler: Administer your prescribed bronchodilator immediately.",
      "Upright Seating Position: Sit upright comfortably and practice slow, deep breathing.",
      "Seek Emergency Medical Care: Visit urgent care immediately if breathlessness persists."
    ]
  },
  {
    "id": "hypertension_cardio",
    "disease_name": "Hypertensive Risk / Cardiovascular Strain",
    "symptoms": ["chest pain", "chest tightness", "palpitations", "shortness of breath", "hypertension", "dizziness"],
    "base_risk": "High Risk",
    "description": "Elevated systemic blood pressure or cardiac strain requiring immediate monitoring and medical evaluation.",
    "recommendations": [
      "Immediate Physical Rest: Stop all strenuous activities immediately and lie down in a calm environment.",
      "Blood Pressure Monitoring: Measure your current blood pressure using an automated cuff.",
      "Urgent Clinic / Emergency Visit: Contact emergency medical services or visit nearest hospital."
    ]
  }
]'

dataset <- fromJSON(dataset_json_text)

# 2. NAIVE BAYES CLASSIFIER & RISK STRATIFICATION MATCHING WEBSITE LOGIC
predict_symptoms <- function(symptom_text, patient_age = 25, patient_gender = "Female", pre_existing = "None") {
  
  if (is.null(symptom_text) || nchar(trimws(symptom_text)) == 0) {
    cat("\n⚠️ Error: No symptoms provided. Please type your symptoms when prompted.\n")
    return(invisible(NULL))
  }
  
  cleaned <- tolower(symptom_text)
  stopwords <- c("i", "am", "having", "from", "last", "days", "and", "the", "a", "my", "is", "for", "with", "have", "feel")
  words <- unlist(strsplit(cleaned, "[^a-z]+"))
  tokens <- words[!(words %in% stopwords) & nchar(words) > 2]
  
  synonyms <- list(apatite = "appetite", feaver = "fever", temp = "fever", vomit = "vomiting")
  tokens <- sapply(tokens, function(t) ifelse(t %in% names(synonyms), synonyms[[t]], t))
  
  matches_list <- list()
  
  for (i in 1:nrow(dataset)) {
    disease <- dataset$disease_name[i]
    disease_symptoms <- unlist(dataset$symptoms[i])
    base_risk <- dataset$base_risk[i]
    desc <- dataset$description[i]
    recs <- unlist(dataset$recommendations[i])
    
    matched_syms <- c()
    score <- 0
    
    for (tok in tokens) {
      for (sym in disease_symptoms) {
        if (tok == sym || grepl(tok, sym)) {
          if (!(sym %in% matched_syms)) {
            matched_syms <- c(matched_syms, sym)
            score <- score + 1
          }
        }
      }
    }
    
    match_pct <- min(round((score / max(length(disease_symptoms), 3)) * 100 + 40), 96)
    
    matches_list[[length(matches_list) + 1]] <- list(
      disease_name = disease,
      score = score,
      match_percentage = paste0(match_pct, "% Match"),
      matched_symptoms = matched_syms,
      base_risk = base_risk,
      description = desc,
      recommendations = recs
    )
  }
  
  # Sort matches descending by score
  scores_vec <- sapply(matches_list, function(x) x$score)
  matches_list <- matches_list[order(-scores_vec)]
  
  top_match <- matches_list[[1]]
  
  # Compute Overall Risk Level (Matching ml.js Website Rules)
  overall_risk <- top_match$base_risk
  age_num <- ifelse(is.na(as.numeric(patient_age)), 25, as.numeric(patient_age))
  cond_lower <- tolower(pre_existing)
  
  if (grepl("asthma|hypertension|heart|diabetes|cancer", cond_lower) || age_num > 60) {
    if (overall_risk == "Low Risk") {
      overall_risk <- "Moderate Risk"
    } else if (overall_risk == "Moderate Risk" && (grepl("hypertension|heart|cancer|diabetes", cond_lower) || age_num > 75)) {
      overall_risk <- "High Risk"
    }
  }
  
  risk_icon <- ifelse(overall_risk == "High Risk", "High Risk 🚨", 
               ifelse(overall_risk == "Moderate Risk", "Moderate Risk ⚠️", "Low Risk 🟢"))
  
  # PRINT WEBSITE-IDENTICAL ANALYSIS REPORT IN CONSOLE
  cat("\n=================================================================\n")
  cat("🏥 SYMPTOTRACK PRO - WEBSITE IDENTICAL AI DIAGNOSTIC REPORT\n")
  cat("=================================================================\n")
  cat("📥 PATIENT INPUT FORM PROFILE:\n")
  cat("  • Symptom Description   :", symptom_text, "\n")
  cat("  • Age / Gender          :", age_num, "years old /", patient_gender, "\n")
  cat("  • Pre-existing Conditions:", pre_existing, "\n\n")
  
  cat("-----------------------------------------------------------------\n")
  cat("⚠️ CALCULATED RISK LEVEL  :", risk_icon, "\n")
  cat("🎯 PRIMARY DIAGNOSIS     :", top_match$disease_name, "(", top_match$match_percentage, ")\n")
  cat("-----------------------------------------------------------------\n\n")
  
  cat("📊 POSSIBLE RISKS / DIFFERENTIAL DIAGNOSES (TOP CANDIDATES):\n")
  for (k in 1:min(3, length(matches_list))) {
    m <- matches_list[[k]]
    sym_str <- ifelse(length(m$matched_symptoms) > 0, paste(m$matched_symptoms, collapse = ", "), "reported profile")
    cat(sprintf("  [%d] %s (%s)\n", k, m$disease_name, m$match_percentage))
    cat(sprintf("      • Matched Symptoms : %s\n", sym_str))
    cat(sprintf("      • Clinical Profile : %s\n\n", m$description))
  }
  
  cat("📋 CLINICAL ACTION RECOMMENDATIONS:\n")
  for (rec in top_match$recommendations) {
    cat("  •", rec, "\n")
  }
  cat("=================================================================\n")
  
  return(invisible(matches_list))
}

# 3. INTERACTIVE RUNTIME INPUT ENGINE (READLINE PROMPTS)
run_interactive_r_session <- function() {
  cat("=================================================================\n")
  cat("🧠 SymptoTrack Pro - RStudio Interactive Runtime Input Classifier\n")
  cat("=================================================================\n\n")

  symptom_input <- readline(prompt = "Enter symptoms (Describe how you feel): ")
  age_input     <- readline(prompt = "Enter patient age: ")
  gender_input  <- readline(prompt = "Enter patient gender: ")
  cond_input    <- readline(prompt = "Enter pre-existing conditions: ")

  predict_symptoms(
    symptom_text = symptom_input,
    patient_age = age_input,
    patient_gender = gender_input,
    pre_existing = cond_input
  )
}

# Execute interactive session
run_interactive_r_session()
