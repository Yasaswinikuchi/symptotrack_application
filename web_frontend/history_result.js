document.addEventListener("DOMContentLoaded", () => {
    const rawData = sessionStorage.getItem("historyResult");
    const reportId = sessionStorage.getItem("historyReportId");

    if (!rawData || !reportId) {
        alert("No history result found. Returning to history.");
        window.location.href = "history.html";
        return;
    }

    const data = JSON.parse(rawData);
    
    // Set Risk Level
    const riskValue = document.getElementById("riskValue");
    const riskIcon = document.getElementById("riskIcon");
    const riskDescription = document.getElementById("riskDescription");
    
    riskValue.textContent = data.riskLevel || "Unknown";
    riskDescription.textContent = data.riskDescription || "Please consult a healthcare professional.";
    
    if (data.riskLevel === "High") {
        riskIcon.className = "risk-icon high";
        riskValue.style.color = "#DC2626"; // Red
    } else if (data.riskLevel === "Medium") {
        riskIcon.className = "risk-icon medium";
        riskValue.style.color = "#0F172A"; // Dark
    } else {
        riskIcon.className = "risk-icon low";
        riskValue.style.color = "#16A34A"; // Green
    }

    // Populate Conditions
    const conditionsList = document.getElementById("conditionsList");
    if (data.conditions && data.conditions.length > 0) {
        conditionsList.innerHTML = data.conditions.map((cond, index) => {
            const colorClass = index % 2 === 0 ? "border-orange" : "border-yellow";
            const badgeClass = index % 2 === 0 ? "badge-orange" : "badge-yellow";
            
            return `
            <div class="condition-card ${colorClass}">
                <div class="condition-header">
                    <h3 class="condition-name">${cond.name}</h3>
                    <span class="match-badge ${badgeClass}">${cond.matchPercentage}</span>
                </div>
                <p class="condition-desc">${cond.description}</p>
            </div>
            `;
        }).join("");
    } else {
        conditionsList.innerHTML = "<p>No specific conditions identified.</p>";
    }

    // Populate Recommendations
    const recommendationsList = document.getElementById("recommendationsList");
    if (data.recommendations && data.recommendations.length > 0) {
        const itemsHtml = data.recommendations.map((rec, index) => {
            const iconSvg = (rec.icon === "alert" || index === 0)
                ? `<svg viewBox="0 0 24 24" fill="none" class="rec-icon orange"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><path d="M12 8v4M12 16h.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>`
                : `<svg viewBox="0 0 24 24" fill="none" class="rec-icon green"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><path d="M8 12l3 3 5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>`;

            return `
            <div class="rec-item">
                <div class="rec-icon-wrapper">
                    ${iconSvg}
                </div>
                <div class="rec-content">
                    <h4 class="rec-title">${rec.title}</h4>
                    <p class="rec-desc">${rec.description}</p>
                </div>
            </div>
            `;
        }).join("");

        recommendationsList.innerHTML = itemsHtml;
    }

    const btnDelete = document.getElementById("btnDeleteHistory");
    if (btnDelete) {
        btnDelete.addEventListener("click", () => {
            if (confirm("Are you sure you want to delete this report?")) {
                fetch("../app_backend/api/delete_history.php", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ id: reportId })
                })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        alert("Report deleted.");
                        window.location.href = "history.html";
                    } else {
                        alert(data.error || "Failed to delete report");
                    }
                })
                .catch(err => {
                    alert("Error deleting report");
                    console.error(err);
                });
            }
        });
    }
});
