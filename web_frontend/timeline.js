// timeline.js – Handles data loading, chart rendering, event list, and add‑event modal for the Symptom Timeline page

// Utility: format date as YYYY‑MM‑DD
function fmtDate(date) {
  const d = new Date(date);
  return d.toISOString().split('T')[0];
}

// Load analysis history from localStorage – fallback to sample data if empty
function loadHistory() {
  const raw = localStorage.getItem('symptotrack_analysis_history');
  if (raw) return JSON.parse(raw);
  // Sample events (3) – structure matches real analysis objects
  return [
    {
      date: new Date().setDate(new Date().getDate() - 2),
      condition: 'Common Cold',
      severity: 3,
      confidence: 92,
      symptoms: ['cough', 'sore throat'],
      recommendation: 'Rest, hydrate, over‑the‑counter decongestant.'
    },
    {
      date: new Date().setDate(new Date().getDate() - 5),
      condition: 'Migraine',
      severity: 7,
      confidence: 88,
      symptoms: ['headache', 'nausea'],
      recommendation: 'Dim lighting, magnesium supplement, NSAID if needed.'
    },
    {
      date: new Date().setDate(new Date().getDate() - 9),
      condition: 'Seasonal Allergy',
      severity: 4,
      confidence: 81,
      symptoms: ['runny nose', 'itchy eyes'],
      recommendation: 'Antihistamine, avoid pollen exposure.'
    }
  ];
}

// Save a new event into localStorage (merged with existing history)
function saveEvent(event) {
  const history = loadHistory();
  history.push(event);
  localStorage.setItem('symptotrack_analysis_history', JSON.stringify(history));
}

// Compute stats for the stats row
function computeStats(data) {
  const totalDiagnoses = data.length;
  const conditionCounts = {};
  let severitySum = 0;
  const dates = new Set();
  data.forEach(item => {
    const cond = item.condition || 'Unknown';
    conditionCounts[cond] = (conditionCounts[cond] || 0) + 1;
    severitySum += Number(item.severity) || 0;
    dates.add(fmtDate(item.date));
  });
  const mostCommon = Object.entries(conditionCounts).sort((a, b) => b[1] - a[1])[0]?.[0] || '—';
  const avgScore = totalDiagnoses ? Math.round(severitySum / totalDiagnoses) : 0;
  const daysTracked = dates.size;
  return { totalDiagnoses, mostCommon, avgScore, daysTracked };
}

// Render stats in the UI
function renderStats(stats) {
  document.getElementById('totalDiagnoses').textContent = stats.totalDiagnoses;
  document.getElementById('mostCommon').textContent = stats.mostCommon;
  document.getElementById('avgScore').textContent = stats.avgScore;
  document.getElementById('daysTracked').textContent = stats.daysTracked;
}

// Build Chart.js line chart – severity over time (date on X‑axis)
let timelineChart = null;
function renderChart(data) {
  const ctx = document.getElementById('timelineChart').getContext('2d');
  const sorted = [...data].sort((a, b) => new Date(a.date) - new Date(b.date));
  const labels = sorted.map(d => fmtDate(d.date));
  const severityVals = sorted.map(d => d.severity);
  const chartConfig = {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: 'Severity',
        data: severityVals,
        fill: true,
        backgroundColor: 'rgba(59,130,246,0.2)',
        borderColor: '#2563EB',
        tension: 0.3,
        pointBackgroundColor: '#3B82F6',
        pointBorderColor: '#fff',
        pointRadius: 4,
        pointHoverRadius: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: {
          grid: { color: 'rgba(255,255,255,0.05)' },
          ticks: { color: '#F3F4F6' }
        },
        y: {
          beginAtZero: true,
          max: 10,
          grid: { color: 'rgba(255,255,255,0.05)' },
          ticks: { color: '#F3F4F6' }
        }
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: 'rgba(0,0,0,0.8)',
          titleColor: '#fff',
          bodyColor: '#fff'
        }
      }
    }
  };
  if (timelineChart) timelineChart.destroy();
  timelineChart = new Chart(ctx, chartConfig);
}

// Render event list cards
function renderEvents(data) {
  const container = document.getElementById('eventList');
  container.innerHTML = '';
  const sorted = [...data].sort((a, b) => new Date(b.date) - new Date(a.date)); // newest first
  sorted.forEach(item => {
    const card = document.createElement('div');
    card.className = 'event-card';
    const dateStr = fmtDate(item.date);
    const badgeClass = item.severity <= 3 ? 'low' : item.severity <= 6 ? 'medium' : 'high';
    const symptomPills = (item.symptoms || []).map(sym => `<span class="badge low" style="margin-right:4px;background:#3B82F620;color:#3B82F6;">${sym}</span>`).join('');
    card.innerHTML = `
      <div class="event-header">
        <div class="event-date">${dateStr}</div>
        <div class="event-condition">${item.condition}</div>
      </div>
      <div class="badge ${badgeClass}">Severity ${item.severity}</div>
      <div style="margin-top:6px;">${symptomPills}</div>
      <p style="margin-top:8px;color:#9CA3AF;">${item.recommendation || ''}</p>
      <div class="event-actions"><button class="btn-view" onclick="alert('Full report not implemented')">View Full Report</button></div>
    `;
    container.appendChild(card);
  });
}

// Filter handling – calculate date range
function filterData(range) {
  const all = loadHistory();
  if (range === 'all') return all;
  const days = Number(range);
  const cutoff = new Date();
  cutoff.setDate(cutoff.getDate() - days);
  return all.filter(item => new Date(item.date) >= cutoff);
}

// Attach listeners for filter buttons
function initFilterBar() {
  const bar = document.getElementById('filterBar');
  bar.addEventListener('click', e => {
    if (e.target.tagName !== 'BUTTON') return;
    const range = e.target.dataset.range;
    // Update active class
    Array.from(bar.querySelectorAll('button')).forEach(btn => btn.classList.toggle('active', btn === e.target));
    const filtered = filterData(range);
    updateUI(filtered);
  });
}

// Modal handlers for adding a new event
function initModal() {
  const modal = document.getElementById('eventModal');
  const btnAdd = document.getElementById('btnAddEvent');
  const btnCancel = document.getElementById('btnCancelEvent');
  const btnSave = document.getElementById('btnSaveEvent');
  btnAdd.addEventListener('click', () => modal.classList.add('open'));
  btnCancel.addEventListener('click', () => modal.classList.remove('open'));
  btnSave.addEventListener('click', () => {
    const date = document.getElementById('eventDate').value;
    const condition = document.getElementById('eventCondition').value.trim();
    const severity = document.getElementById('eventSeverity').value;
    const notes = document.getElementById('eventNotes').value.trim();
    if (!date || !condition) {
      alert('Please fill required fields');
      return;
    }
    const newEvent = {
      date: new Date(date).getTime(),
      condition,
      severity: Number(severity),
      confidence: 90,
      symptoms: [],
      recommendation: notes
    };
    saveEvent(newEvent);
    modal.classList.remove('open');
    // Refresh UI with current filter
    const activeBtn = document.querySelector('#filterBar button.active');
    const filtered = filterData(activeBtn.dataset.range);
    updateUI(filtered);
  });
}

function updateUI(data) {
  renderStats(computeStats(data));
  renderChart(data);
  renderEvents(data);
}

// Initial load
document.addEventListener('DOMContentLoaded', () => {
  initFilterBar();
  initModal();
  const activeBtn = document.querySelector('#filterBar button.active');
  const initialData = filterData(activeBtn.dataset.range);
  updateUI(initialData);
});
