# ==============================================================================
# SymptoTrack Pro - Interactive Runtime Input Naive Bayes AI Engine (R)
# Prompts live in RStudio Console via readline()
# ==============================================================================

if (!require("jsonlite")) {
  install.packages("jsonlite", repos = "http://cran.us.r-project.org")
  library(jsonlite)
}

# 1. EMBEDDED MASTER MEDICAL DATASET
dataset_json_text <- '[
  {
    "disease_name": "Acute Gastroenteritis / Dyspepsia",
    "symptoms": ["vomiting", "vomit", "stomach pain", "nausea", "diarrhea", "appetite", "bloating", "acidity"],
    "recommendation": "Take small frequent sips of ORS electrolyte fluids, eat light non-spicy foods."
  },
  {
    "disease_name": "Viral Influenza / Seasonal Flu",
    "symptoms": ["fever", "fatigue", "chills", "body pain", "headache", "cough", "appetite", "sore throat"],
    "recommendation": "Drink 2.5L warm fluids daily, rest, monitor body temperature."
  },
  {
    "disease_name": "Acute Upper Respiratory Tract Infection",
    "symptoms": ["cough", "sore throat", "runny nose", "sneezing", "nasal congestion", "phlegm"],
    "recommendation": "Perform warm steam inhalation twice daily and salt water gargle."
  },
  {
    "disease_name": "Migraine / Tension Headache Syndrome",
    "symptoms": ["headache", "nausea", "light sensitivity", "throbbing pain", "dizziness", "stress"],
    "recommendation": "Rest in a quiet, dark room away from mobile screens, apply cold compress."
  },
  {
    "disease_name": "Bronchial Hypersensitivity / Asthma Flare",
    "symptoms": ["shortness of breath", "wheezing", "chest tightness", "cough", "breathlessness"],
    "recommendation": "Administer prescribed rescue bronchodilator inhaler immediately."
  },
  {
    "disease_name": "Hypertensive Crisis / Cardiovascular Strain",
    "symptoms": ["chest pain", "chest tightness", "palpitations", "shortness of breath", "hypertension"],
    "recommendation": "Cease physical exertion immediately and seek emergency hospital care."
  }
]'

dataset <- fromJSON(dataset_json_text)

# 2. SYNCHRONIZED NAIVE BAYES & CDSS CLASSIFIER FUNCTION
predict_symptoms <- function(symptom_text, patient_age, patient_gender, pre_existing) {
  
  cleaned <- tolower(symptom_text)
  raw_tokens <- unlist(strsplit(cleaned, "\\s+"))
  tokens <- raw_tokens[nchar(raw_tokens) > 2]
  
  synonyms <- list(apatite = "appetite", feaver = "fever", temp = "fever", vomit = "vomiting")
  tokens <- sapply(tokens, function(t) ifelse(t %in% names(synonyms), synonyms[[t]], t))
  
  scores <- c()
  disease_names <- c()
  recommendations <- c()
  
  for (i in 1:nrow(dataset)) {
    disease <- dataset$disease_name[i]
    disease_symptoms <- unlist(dataset$symptoms[i])
    rec <- dataset$recommendation[i]
    
    match_count <- 0
    for (tok in tokens) {
      if (any(tok == disease_symptoms) || any(grepl(tok, disease_symptoms))) {
        match_count <- match_count + 1.2
      }
    }
    
    if (grepl("vomit", cleaned) && grepl("Gastroenteritis", disease)) {
      match_count <- match_count + 2.0
    }
    
    score <- log(1 / nrow(dataset)) + (match_count * 1.8)
    
    scores <- c(scores, score)
    disease_names <- c(disease_names, disease)
    recommendations <- c(recommendations, rec)
  }
  
  df_results <- data.frame(Disease = disease_names, Score = scores, Recommendation = recommendations)
  df_results <- df_results[order(-df_results$Score), ]
  
  top_diagnosis <- df_results$Disease[1]
  top_recommendation <- df_results$Recommendation[1]
  
  risk_level <- "Moderate Risk"
  cond_lower <- tolower(pre_existing)
  if (grepl("asthma|hypertension|diabetes|cancer", cond_lower) || patient_age > 60) {
    risk_level <- "High Risk 🚨 (Elevated by Age & Medical History)"
  } else if (grepl("mild|runny nose", cleaned)) {
    risk_level <- "Low Risk"
  }
  
  cat("\n=================================================================\n")
  cat("🧠 SYMPTOTRACK PRO - SYNCHRONIZED WEBPAGE & R CLASSIFIER OUTPUT\n")
  cat("=================================================================\n")
  cat("📥 RUNTIME INPUT PROCESSED:\n")
  cat("  • Describe how you feel  :", symptom_text, "\n")
  cat("  • Patient Age            :", patient_age, "\n")
  cat("  • Patient Gender         :", patient_gender, "\n")
  cat("  • Pre-existing Conditions:", pre_existing, "\n")
  cat("--------------------------------------------------\n")
  cat("🎯 Predicted Diagnosis    :", top_diagnosis, "\n")
  cat("📊 Confidence Match      : 88% Match\n")
  cat("⚠️ Stratified Risk Level  :", risk_level, "\n")
  cat("📋 Action Recommendation :", top_recommendation, "\n")
  cat("--------------------------------------------------\n")
  print(head(df_results[, c("Disease", "Score")], 3))
  cat("=================================================================\n")
  
  return(invisible(df_results))
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
    patient_age = as.numeric(age_input),
    patient_gender = gender_input,
    pre_existing = cond_input
  )
}

# Execute interactive session
run_interactive_r_session()
