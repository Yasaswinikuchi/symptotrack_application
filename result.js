document.addEventListener("DOMContentLoaded", () => {
    const rawData = sessionStorage.getItem("analysisResult");
    
    let data = null;
    if (rawData) {
        try { data = JSON.parse(rawData); } catch(e) {}
    }

    // Require valid scan result session data - redirect to scan page if missing or empty
    if (!data || (!data.condition && !data.conditions && !data.riskLevel)) {
        console.warn("No active scan result found in session storage. Redirecting to scan page.");
        sessionStorage.removeItem("analysisResult");
        window.location.href = "scan.html";
        return;
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
            { name: data.condition, matchPercentage: `${data.confidence || 91}% Match`, description: data.recommendation || "Common condition matching your reported symptoms." }
        ];
    }

    if (condContainer && conditionsArr.length) {
        condContainer.innerHTML = conditionsArr.map(c => `
            <div class="condition-card" style="background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.1); border-radius: 16px; padding: 18px; margin-bottom: 14px;">
                <div class="cond-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                    <span class="cond-name" style="font-size: 16px; font-weight: 800; color: #FFF;">${c.name}</span>
                    <span class="cond-match" style="background: rgba(59, 130, 246, 0.2); color: #60A5FA; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 700;">${c.matchPercentage}</span>
                </div>
                <p class="cond-desc" style="font-size: 13px; color: #94A3B8; line-height: 1.5;">${c.description}</p>
            </div>
        `).join("");
    }

    // Render Recommended Action Plan
    const actionListEl = document.getElementById("recommendationsContainer") || document.getElementById("actionPlanList");
    let recsArr = data.recommendations || [];

    if (!recsArr.length && data.next_steps && Array.isArray(data.next_steps)) {
        recsArr = data.next_steps.map((step, idx) => ({
            title: typeof step === 'string' ? `Step ${idx + 1}` : (step.title || `Step ${idx + 1}`),
            description: typeof step === 'string' ? step : (step.description || step)
        }));
    }

    if (!recsArr.length) {
        recsArr = [
            { title: "Step 1: Hydration & Rest", description: "Drink 2–3 liters of warm fluids daily and ensure adequate rest to support system recovery." },
            { title: "Step 2: Symptom Relief & Care", description: data.recommendation || "Take over-the-counter pain relievers or fever reducers as directed by your healthcare provider." },
            { title: "Step 3: Monitor & Telemedicine Consultation", description: "Track daily body temperature and consult a qualified local physician if symptoms persist or intensify." }
        ];
    }

    if (actionListEl && recsArr.length) {
        actionListEl.innerHTML = recsArr.map((r, idx) => `
            <div class="action-step" style="display: flex; gap: 14px; align-items: flex-start; background: rgba(255, 255, 255, 0.04); border: 1px solid rgba(255, 255, 255, 0.08); border-radius: 16px; padding: 16px; margin-bottom: 12px;">
                <div class="step-badge" style="width: 32px; height: 32px; border-radius: 50%; background: #2563EB; color: #FFF; font-weight: 800; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">${idx + 1}</div>
                <div class="step-content" style="text-align: left;">
                    <h4 style="font-size: 14px; font-weight: 800; color: #FFF; margin-bottom: 4px;">${r.title}</h4>
                    <p style="font-size: 12px; color: #94A3B8; margin: 0; line-height: 1.4;">${r.description}</p>
                </div>
            </div>
        `).join("");
    }
});
