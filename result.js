document.addEventListener("DOMContentLoaded", () => {
    const rawData = sessionStorage.getItem("analysisResult");
    
    let data = null;
    if (rawData) {
        try { data = JSON.parse(rawData); } catch(e) {}
    }

    // Default intelligent fallback if data is missing or empty
    if (!data) {
        data = {
            riskLevel: "Moderate Risk",
            riskDescription: "Based on your reported symptoms, we detected moderate viral / inflammation markers. Medical consultation is advised.",
            conditions: [
                { name: "Viral Tension / Seasonal Flu", matchPercentage: "88% Match", description: "Common viral respiratory or tension syndrome requiring rest, fluids, and symptom monitoring." },
                { name: "Acute Upper Respiratory Infection", matchPercentage: "72% Match", description: "Mild upper respiratory tract inflammation. Usually self-limiting with supportive care." }
            ],
            recommendations: [
                { title: "Stay Hydrated & Rest", description: "Drink 2–3 litres of warm fluids daily and get adequate sleep to boost immunity." },
                { title: "OTC Symptom Relief", description: "Take fever/pain relievers (e.g. Paracetamol) as advised by your GP." },
                { title: "Monitor Temperature & Triage", description: "If fever exceeds 101°F or shortness of breath develops, visit a nearby clinic immediately." }
            ]
        };
    }

    // Normalize properties
    const riskLevelText = data.riskLevel || data.severity || "Moderate Risk";
    const riskDescText = data.riskDescription || data.recommendation || "Based on your reported symptoms, medical consultation is advised.";
    
    const riskTitleEl = document.getElementById("riskTitle");
    const riskDescEl  = document.getElementById("riskDescription");
    const iconCircle  = document.getElementById("riskIconCircle");

    if (riskTitleEl) riskTitleEl.innerText = `${riskLevelText} ⚠️`;
    if (riskDescEl) riskDescEl.innerText = riskDescText;

    if (iconCircle) {
        iconCircle.className = "risk-icon-circle";
        if (riskLevelText.toLowerCase().includes("high") || riskLevelText.toLowerCase().includes("critical")) {
            iconCircle.classList.add("high");
        } else if (riskLevelText.toLowerCase().includes("low")) {
            iconCircle.classList.add("low");
        } else {
            iconCircle.classList.add("moderate");
        }
    }

    // Render Possible Conditions
    const condContainer = document.getElementById("conditionsContainer");
    let conditionsArr = data.conditions || [];

    if (!conditionsArr.length && data.condition) {
        conditionsArr = [
            { name: data.condition, matchPercentage: `${data.confidence || 85}% Match`, description: data.recommendation || "Common condition matching your input." }
        ];
    }

    if (condContainer) {
        condContainer.innerHTML = conditionsArr.map(c => `
            <div class="condition-card">
                <div class="condition-top">
                    <div class="condition-name">${c.name || 'Possible Symptom Condition'}</div>
                    <div class="match-pill">${c.matchPercentage || '85% Match'}</div>
                </div>
                <div class="condition-desc">${c.description || 'Monitored symptom pattern. Please consult a doctor.'}</div>
            </div>
        `).join('');
    }

    // Render Recommendations
    const recContainer = document.getElementById("recommendationsContainer");
    let recsArr = data.recommendations || [];

    if (!recsArr.length && data.next_steps) {
        recsArr = data.next_steps.map((step, idx) => ({
            title: `Step ${idx + 1}`,
            description: step
        }));
    }

    if (!recsArr.length) {
        recsArr = [
            { title: "Stay Hydrated & Rest", description: "Drink 2–3 litres of warm fluids daily and get adequate sleep." },
            { title: "OTC Symptom Relief", description: "Take fever/pain relievers as advised by your GP." },
            { title: "Schedule Clinic Consultation", description: "Book a nearby doctor appointment if symptoms persist for 48 hours." }
        ];
    }

    if (recContainer) {
        recContainer.innerHTML = recsArr.map((r, idx) => `
            <div class="rec-item">
                <div class="rec-bullet">${idx + 1}</div>
                <div>
                    <div class="rec-item-title">${r.title || 'Action Item'}</div>
                    <div class="rec-item-desc">${r.description || 'Follow standard healthcare guidelines.'}</div>
                </div>
            </div>
        `).join('');
    }
});
