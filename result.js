document.addEventListener("DOMContentLoaded", () => {
    const rawData = sessionStorage.getItem("analysisResult");
    
    let data = null;
    if (rawData) {
        try { data = JSON.parse(rawData); } catch(e) {}
    }

    // Require valid scan result session data - redirect to scan page if missing or empty
    if (!data || !data.condition) {
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
            { name: data.condition, matchPercentage: `${data.confidence || 85}% Match`, description: data.recommendation || "Common condition matching your input." }
        ];
    }

    if (condContainer) {
        condContainer.innerHTML = conditionsArr.map(c => `
            <div class="condition-card">
                <div class="cond-header">
                    <span class="cond-name">${c.name}</span>
                    <span class="cond-match">${c.matchPercentage}</span>
                </div>
                <p class="cond-desc">${c.description}</p>
            </div>
        `).join("");
    }

    // Render Recommended Action Plan
    const actionListEl = document.getElementById("actionPlanList");
    let recsArr = data.recommendations || [];

    if (!recsArr.length && data.next_steps) {
        recsArr = data.next_steps.map((step, idx) => ({
            title: `Step ${idx + 1}`,
            description: step
        }));
    }

    if (actionListEl && recsArr.length) {
        actionListEl.innerHTML = recsArr.map((r, idx) => `
            <div class="action-step">
                <div class="step-badge">${idx + 1}</div>
                <div class="step-content">
                    <h4>${r.title}</h4>
                    <p>${r.description}</p>
                </div>
            </div>
        `).join("");
    }
});
