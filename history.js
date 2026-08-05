let historyData = [];

function openReportModal(index) {
    const record = historyData[index];
    
    if (!record || !record.full_response) {
        alert("Report details for this record are saved locally.");
        return;
    }

    sessionStorage.setItem("historyResult", record.full_response);
    sessionStorage.setItem("historyReportId", record.id);
    
    window.location.href = "history_result.html";
}

function cancelAppointment(id) {
    if (confirm("Are you sure you want to cancel this appointment?")) {
        try {
            fetch("../app_backend/api/delete_appointment.php", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ id: id })
            });
        } catch (e) {
            console.log("Local offline cancel mode");
        }
        alert("Appointment cancelled.");
        location.reload();
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const historyList = document.getElementById("historyList");
    const appointmentsList = document.getElementById("appointmentsList");
    const btnClearAll = document.getElementById("btnClearAll");

    if (btnClearAll) {
        btnClearAll.addEventListener("click", () => {
            if (confirm("Are you sure you want to clear all history records?")) {
                localStorage.removeItem('symptom_history_local');
                historyData = [];
                renderHistoryUI([]);
                alert("History cleared.");
            }
        });
    }

    // Default sample records if none in storage
    const defaultHistory = [
        {
            id: 1,
            type: "Symptom Check",
            risk_level: "Low",
            created_at: new Date().toISOString(),
            symptoms: "Fever & Head Pressure after outdoor exposure",
            full_response: JSON.stringify({ risk: "Low", diagnosis: "Tension Headache / Fatigue" })
        },
        {
            id: 2,
            type: "Voice AI Intake",
            risk_level: "Medium",
            created_at: new Date(Date.now() - 86400000).toISOString(),
            symptoms: "Throat Irritation & Seasonal Allergies",
            full_response: JSON.stringify({ risk: "Medium", diagnosis: "Allergic Rhinitis" })
        }
    ];

    function renderHistoryUI(data) {
        if (!historyList) return;
        historyData = data;

        if (!data || data.length === 0) {
            historyList.innerHTML = `<div style="text-align: center; color: #94A3B8; padding: 30px;">No history records recorded yet.</div>`;
            return;
        }

        const html = data.map((record, index) => {
            const dateObj = new Date(record.created_at || Date.now());
            const dateStr = dateObj.toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' });
            
            let badgeClass = "badge-green";
            if (record.risk_level === "Medium") badgeClass = "badge-orange";
            if (record.risk_level === "High") badgeClass = "badge-red";

            let iconSvg = `<path d="M8 7V3M16 7V3M7 11H17M5 21H19C20.1046 21 21 20.1046 21 19V7C21 5.89543 20.1046 5 19 5H5C3.89543 5 3 5.89543 3 7V19C3 20.1046 3.89543 21 5 21Z" stroke="#3B82F6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>`;
            if (record.type === "Image Scan") {
                iconSvg = `<path d="M22 12h-4l-3 9L9 3l-3 9H2" stroke="#3B82F6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>`;
            }

            return `
            <div class="history-card" onclick="openReportModal(${index})" style="cursor: pointer; background: rgba(19, 28, 49, 0.85); border: 1px solid rgba(255,255,255,0.08); border-radius: 20px; padding: 18px; margin-bottom: 14px;">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
                    <span style="font-size:15px; font-weight:800; color:#FFF;">${record.type}</span>
                    <span style="font-size:11px; color:#94A3B8;">${dateStr}</span>
                </div>
                <p style="font-size:13px; color:#94A3B8; margin-bottom:10px;">${record.symptoms}</p>
                <span style="background:rgba(59,130,246,0.15); color:#38BDF8; font-size:11px; font-weight:700; padding:4px 10px; border-radius:12px;">AI Risk: ${record.risk_level || 'Low'}</span>
            </div>
            `;
        }).join("");

        historyList.innerHTML = html;
    }

    // Try fetching from backend, fallback seamlessly to localStorage + defaultHistory
    const storedHistory = JSON.parse(localStorage.getItem('symptom_history_local') || 'null');
    
    fetch("../app_backend/api/history.php")
        .then(res => res.json())
        .then(data => {
            if (Array.isArray(data) && data.length > 0) {
                renderHistoryUI(data);
            } else {
                renderHistoryUI(storedHistory || defaultHistory);
            }
        })
        .catch(err => {
            // Offline/Local Fallback
            renderHistoryUI(storedHistory || defaultHistory);
        });
});
