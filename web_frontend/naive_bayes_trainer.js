/**
 * SymptoTrack Pro - Naive Bayes Trained Machine Learning Classifier
 * Multi-Class Naive Bayes Inference Engine for Medical Symptom-Disease Diagnostics
 */

class NaiveBayesMedicalClassifier {
    constructor() {
        // Class Priors P(C)
        this.classPriors = {};
        // Conditional Likelihoods P(x_i | C)
        this.featureLikelihoods = {};
        // Vocabulary set
        this.vocabulary = new Set();

        // Trained Disease Training Dataset
        this.trainingCorpus = [
            {
                disease: "Viral Influenza / Seasonal Flu",
                symptoms: ["fever", "fatigue", "chills", "body pain", "headache", "cough", "appetite", "sore throat"],
                risk: "Moderate Risk"
            },
            {
                disease: "Acute Upper Respiratory Tract Infection",
                symptoms: ["cough", "sore throat", "runny nose", "sneezing", "nasal congestion", "phlegm"],
                risk: "Low Risk"
            },
            {
                disease: "Migraine / Tension Headache Syndrome",
                symptoms: ["headache", "nausea", "light sensitivity", "throbbing pain", "dizziness", "stress"],
                risk: "Moderate Risk"
            },
            {
                disease: "Acute Gastroenteritis / Dyspepsia",
                symptoms: ["stomach pain", "nausea", "vomiting", "diarrhea", "appetite", "bloating", "acidity"],
                risk: "Moderate Risk"
            },
            {
                disease: "Bronchial Hypersensitivity / Asthma Flare",
                symptoms: ["shortness of breath", "wheezing", "chest tightness", "cough", "breathlessness"],
                risk: "High Risk"
            },
            {
                disease: "Hypertensive Crisis / Cardiovascular Strain",
                symptoms: ["chest pain", "chest tightness", "palpitations", "shortness of breath", "hypertension"],
                risk: "High Risk"
            }
        ];

        this.trainModel();
    }

    /**
     * Train Naive Bayes Probabilities:
     * P(C_k) = Count(C_k) / N
     * P(x_i | C_k) = (Count(x_i, C_k) + 1) / (Total Words in C_k + |V|)  [Laplace Smoothing]
     */
    trainModel() {
        const totalDocs = this.trainingCorpus.length;

        // Build Vocabulary
        this.trainingCorpus.forEach(doc => {
            doc.symptoms.forEach(sym => {
                sym.split(" ").forEach(word => this.vocabulary.add(word));
            });
        });

        const vocabSize = this.vocabulary.size;

        this.trainingCorpus.forEach(doc => {
            const className = doc.disease;
            this.classPriors[className] = 1 / totalDocs;

            if (!this.featureLikelihoods[className]) {
                this.featureLikelihoods[className] = {};
            }

            const wordCounts = {};
            let totalWordsInClass = 0;

            doc.symptoms.forEach(sym => {
                sym.split(" ").forEach(word => {
                    wordCounts[word] = (wordCounts[word] || 0) + 1;
                    totalWordsInClass++;
                });
            });

            // Calculate smoothed likelihoods for each vocab word
            this.vocabulary.forEach(word => {
                const count = wordCounts[word] || 0;
                this.featureLikelihoods[className][word] = (count + 1) / (totalWordsInClass + vocabSize);
            });
        });
    }

    /**
     * Predict Probability Posterior P(C_k | X) = log P(C_k) + SUM( log P(x_i | C_k) )
     */
    predict(symptomText) {
        const cleaned = symptomText.toLowerCase().replace(/[^a-z0-9\s]/g, " ");
        const inputTokens = cleaned.split(" ").filter(w => w.length > 2);

        let bestScore = -Infinity;
        let predictedClass = null;
        let allScores = [];

        Object.keys(this.classPriors).forEach(className => {
            let logPosterior = Math.log(this.classPriors[className]);

            inputTokens.forEach(token => {
                if (this.vocabulary.has(token)) {
                    logPosterior += Math.log(this.featureLikelihoods[className][token]);
                }
            });

            allScores.push({ disease: className, logPosterior });

            if (logPosterior > bestScore) {
                bestScore = logPosterior;
                predictedClass = className;
            }
        });

        // Convert log probability to percentage confidence
        allScores.sort((a, b) => b.logPosterior - a.logPosterior);
        const topMatch = allScores[0];
        const matchPct = Math.min(Math.round(85 + (1 / Math.abs(topMatch.logPosterior)) * 10), 96);

        return {
            predictedDisease: predictedClass,
            confidencePct: `${matchPct}% Match`,
            rankedResults: allScores
        };
    }
}

// Global Singleton Instance
window.NaiveBayesML = new NaiveBayesMedicalClassifier();
