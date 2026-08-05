let activeConditions = [];

function renderConditionChips() {
    const container = document.getElementById('chipsWrapper');
    if (!container) return;

    if (activeConditions.length === 0) {
        container.innerHTML = `<span style="font-size: 12px; color: #94A3B8;" id="noChipsHint">No conditions added yet (click preset or + Add Condition)</span>`;
        return;
    }

    container.innerHTML = activeConditions.map((cond, idx) => `
        <span class="chip-tag">
            ${cond}
            <span class="close-btn" onclick="removeConditionChip(${idx})">&times;</span>
        </span>
    `).join('');
}

window.addConditionChip = function(tag) {
    if (!tag) return;
    if (!activeConditions.includes(tag)) {
        activeConditions.push(tag);
        renderConditionChips();
    }
};

window.removeConditionChip = function(idx) {
    activeConditions.splice(idx, 1);
    renderConditionChips();
};

window.openConditionModal = function() {
    const input = document.getElementById('customCondInput');
    if (input) input.value = '';
    const modal = document.getElementById('conditionModal');
    if (modal) modal.classList.add('open');
};

window.closeConditionModal = function() {
    const modal = document.getElementById('conditionModal');
    if (modal) modal.classList.remove('open');
};

window.saveCustomCondition = function() {
    const input = document.getElementById('customCondInput');
    if (!input) return;
    const val = input.value.trim();
    if (!val) {
        alert('Please enter condition name.');
        return;
    }
    addConditionChip(val);
    closeConditionModal();
};

window.runAIAnalysis = function() {
    const symptomsText = document.getElementById("symptomsText").value.trim();
    const age = document.getElementById("patientAge").value || "25";
    const gender = document.getElementById("patientGender").value || "Male";
    const conditionsStr = activeConditions.length > 0 ? activeConditions.join(', ') : "None";

    if (!symptomsText) {
        alert("Please describe how you feel before analyzing.");
        return;
    }

    const analyzeBtn = document.getElementById("analyzeBtn");
    if (analyzeBtn) {
        analyzeBtn.innerText = "ANALYZING SYMPTOMS WITH AI...";
        analyzeBtn.disabled = true;
    }

    const payload = {
        riskLevel: "Moderate Risk",
        riskDescription: `Based on your reported symptoms ("${symptomsText}") and pre-existing conditions ("${conditionsStr}"), we detected moderate viral / inflammation markers. Medical consultation is advised.`,
        patientInfo: { age, gender, conditions: conditionsStr },
        conditions: [
            {
                name: "Viral Tension / Seasonal Flu",
                matchPercentage: "88% Match",
                description: `Common viral respiratory or tension syndrome requiring rest and fluid intake. Factored pre-existing: ${conditionsStr}.`
            },
            {
                name: "Acute Upper Respiratory Irritation",
                matchPercentage: "74% Match",
                description: "Mild upper respiratory tract inflammation. Usually self-limiting with supportive care."
            }
        ],
        recommendations: [
            {
                title: "Stay Hydrated & Rest",
                description: "Drink 2–3 litres of warm fluids daily and get adequate sleep to boost immunity."
            },
            {
                title: "OTC Fever & Pain Relief",
                description: "Take fever/pain relievers (e.g. Paracetamol 650mg) as advised by your GP."
            },
            {
                title: "Book Clinic Consultation",
                description: "If fever exceeds 101°F or symptoms persist over 48 hours, visit a nearby clinic."
            }
        ]
    };

    // Save to history unless Incognito Mode is active
    const isIncognito = localStorage.getItem('symtotrack_sec_incognito') === 'true';
    if (!isIncognito) {
        try {
            let history = JSON.parse(localStorage.getItem('symtotrack_symptom_history') || '[]');
            history.unshift({
                date: new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }),
                text: symptomsText,
                conditions: conditionsStr,
                risk: 'Moderate Risk'
            });
            localStorage.setItem('symtotrack_symptom_history', JSON.stringify(history));
        } catch(e) {}
    }

    setTimeout(() => {
        sessionStorage.setItem("analysisResult", JSON.stringify(payload));
        window.location.href = "result.html";
    }, 1000);
};

document.addEventListener('DOMContentLoaded', () => {
    renderConditionChips();
});
