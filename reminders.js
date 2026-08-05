(() => {
const SK = 'symptotrack_reminders';
const TYPE_ICONS = { tablet: '💊', capsule: '🔵', syrup: '🧴', injection: '💉', drops: '👁️' };
const TYPE_COLORS = { tablet: '#3B82F6', capsule: '#7C3AED', syrup: '#F59E0B', injection: '#EF4444', drops: '#10B981' };

let reminders = [];

window.setPresetTime = (timeStr) => {
    const el = document.getElementById('remTime');
    if (el) el.value = timeStr;
};

function getCurrentTimeStr() {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    return `${hh}:${mm}`;
}

function load() {
    try { reminders = JSON.parse(localStorage.getItem(SK) || '[]'); }
    catch { reminders = []; }
    render();
}

function save() {
    localStorage.setItem(SK, JSON.stringify(reminders));
}

function getStats() {
    const now = new Date();
    const today = now.toDateString();
    const takenToday = reminders.filter(r => r.takenDates && r.takenDates.includes(today)).length;
    const dueToday = reminders.filter(r => {
        if (!r.time) return false;
        const [h, m] = r.time.split(':').map(Number);
        const due = new Date(); due.setHours(h, m, 0);
        return due > now;
    }).length;
    return { total: reminders.length, due: dueToday, taken: takenToday };
}

function openAddModal() {
    const modal = document.getElementById('addModal');
    const timeEl = document.getElementById('remTime');
    if (timeEl && !timeEl.value) {
        timeEl.value = getCurrentTimeStr();
    }
    if (modal) modal.classList.add('open');
}

function render() {
    const grid = document.getElementById('remindersGrid');
    const stats = getStats();
    document.getElementById('statTotal').textContent = stats.total;
    document.getElementById('statDue').textContent   = stats.due;
    document.getElementById('statTaken').textContent = stats.taken;

    if (reminders.length === 0) {
        grid.innerHTML = `
        <div class="empty-state-container">
            <div class="icon-circle-bg">
                <svg viewBox="0 0 24 24" fill="none" stroke="#2563EB" stroke-width="2">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                    <polyline points="14 2 14 8 20 8"></polyline>
                    <line x1="16" y1="13" x2="8" y2="13"></line>
                    <line x1="16" y1="17" x2="8" y2="17"></line>
                    <polyline points="10 9 9 9 8 9"></polyline>
                </svg>
            </div>
            <h2 class="empty-title">No Reminders Available</h2>
            <p class="empty-subtitle">You haven't scheduled any medication reminders yet. Set up daily alerts to get real-time dose notifications.</p>
            <button class="btn-book-new" onclick="openAddModalDirect()">
                + ADD NEW REMINDER
            </button>
        </div>`;
        return;
    }

    const today = new Date().toDateString();
    grid.innerHTML = reminders.map((r, i) => {
        const taken = r.takenDates && r.takenDates.includes(today);
        const icon = TYPE_ICONS[r.type] || '💊';
        const color = TYPE_COLORS[r.type] || '#3B82F6';
        return `
        <div class="reminder-card ${taken ? 'taken' : ''}" id="card-${i}">
            <div class="rem-icon" style="background:${color}22;">${icon}</div>
            <div class="rem-name">${r.name}</div>
            <div class="rem-dose">${r.dose || 'As prescribed'}</div>
            <div class="rem-meta">
                <span class="rem-badge" style="background:${color}22;color:${color};">${r.freq}</span>
                <span class="rem-badge" style="background:rgba(255,255,255,0.07);color:#9CA3AF;">⏰ ${r.time || '08:00 AM'}</span>
            </div>
            ${r.notes ? `<div style="font-size:12px;color:#6B7280;margin-bottom:12px;font-style:italic;">${r.notes}</div>` : ''}
            <div class="rem-actions">
                <button class="btn-take" onclick="markTaken(${i})">${taken ? '✅ Taken' : '✔ Mark Taken'}</button>
                <button class="btn-del" onclick="deleteReminder(${i})">🗑</button>
            </div>
        </div>`;
    }).join('');
}

window.openAddModalDirect = openAddModal;

window.markTaken = (idx) => {
    const today = new Date().toDateString();
    if (!reminders[idx].takenDates) reminders[idx].takenDates = [];
    const pos = reminders[idx].takenDates.indexOf(today);
    if (pos > -1) reminders[idx].takenDates.splice(pos, 1);
    else reminders[idx].takenDates.push(today);
    save(); render();
    showToast(pos > -1 ? '↩️ Marked as not taken' : '✅ Medicine marked as taken!');
};

window.deleteReminder = (idx) => {
    if (confirm(`Delete reminder for "${reminders[idx].name}"?`)) {
        reminders.splice(idx, 1);
        save(); render();
    }
};

function showToast(msg) {
    const t = document.createElement('div');
    t.className = 'toast';
    t.textContent = msg;
    document.body.appendChild(t);
    setTimeout(() => t.remove(), 3000);
}

// ── Modal logic ─────────────────────────────────────────────────────────────
const modal    = document.getElementById('addModal');
const fabAdd   = document.getElementById('fabAdd');
const btnCancel = document.getElementById('btnCancelAdd');
const btnSave   = document.getElementById('btnSaveAdd');

if (fabAdd) fabAdd.addEventListener('click', openAddModal);
if (btnCancel) btnCancel.addEventListener('click', () => modal.classList.remove('open'));
if (modal) modal.addEventListener('click', (e) => { if (e.target === modal) modal.classList.remove('open'); });

if (btnSave) {
    btnSave.addEventListener('click', () => {
        const name = document.getElementById('remName').value.trim();
        if (!name) { alert('Please enter a medication name.'); return; }
        
        let chosenTime = document.getElementById('remTime').value;
        if (!chosenTime) {
            chosenTime = getCurrentTimeStr();
        }

        reminders.push({
            name, id: Date.now(),
            dose:  document.getElementById('remDose').value.trim() || '1 tablet',
            type:  document.getElementById('remType').value || 'tablet',
            freq:  document.getElementById('remFreq').value || 'Daily',
            time:  chosenTime,
            notes: document.getElementById('remNotes').value.trim(),
            takenDates: []
        });
        save(); render();
        modal.classList.remove('open');
        showToast('💊 Reminder added successfully!');
        ['remName','remDose','remNotes'].forEach(id => document.getElementById(id).value = '');
    });
}

load();
})();
