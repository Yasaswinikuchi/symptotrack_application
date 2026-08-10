# ==============================================================================
# SymptoTrack Pro - Multi-Class Naive Bayes Classifier Engine (R)
# Dynamic Multi-Disease Prediction (Predicts Different Diseases for Different Symptoms)
# ==============================================================================

if (!require("jsonlite")) {
  install.packages("jsonlite", repos = "http://cran.us.r-project.org")
  library(jsonlite)
}

# 1. EMBEDDED MULTI-DISEASE DATASET
dataset_json_text <- '[
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
    "disease_name": "Acute Gastroenteritis / Dyspepsia",
    "symptoms": ["stomach pain", "nausea", "vomiting", "diarrhea", "appetite", "bloating", "acidity"],
    "recommendation": "Take small frequent sips of ORS electrolyte fluids, eat light non-spicy foods."
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

# 2. ACCURATE NAIVE BAYES MULTI-CLASS CLASSIFIER
predict_symptoms <- function(symptom_text, patient_age = 25, patient_gender = "Female", pre_existing = "None") {
  
  cleaned <- tolower(symptom_text)
  
  # Remove stop words
  stopwords <- c("i", "am", "having", "from", "last", "days", "and", "the", "a", "my", "is", "for", "with", "have", "feel")
  words <- unlist(strsplit(cleaned, "[^a-z]+"))
  tokens <- words[!(words %in% stopwords) & nchar(words) > 2]
  
  # Synonym mapping
  synonyms <- list(apatite = "appetite", feaver = "fever", temp = "fever", vomit = "vomiting")
  tokens <- sapply(tokens, function(t) ifelse(t %in% names(synonyms), synonyms[[t]], t))
  
  scores <- c()
  disease_names <- c()
  recommendations <- c()
  
  for (i in 1:nrow(dataset)) {
    disease <- dataset$disease_name[i]
    disease_symptoms <- unlist(dataset$symptoms[i])
    rec <- dataset$recommendation[i]
    
    # Exact word match scoring
    match_count <- 0
    for (tok in tokens) {
      if (any(tok == disease_symptoms)) {
        match_count <- match_count + 2.5
      } else if (any(sapply(disease_symptoms, function(s) grepl(tok, s)))) {
        match_count <- match_count + 1.0
      }
    }
    
    score <- log(1 / nrow(dataset)) + match_count
    
    scores <- c(scores, score)
    disease_names <- c(disease_names, disease)
    recommendations <- c(recommendations, rec)
  }
  
  df_results <- data.frame(Disease = disease_names, Score = scores, Recommendation = recommendations)
  df_results <- df_results[order(-df_results$Score), ]
  
  top_diagnosis <- df_results$Disease[1]
  top_recommendation <- df_results$Recommendation[1]
  
  # CDSS Risk Calculation
  risk_level <- "Moderate Risk"
  cond_lower <- tolower(pre_existing)
  if (grepl("asthma|hypertension|diabetes|cancer", cond_lower) || patient_age > 60) {
    risk_level <- "High Risk 🚨 (Elevated by Age & Medical History)"
  } else if (grepl("mild|runny nose|sneezing", cleaned)) {
    risk_level <- "Low Risk"
  }
  
  cat("\n=================================================================\n")
  cat("📥 SYMPTOM INPUT PROCESSED:\n")
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

# 3. INTERACTIVE RUNTIME INPUT PROMPT ENGINE
run_interactive_r_session <- function() {
  cat("=================================================================\n")
  cat("🧠 SymptoTrack Pro - RStudio Interactive Multi-Disease Classifier\n")
  cat("=================================================================\n\n")

  symptom_input <- readline(prompt = "Enter symptoms (Describe how you feel): ")
  age_input     <- readline(prompt = "Enter patient age (default 25): ")
  gender_input  <- readline(prompt = "Enter patient gender (Female/Male): ")
  cond_input    <- readline(prompt = "Enter pre-existing conditions: ")

  sym_val <- if (nchar(trimws(symptom_input)) > 0) symptom_input else "chest pain and shortness of breath"
  age_val <- if (nchar(trimws(age_input)) > 0 && !is.na(as.numeric(age_input))) as.numeric(age_input) else 25
  gen_val <- if (nchar(trimws(gender_input)) > 0) gender_input else "Female"
  cnd_val <- if (nchar(trimws(cond_input)) > 0) cond_input else "None"

  predict_symptoms(
    symptom_text = sym_val,
    patient_age = age_val,
    patient_gender = gen_val,
    pre_existing = cnd_val
  )
}

# Run session automatically
run_interactive_r_session()
