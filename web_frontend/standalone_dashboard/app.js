/**
 * VITALIS AI - Standalone Interactive Application Logic
 * Modern JavaScript, Event Handlers, Chart Interactivity & AI Bot Simulation
 */

document.addEventListener('DOMContentLoaded', () => {
    // --- Initial State & Mock Data ---
    const state = {
        theme: localStorage.getItem('vitalis_theme') || 'dark',
        waterIntake: 2250,
        waterGoal: 3000,
        symptomsHistory: [
            { id: 1, title: 'Mild Tension Headache', location: 'Head & Neck', severity: 3, time: '2 hours ago', notes: 'Triggered by prolonged screen focus' },
            { id: 2, title: 'Slight Fatigue', location: 'General Body', severity: 2, time: 'Yesterday, 04:30 PM', notes: 'Post-workout recovery' }
        ],
        chartMetric: 'heart',
        chartData: {
            heart: {
                line: 'M40,140 L110,120 L190,160 L280,90 L370,110 L460,70 L550,100',
                area: 'M40,140 L110,120 L190,160 L280,90 L370,110 L460,70 L550,100 L550,180 L40,180 Z',
                points: ['68 bpm', '72 bpm', '64 bpm', '84 bpm', '76 bpm', '90 bpm', '75 bpm'],
                color: '#3b82f6'
            },
            bp: {
                line: 'M40,100 L110,110 L190,95 L280,105 L370,100 L460,98 L550,102',
                area: 'M40,100 L110,110 L190,95 L280,105 L370,100 L460,98 L550,102 L550,180 L40,180 Z',
                points: ['116/76', '120/80', '115/75', '122/82', '118/78', '119/79', '118/78'],
                color: '#06b6d4'
            },
            spo2: {
                line: 'M40,50 L110,45 L190,55 L280,48 L370,50 L460,42 L550,45',
                area: 'M40,50 L110,45 L190,55 L280,48 L370,50 L460,42 L550,45 L550,180 L40,180 Z',
                points: ['98%', '99%', '98%', '99%', '98%', '100%', '99%'],
                color: '#10b981'
            }
        }
    };

    // --- DOM Elements ---
    const body = document.body;
    const themeToggleBtn = document.getElementById('theme-toggle');
    const themeIcon = document.getElementById('theme-icon');
    const currentDateDisplay = document.getElementById('current-date-display');
    const menuBtn = document.getElementById('menu-btn');
    const sidebar = document.getElementById('sidebar');

    // Symptom Logger Elements
    const symptomChips = document.getElementById('symptom-chips');
    const customSymptomInput = document.getElementById('symptom-custom-input');
    const severityRange = document.getElementById('severity-range');
    const severityNum = document.getElementById('severity-num');
    const symptomLocation = document.getElementById('symptom-location');
    const symptomNotes = document.getElementById('symptom-notes');
    const btnSaveSymptom = document.getElementById('btn-save-symptom');
    const historyList = document.getElementById('symptom-history-list');
    const btnClearHistory = document.getElementById('btn-clear-history');

    // Water & Med Elements
    const btnAddWater = document.getElementById('btn-add-water');
    const waterTextDisplay = document.getElementById('water-text-display');
    const waterFillBar = document.getElementById('water-fill-bar');
    const hydrationStat = document.getElementById('hydration-stat');
    const waterCupsGrid = document.getElementById('water-cups-grid');
    const medList = document.getElementById('med-list');

    // AI Drawer Elements
    const aiFabBtn = document.getElementById('ai-fab-btn');
    const aiChatDrawer = document.getElementById('ai-chat-drawer');
    const closeChatBtn = document.getElementById('close-chat-btn');
    const chatForm = document.getElementById('chat-form');
    const chatInput = document.getElementById('chat-input');
    const chatMessages = document.getElementById('chat-messages');

    // Chart Elements
    const chartTabs = document.getElementById('chart-tabs');
    const chartLine = document.getElementById('chart-line');
    const chartArea = document.getElementById('chart-area');
    const chartPointsGroup = document.getElementById('chart-points');
    const chartTooltip = document.getElementById('chart-tooltip');

    // --- Initialization Functions ---
    function init() {
        applyTheme(state.theme);
        updateDateDisplay();
        renderSymptomHistory();
        setupChartInteractivity();
        setupEventListeners();
    }

    // --- Theme Manager ---
    function applyTheme(theme) {
        state.theme = theme;
        localStorage.setItem('vitalis_theme', theme);
        if (theme === 'light') {
            body.classList.remove('dark-theme');
            body.classList.add('light-theme');
            themeIcon.className = 'fa-solid fa-sun';
        } else {
            body.classList.remove('light-theme');
            body.classList.add('dark-theme');
            themeIcon.className = 'fa-solid fa-moon';
        }
    }

    // --- Date Formatter ---
    function updateDateDisplay() {
        const now = new Date();
        const options = { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' };
        currentDateDisplay.innerHTML = `<i class="fa-regular fa-calendar-days"></i> ${now.toLocaleDateString('en-US', options)}`;
    }

    // --- Symptom Chips Selection ---
    let selectedSymptom = 'Headache';
    symptomChips.addEventListener('click', (e) => {
        if (e.target.classList.contains('chip')) {
            symptomChips.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
            e.target.classList.add('active');
            selectedSymptom = e.target.dataset.val;
            customSymptomInput.value = '';
        }
    });

    severityRange.addEventListener('input', (e) => {
        const val = parseInt(e.target.value);
        let label = 'Mild';
        let cls = 'sev-mild';
        if (val > 3 && val <= 6) {
            label = 'Moderate';
            cls = 'sev-mod';
        } else if (val > 6) {
            label = 'Severe';
            cls = 'sev-high';
        }
        severityNum.textContent = `${val} (${label})`;
        severityNum.className = cls;
    });

    // --- Save Symptom Entry ---
    btnSaveSymptom.addEventListener('click', () => {
        const title = customSymptomInput.value.trim() || selectedSymptom;
        const location = symptomLocation.value;
        const severity = parseInt(severityRange.value);
        const notes = symptomNotes.value.trim() || 'No specific trigger noted';

        const newEntry = {
            id: Date.now(),
            title: title,
            location: location,
            severity: severity,
            time: 'Just now',
            notes: notes
        };

        state.symptomsHistory.unshift(newEntry);
        renderSymptomHistory();
        showToast(`Recorded "${title}" (Severity: ${severity}/10)`);

        // Reset form
        symptomNotes.value = '';
        customSymptomInput.value = '';
    });

    // --- Render Timeline ---
    function renderSymptomHistory() {
        if (state.symptomsHistory.length === 0) {
            historyList.innerHTML = `<div class="timeline-item"><p class="timeline-desc">No symptom logs recorded today.</p></div>`;
            return;
        }

        historyList.innerHTML = state.symptomsHistory.map(item => {
            let badgeClass = 'sev-mild';
            let bgStyle = 'background: rgba(16, 185, 129, 0.15); color: #10b981;';
            if (item.severity > 3 && item.severity <= 6) {
                badgeClass = 'sev-mod';
                bgStyle = 'background: rgba(245, 158, 11, 0.15); color: #f59e0b;';
            } else if (item.severity > 6) {
                badgeClass = 'sev-high';
                bgStyle = 'background: rgba(244, 63, 94, 0.15); color: #f43f5e;';
            }

            return `
                <div class="timeline-item">
                    <div class="timeline-icon">
                        <i class="fa-solid fa-notes-medical"></i>
                    </div>
                    <div class="timeline-content">
                        <div class="timeline-title-row">
                            <span class="timeline-title">${escapeHTML(item.title)}</span>
                            <span class="badge-sev" style="${bgStyle}">Lvl ${item.severity}</span>
                        </div>
                        <p class="timeline-desc">📍 ${escapeHTML(item.location)} • ${escapeHTML(item.notes)}</p>
                        <span class="timeline-time"><i class="fa-regular fa-clock"></i> ${item.time}</span>
                    </div>
                </div>
            `;
        }).join('');
    }

    btnClearHistory.addEventListener('click', () => {
        state.symptomsHistory = [];
        renderSymptomHistory();
        showToast('Activity log cleared');
    });

    // --- Water Hydration Tracker ---
    btnAddWater.addEventListener('click', () => {
        if (state.waterIntake < state.waterGoal) {
            state.waterIntake += 250;
            if (state.waterIntake > state.waterGoal) state.waterIntake = state.waterGoal;
            updateWaterDisplay();
            showToast('Hydration logged: +250 ml 💧');
        } else {
            showToast('Daily water intake target completed! 🎉');
        }
    });

    function updateWaterDisplay() {
        const pct = Math.round((state.waterIntake / state.waterGoal) * 100);
        waterTextDisplay.textContent = `${state.waterIntake.toLocaleString()} / ${state.waterGoal.toLocaleString()} ml`;
        hydrationStat.textContent = `${(state.waterIntake / 1000).toFixed(1)} / ${(state.waterGoal / 1000).toFixed(1)} L`;
        waterFillBar.style.width = `${pct}%`;

        // Update cup icons
        const activeCupsCount = Math.floor((state.waterIntake / state.waterGoal) * 10);
        const cups = waterCupsGrid.querySelectorAll('.cup');
        cups.forEach((cup, idx) => {
            if (idx < activeCupsCount) {
                cup.classList.add('active');
            } else {
                cup.classList.remove('active');
            }
        });
    }

    // --- Medication Checklist Checkboxes ---
    medList.addEventListener('change', (e) => {
        if (e.target.type === 'checkbox') {
            const medName = e.target.dataset.med;
            const item = e.target.closest('.med-item');
            if (e.target.checked) {
                item.classList.add('checked');
                showToast(`Marked ${medName} as taken`);
            } else {
                item.classList.remove('checked');
            }
        }
    });

    // --- Analytics Chart Interactivity ---
    chartTabs.addEventListener('click', (e) => {
        if (e.target.classList.contains('tab-btn')) {
            chartTabs.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
            e.target.classList.add('active');
            
            const metric = e.target.dataset.metric;
            state.chartMetric = metric;
            updateChart(metric);
        }
    });

    function updateChart(metric) {
        const data = state.chartData[metric];
        chartLine.setAttribute('d', data.line);
        chartLine.setAttribute('stroke', data.color);
        chartArea.setAttribute('d', data.area);

        // Update point values
        const circles = chartPointsGroup.querySelectorAll('circle');
        circles.forEach((circle, idx) => {
            circle.setAttribute('fill', data.color);
            circle.setAttribute('data-val', `${data.points[idx]} at timestamp`);
        });
    }

    function setupChartInteractivity() {
        const svg = document.getElementById('vitals-chart-svg');
        chartPointsGroup.addEventListener('mouseover', (e) => {
            if (e.target.tagName === 'circle') {
                const val = e.target.getAttribute('data-val');
                chartTooltip.textContent = val;
                chartTooltip.style.opacity = '1';
                
                const rect = e.target.getBoundingClientRect();
                const containerRect = svg.getBoundingClientRect();
                chartTooltip.style.left = `${rect.left - containerRect.left - 30}px`;
                chartTooltip.style.top = `${rect.top - containerRect.top - 35}px`;
            }
        });

        chartPointsGroup.addEventListener('mouseout', (e) => {
            if (e.target.tagName === 'circle') {
                chartTooltip.style.opacity = '0';
            }
        });
    }

    // --- AI Chatbot Drawer ---
    aiFabBtn.addEventListener('click', () => {
        aiChatDrawer.classList.toggle('open');
    });

    closeChatBtn.addEventListener('click', () => {
        aiChatDrawer.classList.remove('open');
    });

    chatForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const text = chatInput.value.trim();
        if (text) {
            sendMessage(text, 'user');
            chatInput.value = '';
            
            // Bot Response Simulation
            setTimeout(() => {
                respondAI(text);
            }, 800);
        }
    });

    // Handle Quick Prompt Chips
    document.querySelectorAll('.prompt-chip').forEach(chip => {
        chip.addEventListener('click', () => {
            const prompt = chip.dataset.prompt;
            sendMessage(prompt, 'user');
            if (!aiChatDrawer.classList.contains('open')) {
                aiChatDrawer.classList.add('open');
            }
            setTimeout(() => respondAI(prompt), 800);
        });
    });

    function sendMessage(text, sender) {
        const msgDiv = document.createElement('div');
        msgDiv.className = `message msg-${sender}`;
        msgDiv.innerHTML = `<div class="msg-bubble">${escapeHTML(text)}</div>`;
        chatMessages.appendChild(msgDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function respondAI(userText) {
        const lower = userText.toLowerCase();
        let reply = "I've analyzed your question. Your overall vitals look steady. Remember to stay hydrated and take scheduled breaks if symptoms persist.";
        
        if (lower.includes('headache')) {
            reply = "Based on your logs, tension headaches often correlate with screen time. I recommend 10 minutes of neck stretching, drinking a glass of water, and reducing brightness.";
        } else if (lower.includes('blood pressure') || lower.includes('bp')) {
            reply = "Your latest blood pressure reading of 118/78 mmHg is in the **Optimal** range according to AHA guidelines. Excellent cardiovascular state!";
        } else if (lower.includes('sleep')) {
            reply = "To improve REM sleep: 1. Maintain a dark room temperature (~68°F). 2. Avoid caffeine after 2 PM. 3. Try 5 minutes of deep breathing before sleep.";
        }

        sendMessage(reply, 'bot');
    }

    // --- Navigation & Mobile Sidebar ---
    menuBtn.addEventListener('click', () => {
        sidebar.classList.toggle('open');
    });

    themeToggleBtn.addEventListener('click', () => {
        const newTheme = state.theme === 'dark' ? 'light' : 'dark';
        applyTheme(newTheme);
        showToast(`Switched to ${newTheme} theme mode`);
    });

    document.getElementById('btn-quick-log').addEventListener('click', () => {
        document.getElementById('symptom-section').scrollIntoView({ behavior: 'smooth' });
    });

    // --- Toast Notifications ---
    function showToast(message) {
        const container = document.getElementById('toast-container');
        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.innerHTML = `<i class="fa-solid fa-circle-check text-emerald"></i> <span>${escapeHTML(message)}</span>`;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(-20px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    function escapeHTML(str) {
        return str.replace(/[&<>'"]/g, 
            tag => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[tag] || tag)
        );
    }

    function setupEventListeners() {
        // Quick keyboard shortcuts or resize handlers
    }

    // Initialize Application
    init();
});
